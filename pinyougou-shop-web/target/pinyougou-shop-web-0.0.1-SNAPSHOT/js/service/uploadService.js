app.service('uploadService',function($http){
	//上传文件方法
	this.uploadFile=function(){
		var formdata = new FormData();
		formdata.append("file",file.files[0]);//file文件上传框的name
		
		return $http({
			url:"../upload.do",
			method:'post',
			data:formdata,
			headers:{'Content-Type':undefined},//如果不设置的话 默认的未json， 设置后  就为multipart 可以上传文件
			transformRequest:angular.identity	//将文件转换成二进制格式
		});
	}
});