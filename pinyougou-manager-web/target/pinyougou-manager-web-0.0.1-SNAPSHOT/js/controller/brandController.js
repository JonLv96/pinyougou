app.controller("brandController", function($scope,$controller, $http, brandService) {
	
	//伪继承，将baseController中的scope 等于  当前的scope
	$controller('baseController',{$scope:$scope});
	
	// 查询品牌列表
	$scope.findAll = function() {
		brandService.findAll().success(function(response) {
			$scope.list = response;
		});
	}

	// 分页
	$scope.findPage = function(page, size) {
		brandService.findPage(page, size).success(function(response) {
			$scope.list = response.rows; // 显示当前页数据
			$scope.paginationConf.totalItems = response.total; // 更新总记录数
		});
	}

	// 新增 & 修改
	$scope.save = function() {
		var object = null;
		if ($scope.entity.id != null) {
			object = brandService.update($scope.entity);
		} else {
			object = brandService.add($scope.entity);
		}
		object.success(function(response) {
			if (response.success) {
				$scope.reloadList();// 重新加载
			} else {
				alert(response.message);
			}
		});
	}

	// 查询实体
	$scope.findOne = function(id) {
		brandService.findOne(id).success(function(response) {
			$scope.entity = response;
		});
	}

	// 用户勾选的ID集合
	$scope.selectIds = [];

	// 删除操作
	$scope.del = function() {
		brandService.del($scope.selectIds).success(function(response) {
			if (response.success) {
				$scope.reloadList();
				$scope.selectIds = [];
			} else {
				alert(response.message);
			}
		});
	}
	$scope.searchEntity = {};
	// 条件查询
	$scope.search = function(page, size) {
		brandService.search(page, size, $scope.searchEntity).success(
				function(response) {
					$scope.list = response.rows; // 显示当前页数据
					$scope.paginationConf.totalItems = response.total; // 更新总记录数
				});
	}
});
