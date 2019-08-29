package com.pinyougou.sellergoods.service;

import java.util.List;
import java.util.Map;

import com.pinyougou.pojo.TbBrand;

import entity.PageResult;

/**
 * 
 * 品牌接口
 * @author Administrator
 *
 */
public interface BrandService {

	//查询全部商品
	public List<TbBrand> findAll();
	
	/**
	 * 品牌分页
	 * @param pageNum	当前页面
	 * @param pageSize	页面总记录数
	 * @return
	 */
	public PageResult findPage(int pageNum,int pageSize);
	
	//按条件分页查询
	public PageResult findPage(TbBrand tbBrand,int pageNum,int pageSize);
	/**
	 * 增加
	 * @param tbBrand
	 */
	public void add(TbBrand tbBrand);
	
	public TbBrand findOne(Long id);
	
	public void update(TbBrand tbBrand);
	
	public void delete(Long[] id);
	
	public List<Map> selectOptionList();
	
}
