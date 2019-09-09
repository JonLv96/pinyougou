app.controller('searchController',function($scope,searchService){
	
	//搜索
	$scope.search=function(){
		searchService.search($scope.searchMap).success(function(response){
			$scope.resultMap=response;
		});
	}
	//定义搜索对象的结构， 存储搜索处面包屑的值 
	$scope.searchMap={'keywords':'','category':'','brand':'','spec':{}};
	//添加搜索项，改变searchMap的值
	$scope.addSearchItem=function(key,value){
		if(key == 'category' || key == 'brand'){	//如果点击的是 分类 或者品牌
			$scope.searchMap[key] = value;
		}else{	//如果点击的是 规格
			$scope.searchMap.spec[key] = value;
		}
		$scope.search();//查询
	}
	
	//撤销搜索项
	$scope.removeSearchItem=function(key){
		if(key == 'category' || key == 'brand'){	//如果点击的是 分类 或者品牌
			$scope.searchMap[key] = "";
		}else{	//如果点击的是 规格
			delete $scope.searchMap.spec[key];
		}
		$scope.search();//查询
	}
});