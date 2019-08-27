package com.pinyougou.sellergoods.service;

import java.util.List;

import com.pinyougou.pojo.TbBrand;

/**
 * 
 * 品牌接口
 * @author Administrator
 *
 */
public interface BrandService {

	//查询全部商品
	public List<TbBrand> findAll();
	
}
