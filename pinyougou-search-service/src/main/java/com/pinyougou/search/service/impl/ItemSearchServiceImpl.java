package com.pinyougou.search.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.FilterQuery;
import org.springframework.data.solr.core.query.GroupOptions;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.HighlightQuery;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.SolrDataQuery;
import org.springframework.data.solr.core.query.result.GroupEntry;
import org.springframework.data.solr.core.query.result.GroupPage;
import org.springframework.data.solr.core.query.result.GroupResult;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightEntry.Highlight;
import org.springframework.data.solr.core.query.result.HighlightPage;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;

//由于可能服务器性能比较差，不能在默认时间段内搜索完毕， 可能会出现超时异常， 因为这里可以对时间进行设置。
@Service(timeout = 5000)
public class ItemSearchServiceImpl implements ItemSearchService {

	@Autowired
	private SolrTemplate solrTemplate;

	@Override
	public Map search(Map searchMap) {
		Map map = new HashedMap();
		
		//空格处理
		String keywords = (String) searchMap.get("keywords");
		searchMap.put("keywords", keywords.replace(" ", ""));
		
		//1.查询列表
		map.putAll(searchList(searchMap));
		
		//2.分组查询  商品分类列表
		List<String> categoryList = searchCategoryList(searchMap);
		map.put("categoryList", categoryList);
		
		//3.查询品牌和 规格列表
		String category = (String) searchMap.get("category");
		if(!category.equals("")) {
			map.putAll(searchBrandAndSpectList(category));
		}else {
			if(categoryList.size()> 0 ) {
				map.putAll(searchBrandAndSpectList(categoryList.get(0)));
			}
		}
		
		return map;
	}
	
	//查询列表方法 
	private Map searchList(Map searchMap) {
		Map map = new HashedMap();
		 
		//普通显示
//		Query query = new SimpleQuery("*:*");
//		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
//		query.addCriteria(criteria);
//
//		ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
//
//		map.put("rows", page.getContent());

		
		// 关键词 高亮显示
		HighlightQuery query = new SimpleHighlightQuery();
		//增加高亮域
		HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");
		//设置高亮样式 前缀
		highlightOptions.setSimplePrefix("<em style='color:red'>");
		//设置高亮样式 后缀
		highlightOptions.setSimplePostfix("</em>");
		
		query.setHighlightOptions(highlightOptions);//为查询对象设置高亮选项
		
		//1.1关键字查询
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		
		//1.2 按照商品分类进行筛选
		if(!"".equals(searchMap.get("category"))) {//如果用户选择了分类
			FilterQuery filterQuery = new SimpleFilterQuery();
			Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
			filterQuery.addCriteria(filterCriteria);
			query.addFilterQuery(filterQuery);
		}
		//1.3 按照品牌进行筛选
		if(!"".equals(searchMap.get("brand"))) {//如果用户选择了品牌
			FilterQuery filterQuery = new SimpleFilterQuery();
			Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
			filterQuery.addCriteria(filterCriteria);
			query.addFilterQuery(filterQuery);
		}
		//1.4 按规格过滤
		if(searchMap.get("spec")!=null) {
			Map<String, String> specMap= (Map<String, String>) searchMap.get("spec");
			for(String key: specMap.keySet()) {
				FilterQuery filterQuery = new SimpleFilterQuery();
				Criteria filterCriteria = new Criteria("item_spec_"+key).is(specMap.get(key));
				filterQuery.addCriteria(filterCriteria);
				query.addFilterQuery(filterQuery);
			}
		}
		//1.5按及格过滤
		if(!"".equals(searchMap.get("price"))) {
			String[] price = ((String)searchMap.get("price")).split("-");
			//当最低价格为零时， 只需要让搜索价格小于最高价格 即可
			//当最高价格为*时， 只需要让搜索价格 大于最低价格即可
			if(!price[0].equals("0")) {//最低价格不等于零
				FilterQuery filterQuery = new SimpleFilterQuery();
				Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(price[0]);
				filterQuery.addCriteria(filterCriteria);
				query.addFilterQuery(filterQuery);
			}
			if(!price[1].equals("*")) {//最高价格不等于*
				FilterQuery filterQuery = new SimpleFilterQuery();
				Criteria filterCriteria = new Criteria("item_price").lessThanEqual(price[1]);
				filterQuery.addCriteria(filterCriteria);
				query.addFilterQuery(filterQuery);
			}
			
		}
		//1.6分页
		Integer pageNo= (Integer) searchMap.get("pageNo");//获取页码
		if(pageNo==null){
			pageNo=1;
		}
		Integer pageSize= (Integer) searchMap.get("pageSize");//获取页大小
		if(pageSize==null){
			pageSize=20;
		}
		
		query.setOffset((pageNo-1)*pageSize);//起始索引
		query.setRows(pageSize);//每页记录数
		
		//1.7 按价格排序
		String sortValue = (String) searchMap.get("sort");//升序 ASC ，降序DESC
		String sortField = (String) searchMap.get("sortField");//升序 ASC ，降序DESC
		if(sortValue!=null && !sortValue.equals("")) {
			if(sortValue.equals("ASC")) {
				Sort sort = new Sort(Sort.Direction.ASC, "item_"+sortField);
				query.addSort(sort);
			}
			if(sortValue.equals("DESC")) {
				Sort sort = new Sort(Sort.Direction.DESC, "item_"+sortField);
				query.addSort(sort);
			}
		}
		
		
		
		
		
		
		/*****************获取高亮结果集**********************/
		//返回高亮页对象
		HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
		//高亮入口集合
		List<HighlightEntry<TbItem>> highlighted = page.getHighlighted();
		for (HighlightEntry<TbItem> entry : highlighted) {
			//获取高亮列表(由上面设置的 ”增加高亮域“高亮域的  个数决定)
			List<Highlight> highlightList = entry.getHighlights();
//					for (Highlight h : highlightList) {
//						List<String> sns = h.getSnipplets();
//						System.out.println(sns);
//					}
			//如果 有高亮列表 才将 title  进行重新设值
			if(highlightList.size()>0 && highlightList.get(0).getSnipplets().size()>0) {
				TbItem item = entry.getEntity();
				item.setTitle(highlightList.get(0).getSnipplets().get(0));
			}
		}
		map.put("rows", page.getContent());
		map.put("totalPages", page.getTotalPages());//传入总页数
		map.put("total", page.getTotalElements());//传入总记录数
		return map;
		
	}
	
