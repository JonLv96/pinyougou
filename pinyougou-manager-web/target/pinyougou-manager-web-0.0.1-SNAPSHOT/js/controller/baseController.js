app.controller('baseController',function($scope) {
	// 分页控件配置 *currentPage：当前页， *totalItems：总记录数
	// *itemsPerPage：每页记录数，*perPageOptions：分页选项
	// onChange：当页码变更后自动促发的方法
	$scope.paginationConf = {
		currentPage : 1,
		totalItems : 10,
		itemsPerPage : 10,
		perPageOptions : [ 10, 20, 30, 40, 50 ],
		onChange : function() {
			$scope.reloadList();// 重新加载
		} 
	};

	// 刷新列表
	$scope.reloadList = function() {
		$scope.search($scope.paginationConf.currentPage,
				$scope.paginationConf.itemsPerPage);
	}

	$scope.selectIds=[];
	// 保存勾选的 和取消勾选的
	$scope.updateSelection = function($event, id) {
		if ($event.target.checked) {
			$scope.selectIds.push(id);
		} else {
			var index = $scope.selectIds.indexOf(id);// 返回值为id 的索引
			$scope.selectIds.splice(index, 1);// 移除的位置， 移除的个数
		}
	}

	//
	$scope.jsonToString = function(jsonString,key){
		var json = JSON.parse(jsonString);
		var value="";
		
		for(var i=0;i<json.length;i++){
			
			if(i>0){
				value+=",";
			}
			value += json[i][key];
			
		}
		return value;
	}
	
});