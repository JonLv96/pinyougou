package com.pinyougou.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.SellerService;


public class UserDetailsServiceImpl implements UserDetailsService {

	private SellerService sellerService;
	public void setSellerService(SellerService sellerService) {
		this.sellerService = sellerService;
	}



	/**
	 * 用户登录就会访问该方法
	 */
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		System.out.println("经过UserDetails ");
		//构建角色列表
		List<GrantedAuthority> grantedAuths = new ArrayList();
		grantedAuths.add(new SimpleGrantedAuthority("ROLE_SELLER"));
		
		//得到商家对象
		TbSeller seller = sellerService.findOne(username);
		System.out.println(seller);
		if(seller!=null) {
			//商家必须是通过审核的方可登录
			if(seller.getStatus().equals("1")) {
				//返回用户对象
				return new User(username,seller.getPassword(),grantedAuths);
			}else {
				return null;
			}
		}else {
			//商家不存在
			return null;
		}
		
	}

	
}
