app.controller('indexController',function($scope,loginNameService){
	//显示当前用用户名
	$scope.loginName= function(){
		loginNameService.getLoginName().success(
			function(response){
				$scope.sellerId= response.loginName;
			});
	}

});