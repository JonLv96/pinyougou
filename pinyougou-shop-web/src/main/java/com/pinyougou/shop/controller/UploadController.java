package com.pinyougou.shop.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import entity.Result;
import util.FastDFSClient;

/**
 * 
 * @author Administrator
 *
 */
@RestController
public class UploadController {

	//注解方式获取application.properties文件的值 ， springmvc中进行了配置
	@Value("${FILE_SERVER_URL}")
	private String FILE_SERVER_URL;
	
	
	@RequestMapping("/upload")
	public Result upload(MultipartFile file){
		String originalFilename = file.getOriginalFilename();
		//获取扩展名
		String extension = originalFilename.substring(originalFilename.lastIndexOf('.')+1);
		
		try {
			FastDFSClient client = new FastDFSClient("classpath:config/fdfs_client.conf");
			String fileId= client.uploadFile(file.getBytes(), extension);
			//图片完整的访问地址
			String url = FILE_SERVER_URL + fileId;
			return new Result(true, url);
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "上传失败！！！");
		}
	}
		
}
