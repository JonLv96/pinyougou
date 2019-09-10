app.controller('contentService',function($scope,contentService){
	
	$scope.contentList=[];//广告列表
	$scope.findByCategoryId=function(categoryId){
		contentService.findByCategoryId(categoryId).success(function(response){
			$scope.contentList[categoryId]=response;
		});
	}
	
	//将广告页出 搜索的信息传递到搜索出  并且进行跳转
	$scope.search=function(){
		location.href="http://localhost:9104/search.html#?keywords="+$scope.keywords;
	}
	
	
});