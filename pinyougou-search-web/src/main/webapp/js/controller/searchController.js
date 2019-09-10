app.controller('searchController',function($scope,$location,searchService){
	
	//搜索
	$scope.search=function(){
		$scope.searchMap.pageNo=parseInt($scope.searchMap.pageNo);
		searchService.search($scope.searchMap).success(function(response){
			$scope.resultMap=response;
			buildPageLabel();//构建分页栏
		});
	}
	buildPageLabel=function(){
		//构建分页标签
		$scope.pageLabel = [];
		var firstPage = 1;	//开始页码
		var lastPage = $scope.resultMap.totalPages;	//截至页码
		
		$scope.firstDot=true;//开始位置  有点
		$scope.lastDot=true;//结尾位置  有点
		
		if($scope.resultMap.totalPages>5){//当总页数大于5
			if($scope.searchMap.pageNo<=3){//如果当前页小于三， 显示前五页
				$scope.firstDot=false;
				lastPage=5;
			}else if($scope.searchMap.pageNo>=$scope.resultMap.totalPages-2){//如果当前页 大于 （总页数-2）  显示后五页
				$scope.lastDot=false;
				firstPage=$scope.resultMap.totalPages-4
			}else{//显示当前页的 前两页后后两页
				firstPage=$scope.searchMap.pageNo-2;
				lastPage=$scope.searchMap.pageNo+2;
			}
		}else{//总页数小于5
			$scope.firstDot=false;//开始位置  无点
			$scope.lastDot=false;//结尾位置  无点

		}
		//构建页码
		for(var i=firstPage;i<=lastPage;i++){
			$scope.pageLabel.push(i);
		}
		
	}
	
	//定义搜索对象的结构， 存储搜索处面包屑的值 
	$scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':40,'sort':'','sortField':''};
	//添加搜索项，改变searchMap的值
	$scope.addSearchItem=function(key,value){
		if(key == 'category' || key == 'brand' || key=='price'){	//如果点击的是 分类 或者品牌
			$scope.searchMap[key] = value;
		}else{	//如果点击的是 规格
			$scope.searchMap.spec[key] = value;
		}
		$scope.search();//查询
	}
	
	//撤销搜索项
	$scope.removeSearchItem=function(key){
		if(key == 'category' || key == 'brand' || key=='price'){	//如果点击的是 分类 或者品牌
			$scope.searchMap[key] = "";
		}else{	//如果点击的是 规格
			delete $scope.searchMap.spec[key];
		}
		$scope.search();//查询
	}
	
	$scope.queryByPage=function(pageNo){
		if(pageNo<1 || pageNo>$scope.resultMap.totalPages){
			return;
		}
		$scope.searchMap.pageNo=pageNo;
		$scope.search();
	}
	
	//判断当前页是否为首页
	$scope.isTopPage=function(){
		if($scope.searchMap.pageNo == 1){
			return true;
		}else{
			return false;
		}
	}
	//判断当前页是否为尾页
	$scope.isEndPage=function(){
		if($scope.searchMap.pageNo == $scope.resultMap.totalPages){
			return true;
		}else{
			return false;
		}
	}
	
	//排序查询
	$scope.sortSearch=function(sortField,sort){
		$scope.searchMap.sortField=sortField;
		$scope.searchMap.sort=sort;
		$scope.search();
	}
	
	//判断关键字是否含有品牌
	$scope.keywordsIsBrand=function(){
		for(var i=0;i<$scope.resultMap.brandList.length;i++){
			if($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text) >=0){
				return true;
			}
		}
		
		return false;
	}
	
	//加载关键字
	$scope.loadkeywords=function(){
		$scope.searchMap.keywords=$location.search()['keywords'];
		$scope.search();
	}
	
	
});