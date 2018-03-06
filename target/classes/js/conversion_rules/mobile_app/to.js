var functions = {
	
	"v8:InformationRegisterRecordSet.КадроваяИсторияСотрудников" : function(jsonObj, jsonData) {
		var objData = jsonData["v8:InformationRegisterRecordSet.КадроваяИсторияСотрудников"]["v8:Record"];
		if(!objData)
			return null;
		
		if(Array.isArray(objData)) {
			objData.forEach(function(elem) {
				saveUser(elem);
			});
		} else {
			saveUser(objData);
		}
		
		function saveUser(objData) {
			
			console.log('processing ' + JSON.stringify(objData));
			
			var objUser = null;
			
			if(objData["v8:ВидСобытия"] == "Увольнение") {
				objUser = CommonUtils.getObject(objData["v8:Сотрудник"], "mobile_app", "user");
				if(objUser == null)
					return;
				
			} else {
				objUser = CommonUtils.getOrCreateObject(objData["v8:Сотрудник"], "mobile_app", "user");
			}
			
			var userData = JSON.parse(objUser.data);
			
			var position = CommonUtils.getObject(objData["v8:Должность"],  "1c", "v8:CatalogObject.Должности", true);
			if(position) {
				userData.position = position["v8:CatalogObject.Должности"]["v8:Description"];
				userData.positionUUID = objData["v8:Должность"];
			}
			
			var department = CommonUtils.getObject(objData["v8:Подразделение"], "1c", "v8:CatalogObject.ПодразделенияОрганизаций", true);
		
			if(department) {
				userData.department = department["v8:CatalogObject.ПодразделенияОрганизаций"]["v8:Description"];
				userData.departmentUUID = objData["v8:Подразделение"];
				var parentDepartment = CommonUtils.getObject(department["v8:CatalogObject.ПодразделенияОрганизаций"]["v8:Parent"], "1c", "v8:CatalogObject.ПодразделенияОрганизаций", true);
				if(parentDepartment)
					userData.department =  userData.department + "/" + parentDepartment["v8:CatalogObject.ПодразделенияОрганизаций"]["v8:Description"];
			}
			
			if(!userData.position)
				userData.position = "";
			if(!userData.positionUUID)
				userData.positionUUID = "";
			if(!userData.department)
				userData.department = "";
			if(!userData.departmentUUID)
				userData.departmentUUID = "";
			
			objUser.data = JSON.stringify(userData);
			objUser.changedByProxy = true;
			CommonUtils.saveObject(objUser);
		}
		
		return null;
		
	},
	
	"v8:CatalogObject.Сотрудники" : function(jsonObj, jsonData) {
		var objData = jsonData["v8:CatalogObject.Сотрудники"];
		
		var objUser = CommonUtils.getOrCreateObject(objData["v8:Ref"], "mobile_app", "user");
		
		var userData = JSON.parse(objUser.data);
		
		userData.name = objData["v8:Description"];
		//userData.tabnum = objData["v8:Code"];
		userData.uuid = objData["v8:Ref"];
		userData.timeuuid = objUser.timeuuid;
		userData.objectType = "user";
		

		
		var additionalReqs = objData["v8:ДополнительныеРеквизиты"];
		if(additionalReqs) {
			if(additionalReqs["v8:Свойство"] == "e4c03aaf-0ddf-11e6-942a-005056ac0e86")
				userData.tabnum = additionalReqs["v8:Значение"]["content"];
		}
		console.log('user data: ' + JSON.stringify(userData))
		if(userData.tabnum) {
			if(objData["v8:DeletionMark"]) {
				UserManagememt.deleteUser(userData.tabnum);
			} else if(!objData["v8:ВАрхиве"]) {
				UserManagememt.createUser(userData.tabnum, '', ['user'], objData["v8:Ref"]);
			}
		}
		
		if(objData["v8:ВАрхиве"]) {
			var sysUser = UserManagememt.getUserByUUID(userData.uuid);
			if(!!sysUser)
				UserManagememt.createUser(sysUser.name, sysUser.password, [], userData.uuid);
		}
		
		if(!userData.position)
			userData.position = "";
		if(!userData.positionUUID)
			userData.positionUUID = "";
		if(!userData.department)
			userData.department = "";
		if(!userData.departmentUUID)
			userData.departmentUUID = "";
		
		objUser.data = JSON.stringify(userData);
		objUser.changedByProxy = true;
		return objUser;
	},
	
	"v8:DocumentObject.ЗаказНарядНаРаботы" : function(jsonObj, jsonData) {
		var objData = jsonData["v8:DocumentObject.ЗаказНарядНаРаботы"];
		
//		console.log('1c task: ' + JSON.stringify(objData));
		
		if(objData["v8:DeletionMark"]) {
			CommonUtils.deleteObject(objData["v8:Ref"], "mobile_app");
			return null;
		}
			
		
		var objTask = CommonUtils.getOrCreateObject(objData["v8:Ref"], "mobile_app", "task");
		
		var taskData = JSON.parse(objTask.data);
		
		var workType = CommonUtils.getObject(objData["v8:ВидРабот"], "1c", "v8:CatalogObject.ВидыРаботСотрудников", true);
		
		taskData.parentTaskGroupUUID = null;
		
		if(workType) {
			taskData.name = workType["v8:CatalogObject.ВидыРаботСотрудников"]["v8:Description"];
			//console.log('1c work type: ' + JSON.stringify(workType));
			var parentWorkType = CommonUtils.getObject(workType["v8:CatalogObject.ВидыРаботСотрудников"]["v8:Parent"], "1c", "v8:CatalogObject.ВидыРаботСотрудников", true);
			if(parentWorkType) {
				//console.log('1c parent work type: ' + JSON.stringify(workType));
				var objTaskGroup = CommonUtils.getOrCreateObject(parentWorkType["v8:CatalogObject.ВидыРаботСотрудников"]["v8:Ref"], "mobile_app", "task_group");
				var taskGroupData = JSON.parse(objTaskGroup.data);
				
				taskGroupData.objectType = "task_group";
				taskGroupData.uuid = objTaskGroup.uuid;
				taskGroupData.timeuuid = objTaskGroup.timeuuid;
				
				taskGroupData.name = parentWorkType["v8:CatalogObject.ВидыРаботСотрудников"]["v8:Description"];
				
				objTaskGroup.data = JSON.stringify(taskGroupData);
				objTaskGroup.changedByProxy = true;
				CommonUtils.saveObject(objTaskGroup);
				
				taskData.parentTaskGroupUUID = taskGroupData.uuid;
			}
			
			//console.log('1c work type: ' +  JSON.stringify(workType["v8:CatalogObject.ВидыРаботСотрудников"]));
		}
		
		taskData.description = objData["v8:Комментарий"];

		taskData.start = {"properties": []};
		taskData.start.date =  DateUtils.fromXML2JSON(objData["v8:ДатаНачала"]);
		taskData.start.userUUID = objData["v8:Исполнитель"]["content"];
		taskData.start.departmentUUID = objData["v8:Подразделение"];
		
		taskData.finish = {"properties": []};
		//taskData.finish.date =  DateUtils.fromXML2JSON(objData["v8:ДатаВыполнения"]);
		
		var workParameters = objData["v8:ПараметрыВыполненияРабот"];
		
		if(!!workParameters) {
			if(Array.isArray(workParameters)) {
				workParameters.forEach(function(workParameter) {
					covnertWorkParameter(workParameter);
				});
			} else {
				covnertWorkParameter(workParameters);
			}
		}
		
		function covnertWorkParameter(workParameter) {
			//helper functions
			function getValueType(val_type_1c) {
				var valTypes = {
						"xs:decimal": "number",
						"xs:string" : "string",
						"xs:boolean" : "boolean",
						"xs:dateTime" : "date",
						"v8:CatalogRef.Тележки" : "number" 
				}
				
				return valTypes[val_type_1c];
			}
			
			function getRegexpString(workParameterObjectData) {
				var valType = workParameterObjectData["v8:ValueType"];
				if(valType["Type"]["content"] == "xs:decimal") {
					var digits = valType["NumberQualifiers"]["Digits"];
					return "\\d{0," + digits + "}";
				}
				return null;
			}
			
			if(!workParameter["v8:Параметр"]) {
				return;
			}
			
			var workParameterObject =  CommonUtils.getObject(workParameter["v8:Параметр"], "1c", "v8:ChartOfCharacteristicTypesObject.ПараметрыВыполненияРабот", true);
			if(!workParameterObject || workParameterObject["v8:ChartOfCharacteristicTypesObject.ПараметрыВыполненияРабот"]["v8:DeletionMark"]) {
				return;
			}
			var workParameterObjectData = workParameterObject["v8:ChartOfCharacteristicTypesObject.ПараметрыВыполненияРабот"];
			
			var taskProperty = {
				"uuid" : workParameter["v8:Параметр"],
				"name" : workParameterObjectData["v8:Description"],
				"description" : workParameterObjectData["v8:Комментарий"],
				"regexp" : !!workParameter["v8:РегулярноеВыражение"] ? workParameter["v8:РегулярноеВыражение"] : getRegexpString(workParameterObjectData),
				"value" : {
					"valueType" : workParameterObjectData["v8:Description"] == "Фото" ? "binary" : getValueType(workParameterObjectData["v8:ValueType"]["Type"]["content"]),
					"value" : workParameterObjectData["v8:РезультатВыполнения"]
				},
				"required" : workParameter["v8:Обязательный"]
			}
			
			if(workParameter["v8:ПриЗавершении"]) {
				taskData.finish.properties.push(taskProperty);
			} else {
				taskData.start.properties.push(taskProperty);
			}
		}
		
		taskData.requireOnlineStart = false;
		taskData.order = 0;
		taskData.requireFinish =true;

		taskData.uuid = objTask.uuid;
		taskData.timeUUID = objTask.timeUUID;
		taskData.objectType = "task";
		
		taskData.personal_task = objData["v8:Исполнитель"]["xsi:type"] == "v8:CatalogRef.Сотрудники";
		//console.log('task_data: ' + JSON.stringify(taskData));
		objTask.data = JSON.stringify(taskData);
		objTask.changedByProxy = true;
		return objTask;
	
	}
}


function convertData(source) {
	console.log(source);
	var jsonObj = JSON.parse(source);
	var objectType = jsonObj.objecttype;
	if(objectType in functions) {
		var converted = functions[objectType](jsonObj, JSON.parse(jsonObj.data));
		return !!converted ? JSON.stringify(converted) : null;
	}else {
		return null;
	}
}