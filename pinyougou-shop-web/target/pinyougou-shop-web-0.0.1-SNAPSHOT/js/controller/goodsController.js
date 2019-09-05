 //控制层 
app.controller('goodsController' ,function($scope,$controller,$location   ,goodsService,uploadService,itemCatService,typeTemplateService){	
	
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
	$scope.findOne=function(){
		var id = $location.search()['id'];
		
		if(id==null){
			return;
		}
		
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;
				//将商品介绍内容填入富文本
				editor.html($scope.entity.goodsDesc.introduction);
				//商品图片处理
				$scope.entity.goodsDesc.itemImages=JSON.parse($scope.entity.goodsDesc.itemImages);
				//扩展属性的转换
				$scope.entity.goodsDesc.customAttributeItems=JSON.parse($scope.entity.goodsDesc.customAttributeItems);
				//规格选择
				$scope.entity.goodsDesc.specificationItems=JSON.parse($scope.entity.goodsDesc.specificationItems);
				//转换SKU 列表中的规格对象
				for(var i=0;i<$scope.entity.itemList.length;i++){
					$scope.entity.itemList[i].spec = JSON.parse($scope.entity.itemList[i].spec);
				}
			}
		);				
	}
	
	//保存 
	$scope.save=function(){	
		$scope.entity.goodsDesc.introduction=editor.html();
		
		var serviceObject;//服务层对象  				
		if($scope.entity.goods.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					alert("保存成功");
					location.href='goods.html';
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
			//先判断是新增还是修改，  如果是新增的话，不应该执行这句话， 应该会把应该有的值给覆盖
			if($location.search()['id']==null){
				$scope.entity.goodsDesc.customAttributeItems=JSON.parse($scope.typeTemplate.customAttributeItems);//品牌列表类型转换
			}
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
	//方便前端显示状态
	$scope.status=['未审核','已审核','审核通过','审核未通过','已关闭'];
	
	$scope.itemCatList=[]	//商品分类列表
	//查询商品分类列表
	$scope.findItemCatList=function(){
		itemCatService.findAll().success(function(response){
			for(var i=0;i<response.length;i++){
				$scope.itemCatList[response[i].id]=response[i].name;
			}
		});
	}
	
	//判断规格与给选项是否应该被勾选
	$scope.checkAttributeValue=function(specName,optionName){
		var items = $scope.entity.goodsDesc.specificationItems;
		//查询是否带有规格 以 attribute为名称的  对象
		var object = $scope.searchObjectByKey(items,'attributeName',specName);
		
		if(object){
			//判断 该规格下的    是否带有以  optionName 的规格
			if(object.attributeValue.indexOf(optionName)>=0){
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
	}
	
	
});	