	//查询商品分类列表
	private List<String> searchCategoryList(Map searchMap) {
		List<String> list = new ArrayList();
		
		Query query = new SimpleQuery("*:*");
		//关键字查询  相对于where条件
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		//设置分组选项相对于group  by
		GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
		query.setGroupOptions(groupOptions );
		//获取分组页
		GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
		//获取分组结果对象
		GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
		//获取分组入口页
		Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
		//获取分组入口集合
		List<GroupEntry<TbItem>> entryList = groupEntries.getContent();
		for (GroupEntry<TbItem> entry : entryList) {
			list.add(entry.getGroupValue());	//将分组结果添加到返回值
		}
		return list;
	}
	
	
	@Autowired
	private RedisTemplate redisTemplate;
	/**
	 * 根据商品分类名称 拆线呢 品牌和规格列表
	 * @param category 商品分类名称
	 * @return
	 */
	private Map searchBrandAndSpectList(String category) {
		Map map = new HashedMap();
		//1.根据商品分类名称得到模板ID
		Long templateId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
		if(templateId != null) {
			//2.根据模板ID获取品牌列表
			List brandList = (List) redisTemplate.boundHashOps("brandList").get(templateId);
			map.put("brandList", brandList );
			System.out.println("品牌列表条数："+brandList.size());
			
			//3.根据模板ID 获取规格列表
			List specList = (List) redisTemplate.boundHashOps("specList").get(templateId);
			map.put("specList",specList);
			System.out.println("规格列表条数："+specList.size());
			
		}
		return map;
	}

	@Override
	public void importList(List list) {
		solrTemplate.saveBeans(list);
		solrTemplate.commit();
	}

	@Override
	public void deleteByGoodsIds(List goodIds) {
		Query query = new SimpleQuery();
		Criteria criteria = new Criteria("item_goodsid").in(goodIds);
		query.addCriteria(criteria);
		
		solrTemplate.delete(query);
		solrTemplate.commit();
	}
}
