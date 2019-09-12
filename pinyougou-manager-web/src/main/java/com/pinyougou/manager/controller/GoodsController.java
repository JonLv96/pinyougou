package com.pinyougou.manager.controller;

import java.util.Arrays;
import java.util.List;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;
import entity.Result;
/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){			
		return goodsService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int page,int rows){			
		return goodsService.findPage(page, rows);
	}
	
	
	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods){
		try {
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public Goods findOne(Long id){
		return goodsService.findOne(id);		
	}
	
	
	
	@Autowired
	private Destination queueSolrDeleteDestination;
	@Autowired
	private Destination topicPageDeleteDestination;
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(final Long [] ids){
		try {
			goodsService.delete(ids);
			//从索引库中删除
//			itemSearchService.deleteByGoodsIds(Arrays.asList(ids));
			jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
				@Override
				public Message createMessage(Session session) throws JMSException {
					return session.createObjectMessage(ids);
				}
			});
			
			//删除每个服务器上的商品详细页
			jmsTemplate.send(topicPageDeleteDestination, new MessageCreator() {
				@Override
				public Message createMessage(Session session) throws JMSException {
					return session.createObjectMessage(ids);
				}
			});
			
			
			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
		/**
	 * 查询+分页
	 * @param brand
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){
		return goodsService.findPage(goods, page, rows);		
	}
	
	
//	@Reference(timeout = 100000)
//	private ItemSearchService itemSearchService;
	
	@Autowired
	private JmsTemplate jmsTemplate;
	
	//点对点方式   用于导入solr索引的的消息队列
	@Autowired
	private Destination queueSolrDestination;
	

	//发布订阅方式   用于生成静态页
	@Autowired
	private Destination topicPageDestination;
	
	@RequestMapping("/updateStatus")
	public Result updateStatus(Long[] ids,String status) {
		try {
			goodsService.updateStatus(ids, status);
			
			if("1".equals(status)) {
				for (Long long1 : ids) {
					System.out.println(long1);
				}
		 		//******导入到索引库
				//获取需要导入的SKU列表（此处为新审核的数据 且 状态为1 审核通过s）
				List<TbItem> itemList = goodsService.findItemListByGoodsIdListAndStatus(ids,status);
				System.out.println(itemList.size());
				//导入到solr
//				itemSearchService.importList(itemList);
				
				//改成消息队列方式, 进行解耦
				
				final String jsonString = JSON.toJSONString(itemList);//转成JJSON传输
				System.out.println(jsonString);
				jmsTemplate.send(queueSolrDestination,new MessageCreator() {
					@Override
					public Message createMessage(Session session) throws JMSException {
						return session.createTextMessage(jsonString);
					}
				});
				
				
				//****生成商品详细页
//				for(Long goodsId:ids) {
//					itemPageService.genItemHtml(goodsId);
//				}
				//****生成商品详细页
				for(final Long goodsId:ids) {
					jmsTemplate.send(topicPageDestination,new MessageCreator() {
						@Override
						public Message createMessage(Session session) throws JMSException {
							return session.createTextMessage(goodsId+"");
						}
					});
				}
				
				
				
			}
			
			return new Result(true, "成功！！！");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "失败！！！");
		}
	}
	
//	@Reference(timeout = 40000)
//	private ItemPageService itemPageService;
	
	@RequestMapping("/genHtml")
	public void genHtml(Long goodsId) {
//		itemPageService.genItemHtml(goodsId);
		// 解耦 ,采用 jms 来做
		
		
		
	}
	
}
