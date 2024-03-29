package com.pinyougou.manager.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;

import entity.PageResult;
import entity.Result;

//组合注解：@Controller、@ResponseBody、@Target(value={TYPE})、@Retention(value=RUNTIME)、@Documented、@RestController	
@RestController
@RequestMapping("/brand")
public class BrandController {

	@Reference
	private BrandService brandService;
	
	@RequestMapping("/findAll")
	public List<TbBrand> findAll() {
		return brandService.findAll();
	}
	
	@RequestMapping("/findPage")
	public PageResult findPage(int page,int size) {
		return brandService.findPage(page, size);
	}
	
	@RequestMapping("/add")
	public Result add(@RequestBody TbBrand tbBrand) {
		System.out.println("add1111111111111111");
		try {
			brandService.add(tbBrand);
			return new Result(true, "添加成功！！！");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "添加失败！！！");
		}
	}

	@RequestMapping("/findOne")
	public TbBrand findOne(Long id) {
		System.out.println("findOne1111111111111111");
		return brandService.findOne(id);
	}
	
	@RequestMapping("/update")
	public Result update(@RequestBody TbBrand tbBrand) {
		System.out.println(tbBrand);
		System.out.println("update1111111111111111");
		try {
			brandService.update(tbBrand);
			return new Result(true, "更新成功！！！");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "更新失败！！！");
		}
	}
	
	
	//删除
	@RequestMapping("/delete")
	public Result delete(Long[] ids) {
		try {
			brandService.delete(ids);
			return new Result(true, "删除成功！！！");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败！！！");
		}
		
	}
	
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbBrand tbBrand, int page,int size) {
		return brandService.findPage(tbBrand, page, size);
	}
	
	@RequestMapping("/selectOptionList")
	public List<Map> selectOptionList(){
		
		return brandService.selectOptionList();
	}
}
