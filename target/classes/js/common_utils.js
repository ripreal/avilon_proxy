var console = {
    log: function(obj) {
        Packages.ru.avilon.proxy.conversion.Converter.log(obj);
    }
}

var DateUtils = {
	fromXML2JSON: function(xmlDate) {
		return new Date(new Date(xmlDate).getTime() + (new Date()).getTimezoneOffset() * 60 * 1000).getTime();
	},
	
	fromJSON2XML: function(jsonDate) {
		console.log('fromJSON2XML: ' + jsonDate.toString())
		return new Date(jsonDate - (new Date()).getTimezoneOffset() * 60 * 1000).toISOString().slice(0, -5);
	}
}

var UUIDUtils = {
	currentTimeUUID : function() {
		return CommonUtils.toJSString(Packages.ru.avilon.proxy.utils.TimeUUIDUtils.getCurrentTimeUUID().toString());
	},
	
	newUUID: function () {
		return CommonUtils.toJSString(Packages.java.util.UUID.randomUUID().toString());
	},
	
	getTimeFromTimeUUID(uuid) {
		if(uuid)
			return Packages.ru.avilon.proxy.utils.TimeUUIDUtils.getTimeFromUUID(uuid);
		else
			return new Date().getTime();
	}
}

var CommonUtils = {
	toJSString: function(obj) {
		return (new String(obj)).toString();
	},
	
	getUserUUID: function() {
		try {
			return this.toJSString(currentUser);
		} catch(e) {
			return '';
		}
	},
	
	getObject: function(uuid, clientApp, type, dataOnly) {
		//console.log('getting object ' + uuid + ' type ' + type);
		var objDataStr = Packages.ru.avilon.proxy.conversion.Converter.getObject(uuid, clientApp, type, !!dataOnly);
		return objDataStr != null ? JSON.parse(objDataStr) : objDataStr; 
	},
	
	getOrCreateObject: function(uuid, clientApp, type) {
		var obj = CommonUtils.getObject(uuid, clientApp, type);
		
		if(!obj) {
			obj = {
				"uuid" : uuid,
				"objecttype": type,
				"timeuuid" : UUIDUtils.currentTimeUUID(), 
				"clientapp" : clientApp,
				"data" : "{}"
			}
		}
		return obj;
	},
	
	saveObject: function(object) {
		Packages.ru.avilon.proxy.conversion.Converter.saveObject(JSON.stringify(object));
	},
	
	deleteObjects: function(uuid) {
		Packages.ru.avilon.proxy.conversion.Converter.deleteObject(uuid, null, null);
	},
	
	deleteObject: function(uuid, client_app) {
		Packages.ru.avilon.proxy.conversion.Converter.deleteObject(uuid, client_app, null);
	}
}

var UserManagememt = {
		createUser: function(login, password, roles, json_object_uuid) {
			Packages.ru.avilon.proxy.conversion.Converter.createUser(login, password, roles, json_object_uuid);
		},
		deleteUser: function(login) {
			Packages.ru.avilon.proxy.conversion.Converter.deleteUser(login);
		},
		getUserByUUID : function(json_obj_uuid) {
			var userDataStr = Packages.ru.avilon.proxy.conversion.Converter.getUserByUUID(json_obj_uuid);
			return userDataStr != null ? JSON.parse(userDataStr) : null; 
		}
}

