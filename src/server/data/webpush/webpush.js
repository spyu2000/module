/**
* WebPush的客户端模块
* 基于JQuery
* ops支持的参数：url，suid
*/

function WebPushClient(ops){
	this.url = ops.url+"/create_push_wait";
	this.durl = ops.url + "/destroy_push_wait";
	this.suid = ops.suid;
	this.opts = null;
	this.listenerCallback = $.Callbacks();
	this.ajax = null;
	this.pending = true;
	this.requestTimer = null;
	this.immediateRequest = false;
}

WebPushClient.prototype.updateOptionalInfo = function(opts){
	this.opts = opts;
};

WebPushClient.prototype.addMsgListener = function(listener){
	if(listener == null){
		return;
	}
	this.listenerCallback.add(listener);
};

WebPushClient.prototype.removeMsgListener = function(listener){
	if(listener == null){
		return;
	}
	
	this.listenerCallback.remove(listener);
};

WebPushClient.prototype.startServer = function(){
	this.pending = true;
	this.sendRequest();
};

WebPushClient.prototype.resendRequest = function(){
	this.immediateRequest = true;
	this.ajax.abort();
};

WebPushClient.prototype.sendRequest = function(){
	if(!this.pending){
		return;
	}
	if(this.ajax != null){
		return ;
	}
	this.immediateRequest = false;
	
	var postInfo = {flag:this.suid};
	if(this.opts){
		postInfo.opt = this.opts;
	}
	this.ajax = $.ajax({
		type:"POST",
		url:this.url,
		dataType:"xml",
		timeout:300000,
		global:false,
		error:this.requestError,
		success:this.requestSuccess,
		crossDomain:true,
		data:$.toJSONString(postInfo),
		context:this,
		cache:false
		});
};

WebPushClient.prototype.requestError = function(xmlHttp, textStatus, errorThrown){
	if(this.requestTimer != null){
		window.clearTimeout(this.requestTimer);
	}
	this.ajax = null;
	if(this.immediateRequest){
		this.requestTimer = this.sendRequest.delay(10,this);
	}else{
		this.requestTimer = this.sendRequest.delay(5000,this);
	}
};


WebPushClient.prototype.requestSuccess = function(data, textStatus, xmlHttp){
	try{
		this.disposeData(data);
	}catch(e){}
	this.ajax = null;
		
	this.sendRequest();
};
WebPushClient.prototype.disposeData = function(data, textStatus, xmlHttp){
	var root = data.documentElement;
	var arr = root.getElementsByTagName("msg");
	if(arr.length == 0){
		alert("error");
		return ;
	}
	
	var allCmd = [];
	for(var i=0;i<arr.length;i++){
		allCmd[allCmd.length] = $.evalJSON($(arr[i]).text());
	}
	
	this.listenerCallback.fire(allCmd);
};

WebPushClient.prototype.stopServer = function(){
	this.pending = false;
	if(this.requestTimer != null){
		window.clearTimeout(this.requestTimer);
		this.requestTimer = null;
	}
	
	if(this.ajax != null){
		var postInfo = {flag:this.suid};
		this.ajax.abort();
		$.ajax({
			type:"POST",
			url:this.durl,
			dataType:"xml",
			timeout:300000,
			global:false,
			crossDomain:true,
			data:$.toJSONString(postInfo),
			context:this,
			cache:false
			});
	}
};


Object.isUndefined = function(object) {
    return typeof object == "undefined";
};
Object.isFunction = function(object) {
    return typeof object == "function";
};
Array.__WebKit = (navigator.userAgent.indexOf('AppleWebKit/') > -1);
Array.__toArray = function(iterable){
	if (Array.__WebKit) {
		if (!iterable) return [];
	  if (!(Object.isFunction(iterable) && iterable == '[object NodeList]') && iterable.toArray){
	  	return iterable.toArray();
	  }
	  var length = iterable.length || 0, results = new Array(length);
	  while (length--) results[length] = iterable[length];
	 	return results;
	}else{
		if (!iterable) return [];
	  if (iterable.toArray){
	  	return iterable.toArray();
	  }
	  var length = iterable.length || 0, results = new Array(length);
	  while (length--){
	  	results[length] = iterable[length];
	  }
	  return results;
	}
};
Function.prototype.bind = function(){
	if (arguments.length < 2 && Object.isUndefined(arguments[0])) return this;
    var __method = this, args = Array.__toArray(arguments), object = args.shift();
    return function() {
      return __method.apply(object, args.concat(Array.__toArray(arguments)));
    }
};
Function.prototype.delay = function(){
	var __method = this;
	var args = Array.__toArray(arguments);
	var timeout = args.shift();
	var caller = args.shift();
	if(caller == null){
		caller = this;
	}
	
    return window.setTimeout(function() {
      return __method.apply(caller, args);
    }, timeout);
};

/** * @see 将json字符串转换为对象 * @param json字符串 * @return 返回object,array,string等对象 */
jQuery.extend({
	evalJSON : function(strJson) {
		return eval("(" + strJson + ")");
	}
});

/**
 * 将javascript数据类型转换为json字符串的方法。
 *
 * @public
 * @param  {object}  需转换为json字符串的对象, 一般为Json 【支持object,array,string,function,number,boolean,regexp *】
 * @return 返回json字符串
 **/
jQuery.extend({
	toJSONString : function(object) {
		var type = typeof object;
		if ('object' == type) {
			if (Array == object.constructor){
				type = 'array';
			}else if (RegExp == object.constructor){
				type = 'regexp';
			}else{
				type = 'object';
			}
		}
		switch (type) {
		case 'undefined':
		case 'unknown':
			return;
			break;
		case 'function':
		case 'boolean':
		case 'regexp':
			return object.toString();
			break;
		case 'number':
			return isFinite(object) ? object.toString() : 'null';
			break;
		case 'string':
			return '"'
					+ object.replace(/(\\|\")/g, "\\$1").replace(
							/\n|\r|\t/g,
							function() {
								var a = arguments[0];
								return (a == '\n') ? '\\n'
										: (a == '\r') ? '\\r'
												: (a == '\t') ? '\\t' : ""
							}) + '"';
			break;
		case 'object':
			if (object === null)
				return 'null';
			var results = [];
			for ( var property in object) {
				var value = jQuery.toJSONString(object[property]);
				if (value !== undefined)
					results.push(jQuery.toJSONString(property) + ':' + value);
			}
			return '{' + results.join(',') + '}';
			break;
		case 'array':
			var results = [];
			for ( var i = 0; i < object.length; i++) {
				var value = jQuery.toJSONString(object[i]);
				if (value !== undefined)
					results.push(value);
			}
			return '[' + results.join(',') + ']';
			break;
		}
	}
});