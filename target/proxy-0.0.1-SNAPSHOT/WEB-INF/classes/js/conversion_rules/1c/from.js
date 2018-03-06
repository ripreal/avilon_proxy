var functions = {
	"v8:InformationRegisterRecordSet.КадроваяИсторияСотрудников" : function(jsonObj) {
		var objectType = "v8:InformationRegisterRecordSet.КадроваяИсторияСотрудников";
		var objData = jsonObj[objectType];
		var converted = {
			"uuid" : objData["v8:Filter"]["FilterItem"]["Value"]["content"],
			"objecttype" : objectType,
			"data" : JSON.stringify(jsonObj)
		}
		
		return converted;
	},
	
	"ObjectDeletion" : function(jsonObj) {
		var objData = jsonObj["ObjectDeletion"];
		var uuid = objData["Ref"]["content"];
		CommonUtils.deleteObjects(uuid);
	}

}

function convertData(source) {
	console.log(source);
	var jsonObj = JSON.parse(source);
	for (var objectType in jsonObj) {
		
		var objData = jsonObj[objectType];
		
		var converted = {};
		
		if(objectType in functions) {
			converted = functions[objectType](jsonObj);
		}else {
			
			converted = {
				"uuid" : "v8:Ref" in objData ? objData["v8:Ref"] : null,
				"objecttype" : objectType,
				"data" : JSON.stringify(jsonObj)
			}
		}
		return !!converted ? JSON.stringify(converted) : null;
	}

}