app.controller('indexController',function($scope,loginService){
	//显示当前用用户名
	$scope.showLoginName = function(){
		loginService.loginName().success(
			function(response){
				$scope.sellerId = response.loginName;
			});
	}

});