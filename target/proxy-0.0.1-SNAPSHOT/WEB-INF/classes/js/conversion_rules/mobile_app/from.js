var functions = {
	"user" : function(jsonObj) {
		var converted = {
			"name" :  jsonObj.name,
			"department" :  jsonObj.department,
			"tabnum" :  jsonObj.tabnum,
			"position" :  jsonObj.position
		}
		return converted;
	},
	
	"task" : function(jsonObj) {
		var converted = {
			"name" :  jsonObj.name,
			"order" :  jsonObj.order,
			"iconEncoded" :  jsonObj.iconEncoded,
			"parentTaskGroupUUID" : jsonObj.parentTaskGroupUUID,
			"requireFinish" : jsonObj.requireFinish,
			"requireOnlineStart" : jsonObj.requireOnlineStart,
			"description" : jsonObj.description,
			"personal_task" : jsonObj.personal_task,
			"start" : jsonObj.start,
			"finish" : jsonObj.finish
		}
		return converted;
	},
	
	"task_result" : function(jsonObj) {
		var converted = {
				"name" :  jsonObj.name,
				"order" :  jsonObj.order,
				"iconEncoded" :  jsonObj.iconEncoded,
				"parentTaskGroupUUID" : jsonObj.parentTaskGroupUUID,
				"requireFinish" : jsonObj.requireFinish,
				"requireOnlineStart" : jsonObj.requireOnlineStart,
				"description" : jsonObj.description,
				"personal_task" : jsonObj.personal_task,
				"start" : jsonObj.start,
				"finish" : jsonObj.finish,
				"taskUUID" : jsonObj.taskUUID
				
			}
			return converted;
	},


	"geo" : function(jsonObj) {
		var converted = {
			"longitude" :  jsonObj.longitude,
			"latitude" :  jsonObj.latitude,
			"userUUID" : (("userUUID" in jsonObj && (!!jsonObj.userUUID)) ? jsonObj.userUUID : CommonUtils.getUserUUID())
		}
		return converted;
	},
	
	"log" : function(jsonObj) {
		var converted = {
			"message" :  jsonObj.message
		}
		return converted;
	},
	
	"message" : function(source) {
		var converted = {
			"recipientUUIDS" :  jsonObj.recipientUUIDS,
			"message" :  jsonObj.message
		}
		return converted;
	},
	
	"task_group" : function(jsonObj) {
		var converted = {
			"name" :  jsonObj.name,
			"parentTaskGroupUUID" :  jsonObj.parentTaskGroupUUID
		}
		return converted;
	}
}


function convertData(source) {
	console.log(source);
	var jsonObj = JSON.parse(source);
	var objectType = jsonObj.objectType;
	if(objectType in functions) {
		var data = functions[objectType](jsonObj);
		data.uuid = (("uuid" in jsonObj) ? jsonObj.uuid : UUIDUtils.newUUID());
		data.timeUUID = (("timeUUID" in jsonObj) ? jsonObj.timeUUID : UUIDUtils.currentTimeUUID());
		data.objectType = objectType;
		var converted = {
			"uuid" : data.uuid,
			"clientapp" : "mobile_app",
			"timeUUID" : data.timeUUID,
			"objecttype" : data.objectType,
			"changedByProxy" : true,
			"data" : JSON.stringify(data)
		}
		return JSON.stringify(converted);
	}else {
		return source;
	}
}