package com.pinyougou.search.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.search.service.ItemSearchService;

@RestController
@RequestMapping("/itemsearch")
public class ItemSearchController {
	
	//由于可能服务器性能比较差，不能在默认时间段内搜索完毕， 可能会出现超时异常， 因为这里可以对时间进行设置。
	@Reference//(timeout = 5000)  //或者在服务的提供方进行配置(推荐放在服务的，若两处都配置了以服务的消费方为准)
	private ItemSearchService itemSearchService;

	@RequestMapping("/search")
	public Map search(@RequestBody Map searchMap) {
		return itemSearchService.search(searchMap);
	}
	
}
