var functions = {
	"task_result": function(jsonObj, jsonData) {
		
		jsonData.uuid = ("uuid" in jsonObj && !!jsonData.uuid) ? jsonData.uuid : UUIDUtils.newUUID();
		
		var objTask = CommonUtils.getOrCreateObject(jsonData.uuid, "1c", "v8:DocumentObject.ЗаказНарядНаРаботы");
		
		var parentTask1c = CommonUtils.getObject(jsonData.taskUUID,  "1c", "v8:DocumentObject.ЗаказНарядНаРаботы", true);
		if(!parentTask1c) {
			var err = 'cant find parent task for ' +JSON.stringify(jsonData);
			console.log(err);
			throw err;
		}
		
		var parentTask1cData = parentTask1c["v8:DocumentObject.ЗаказНарядНаРаботы"];
		
		if(!jsonData.timeUUID) {
			jsonData.timeUUID = UUIDUtils.currentTimeUUID();
		}
		
		var objTaskData = {
			"v8:DocumentObject.ЗаказНарядНаРаботы" : {
				"v8:Ref" : (("uuid" in jsonObj) ? jsonData.uuid : UUIDUtils.newUUID()),
				"v8:DeletionMark" : false,
				"v8:Date" : DateUtils.fromJSON2XML(UUIDUtils.getTimeFromTimeUUID(jsonData.timeUUID)),
				"v8:Number": "",
				"v8:Posted": false,
				"v8:Организация" : parentTask1cData["v8:Организация"],
				"v8:Подразделение" : parentTask1cData["v8:Подразделение"],
				"v8:Теплица" : parentTask1cData["v8:Теплица"],
				"v8:ПосевнаяПлощадь" : parentTask1cData["v8:ПосевнаяПлощадь"],
				"v8:Комментарий": "",
				"v8:ВидРабот" : parentTask1cData["v8:ВидРабот"],
				"v8:Инициатор" : parentTask1cData["v8:Инициатор"],
				"v8:Исполнитель" : parentTask1cData["v8:Исполнитель"],
				"v8:ТехнологическаяКарта" : parentTask1cData["v8:ТехнологическаяКарта"],
				"v8:ДатаНачала" : !!jsonData.start.date ? DateUtils.fromJSON2XML(jsonData.start.date) : "0001-01-01T00:00:00",
				"v8:ДатаВыполнения" : !!jsonData.finish.date ?  DateUtils.fromJSON2XML(jsonData.finish.date) : "0001-01-01T00:00:00",
				"v8:Факт": true
				//"v8:ДополнительныеРеквизиты": ""
				//"v8:ПараметрыВыполненияРабот" : parentTask1cData["v8:ПараметрыВыполненияРабот"]
			}
		}
		
		objTaskData["v8:DocumentObject.ЗаказНарядНаРаботы"]["v8:Исполнитель"]["content"] = jsonData.start.userUUID;
		objTaskData["v8:DocumentObject.ЗаказНарядНаРаботы"]["v8:Исполнитель"]["xsi:type"] ="v8:CatalogRef.Сотрудники";
		
		var workParameters = [];
		
		jsonData.start.properties.forEach(function(elem) {
			addToWorkParameters(elem, false);
		});
		
		jsonData.finish.properties.forEach(function(elem) {
			addToWorkParameters(elem, true);
		});
		
		function addToWorkParameters(param, isFinish) {
			var workParameter = {
				"v8:Параметр":param.uuid,
				"v8:РегулярноеВыражение": !!param.regexp ? param.regexp : "",
				"v8:РезультатВыполнения": !!param.value.value ? param.value.value : "",
				"v8:Обязательный": param.required,
				"v8:ПриЗавершении": isFinish,
				"v8:Комментарий": ""
			}
			workParameters.push(workParameter);
		}
		
		objTaskData["v8:DocumentObject.ЗаказНарядНаРаботы"]["v8:ПараметрыВыполненияРабот"] = workParameters;
		
		objTask.data = JSON.stringify(objTaskData);
		objTask.changedByProxy = true;
		objTask.timeUUID = UUIDUtils.currentTimeUUID();
		return objTask;
	}
}


function convertData(source) {
	console.log(source);
	var jsonObj = JSON.parse(source);
	var objectType = jsonObj.objecttype;
	if(objectType in functions) {
		var converted = functions[objectType](jsonObj, JSON.parse(jsonObj.data));
		console.log(JSON.stringify(converted));
		return JSON.stringify(converted);
	}else {
		return null;
	}
}