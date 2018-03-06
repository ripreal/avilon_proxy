var mobile_app_user = null;

var functions = {
	"task" : function (jsonObj) {
		var taskData = JSON.parse(jsonObj.data);
		var currentTime = new Date().getTime();

		if (taskData.personal_task) {
			//filter by user
			if (taskData.start.userUUID != currentUserUUID)
				return null;				
		} else {
			//cache user
			if (!mobile_app_user)
				mobile_app_user = CommonUtils.getObject(currentUserUUID, "mobile_app", "user", true);
			//filter by position
			if (taskData.start.userUUID != mobile_app_user.positionUUID || taskData.start.departmentUUID != mobile_app_user.departmentUUID)
				return null;		
				
			if (!!taskData.start.date && taskData.start.date > currentTime)
				return null;		
			if (!!taskData.finish.date && (taskData.finish.date >= 0 && taskData.finish.date < currentTime))
				return null;
		}
			
		return jsonObj;
	},
	
	"user" : function (jsonObj) {
		var userData = JSON.parse(jsonObj.data);
		if(!userData.tabnum && !userData.name)
			return null;
		return jsonObj;
	}
}

function filter(object) {
	var jsonObj = JSON.parse(object);
	if (jsonObj.objecttype in functions) {
		var objResult = functions[jsonObj.objecttype](jsonObj);
		return (!!objResult) ? JSON.stringify(objResult) : null;
	}
	console.log('filtering ' + object);
	return object;
}