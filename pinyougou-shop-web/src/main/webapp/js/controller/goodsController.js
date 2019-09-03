 //控制层 
app.controller('goodsController' ,function($scope,$controller   ,goodsService,uploadService,itemCatService,typeTemplateService){	
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//增加商品
	$scope.add=function(){				
		$scope.entity.goodsDesc.introduction=editor.html();
		
		
		goodsService.add($scope.entity).success(
			function(response){
				if(response.success){
					alert("新增成功！！！")
					$scope.entity={};
					editor.html("");	//清空富文本编辑器
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//上传图片
	$scope.image_entity={};
	$scope.uploadFile=function(){
		uploadService.uploadFile().success(function(response){
			if(response.success){
				$scope.image_entity.url=response.message;
				
			}else{
				alert(response.message);
			}
		}).error(function() {           
        	     alert("上传发生错误");
        });     
	}
	
	$scope.entity={goods:{},goodsDesc:{itemImages:[],specificationItems:[]} };
	//添加图片
	$scope.add_image_entity=function(){
		$scope.entity.goodsDesc.itemImages.push($scope.image_entity);
	}
	//移除图片
	$scope.remove_image_entity=function(index){
		$scope.entity.goodsDesc.itemImages.splice(index,1);
	}
	
	//查询一级商品分类
	$scope.selectItemCat1List=function(){
		itemCatService.findByParentId(0).success(function(response){
			$scope.itemCat1List=response;
			
		});
	}
	
	//二级分类 angularjs 自带方法，用于监控 变量的变化
	$scope.$watch('entity.goods.category1Id',function(newValue,oldValue){
		itemCatService.findByParentId(newValue).success(function(response){
			$scope.itemCat2List=response;
			//如果直接选取一级分类， 那么 三级分类和，模板Id应该置空
			$scope.itemCat3List=null;
			$scope.entity.goods.typeTemplateId=null;
		});
		
	});
	//三级分类 angularjs 自带方法，用于监控 变量的变化
	$scope.$watch('entity.goods.category2Id',function(newValue,oldValue){
		itemCatService.findByParentId(newValue).success(function(response){
			$scope.itemCat3List=response;
			//如果直接选取二级分类， 模板Id应该置空
			$scope.entity.goods.typeTemplateId=null;
		});
		
	});
	//读取模板ID
	$scope.$watch('entity.goods.category3Id',function(newValue,oldValue){
		itemCatService.findByParentId(newValue).success(function(response){
			itemCatService.findOne(newValue).success(function(response){
				$scope.entity.goods.typeTemplateId=response.typeId;
			});
		});
	});
	//读取模板ID 变化后  读取品牌列表,扩展属性，规格列表
	$scope.$watch('entity.goods.typeTemplateId',function(newValue,oldValue){
		typeTemplateService.findOne(newValue).success(function(response){
			$scope.typeTemplate=response;
			$scope.typeTemplate.brandIds=JSON.parse($scope.typeTemplate.brandIds);//品牌列表类型转换
			
			//扩展属性
			$scope.entity.goodsDesc.customAttributeItems=JSON.parse($scope.typeTemplate.customAttributeItems);//品牌列表类型转换
			
		});
		
		//规格列表
		typeTemplateService.findSpecList(newValue).success(function(response){
			$scope.specList = response;
		});
		
	});
	
	
	//规格中的   规格勾选
	$scope.updateSpecAttribute=function($event,name,value){
		var object = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems,'attributeName',name);
		
		if(object != null){
			if($event.target.checked){
				object.attributeValue.push(value);
			}else{//取消勾选
				object.attributeValue.splice(object.attributeValue.indexOf(value),1);
				
				//如果选项都取消了 ，将此条记录移除
				if(object.attributeValue.length==0){
					$scope.entity.goodsDesc.specificationItems.splice(
							$scope.entity.goodsDesc.specificationItems.indexOf(object),1);
				}
			}
		}else{
			$scope.entity.goodsDesc.specificationItems.push({"attributeName":name,"attributeValue":[value]});
		}
	}
	
	//创建SKU列表
	$scope.createItemList=function(){
		$scope.entity.itemList=[{spec:{},price:0,num:9999,status:'0',isDefault:'0'}];	//列表初始化
		
		var items=$scope.entity.goodsDesc.specificationItems;
		
		for(var i=0;i<items.length;i++){
			$scope.entity.itemList = addColumn($scope.entity.itemList,items[i].attributeName,items[i].attributeValue);
			
		}
		
	}
	
	//加$.scope 是为了页面能调用， 如果页面不需要调用， 只是在controller中调用
	//list：勾选的规格列表 ， columnName：属性列的列名，columnValues：属性列的值
	addColumn=function(list,columnName,columnValues){
		//创建一个用于存储SKU 的列表
		var newList=[];
		//遍历lis 有多少种规格
		for(var i=0;i<list.length;i++){
			//浅拷贝， 二者地址相同
			var oldRow = list[i];
			
			//遍历规格中attributeValue 中存储的是一个 数组 ，中有多个值，对其进行遍历
			for(var j=0;j<columnValues.length;j++){
				//深拷贝，二者地址不同
				var newRow =JSON.parse(JSON.stringify(oldRow));
				//将列属性一一 赋给 spec
				newRow.spec[columnName]=columnValues[j];
				newList.push(newRow);
			}
		}
		
		return newList;
	}
	
	
	
	
});	
