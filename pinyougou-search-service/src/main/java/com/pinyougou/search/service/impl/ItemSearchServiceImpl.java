package com.pinyougou.search.service.impl;

import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.ScoredPage;

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
		
		Query query = new SimpleQuery("*:*");
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		
		ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
		
		map.put("rows", page.getContent());
		
		return map;
	}
	
}
