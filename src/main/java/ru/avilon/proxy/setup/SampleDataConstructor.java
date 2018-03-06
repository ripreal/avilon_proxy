package ru.avilon.proxy.setup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import ru.avilon.proxy.entities.ProxyObject;
import ru.avilon.proxy.entities.properties.AdvancedProperty;
import ru.avilon.proxy.entities.properties.BarcodeNumberValue;
import ru.avilon.proxy.entities.properties.BinaryValue;
import ru.avilon.proxy.entities.properties.BooleanValue;
import ru.avilon.proxy.entities.properties.DateValue;
import ru.avilon.proxy.entities.properties.NumberValue;
import ru.avilon.proxy.entities.properties.StringFromListValue;
import ru.avilon.proxy.entities.properties.StringQRValue;
import ru.avilon.proxy.entities.properties.StringValue;
import ru.avilon.proxy.repo.MetadataRepository;
import ru.avilon.proxy.utils.TimeUUIDUtils;

public class SampleDataConstructor {
 
	private static final String COLOR_WHITE = "#FFFFFF";
	private static final String COLOR_WHITE_GREEN = "#ECF1DF";
	private static final String COLOR_WHITE_YELLOW = "#F3F2A2";
	private static final String COLOR_RED = "#FF0000";
	

	List<ProxyObject> objects = new ArrayList<ProxyObject>();
	
	MetadataRepository metadataRepository;
	
	@Inject 
	public SampleDataConstructor(MetadataRepository metadataRepository) throws Exception {
		this.metadataRepository = metadataRepository;
		constructSampleData();
	}
	
	@SuppressWarnings("unchecked")
	private void constructSampleData() throws Exception {
		ProxyObject user = metadataRepository.constructNewProxyObject("user");
		user.setProperty("name", "Иванов");
		user.setProperty("tabnum", "11111");
		addNewObject(user);
		
		ProxyObject task_group_meva = metadataRepository.constructNewProxyObject("task_group");
		task_group_meva.setProperty("name", "Мева");
		addNewObject(task_group_meva);
		
		ProxyObject task_group_gunnar = metadataRepository.constructNewProxyObject("task_group");
		task_group_gunnar.setProperty("name", "Гуннар");
		addNewObject(task_group_gunnar);
		
		ProxyObject task_group_sv = metadataRepository.constructNewProxyObject("task_group");
		task_group_sv.setProperty("name", "Святогор");
		addNewObject(task_group_sv);
		
		ProxyObject task_group_vr_tom = metadataRepository.constructNewProxyObject("task_group");
		task_group_vr_tom.setProperty("name", "Вредители томатные");
		addNewObject(task_group_vr_tom);
		
		ProxyObject task_group_nsp = metadataRepository.constructNewProxyObject("task_group");
		task_group_nsp.setProperty("name", "Неисправности");
		addNewObject(task_group_nsp);
		
		ProxyObject task_group_pers = metadataRepository.constructNewProxyObject("task_group");
		task_group_pers.setProperty("name", "Персональные");
		addNewObject(task_group_pers);
		
		AdvancedProperty rowProp = new AdvancedProperty();
		rowProp.description = "Введите номер ряда";
		rowProp.name = "Ряд";
		rowProp.value = new NumberValue();
		rowProp.regexp = "[1-9]|[1-9][0-9]|(1|2|3|4|5)[0-9][0-9]|5[0-1][0-9]|520";
		
		AdvancedProperty rowProp2 = new AdvancedProperty();
		rowProp2.description = "Введите номер ряда";
		rowProp2.name = "Ряд";
		rowProp2.value = new NumberValue();
		rowProp2.regexp = "[1-9]|[1-9][0-9]";

		
		AdvancedProperty placeProp = new AdvancedProperty();
		placeProp.description = "Введите номер столба";
		placeProp.name = "Место";
		placeProp.value = new NumberValue();
		placeProp.regexp = "1|2|3|4|5|6|7|8|9|10|11|12";
		
		AdvancedProperty photoProp = new AdvancedProperty();
		photoProp.description = "Сфотографируйте растение";
		photoProp.name = "Фотография";
		photoProp.value = new BinaryValue();
		
		AdvancedProperty doneProp = new AdvancedProperty();
		doneProp.description = "Исправлено да/нет";
		doneProp.name = "Выполнено";
		doneProp.value = new BooleanValue();
		
		
		AdvancedProperty rowPropBC = new AdvancedProperty();
		rowPropBC.description = "Введите номер ряда";
		rowPropBC.name = "Ряд";
		rowPropBC.value = new BarcodeNumberValue();
		rowPropBC.regexp = "[1-9]|[1-9][0-9]|(1|2|3|4|5)[0-9][0-9]|5[0-1][0-9]|520";
		
		AdvancedProperty boxPropBC = new AdvancedProperty();
		boxPropBC.name = "Ящик";
		boxPropBC.value = new BarcodeNumberValue();
		
		//task

		List<AdvancedProperty> props = new ArrayList<>();
		props.add(rowProp);
		addTask("Завоз и укладка матов на лотки", 
				"Выберите номер ряда для начала работы и нажмите начать", 
				COLOR_WHITE, false, true, props, props, user, task_group_meva);
		
		props = new ArrayList<>();
		props.add(rowProp);
		addTask("Установка капиляров в маты", 
				"По 5 капилляров на мат", 
				COLOR_WHITE, false, true, props, props, user, task_group_meva);
		
		
		List<AdvancedProperty> propsStart = new ArrayList<>();
		propsStart.add(rowProp);
		propsStart.add(placeProp);
		
		List<AdvancedProperty> propsFinish = new ArrayList<>();
		propsFinish.add(placeProp);
		addTask("Запитка матов", 
				"Выберите ряд от 1 до 5 и столб от 1 до 12", 
				COLOR_WHITE, false, true, propsStart, propsFinish, user, task_group_meva);
		
		propsStart = new ArrayList<>();
		propsStart.add(rowProp);
		
		propsFinish = new ArrayList<>();
		addTask("Завоз рассады в теплицу", 
				"Не более 5 матов на тележку", 
				COLOR_WHITE, false, true, propsStart, propsFinish, user, task_group_meva);
		
		propsStart = new ArrayList<>();
		propsStart.add(rowProp);
		
		propsFinish = new ArrayList<>();
		addTask("Установка рассады на мат", 
				"Выберите номер ряда для начала работы и нажмите начать", 
				COLOR_WHITE, false, true, propsStart, propsFinish, user, task_group_meva);
		
		
		
		propsStart = new ArrayList<>();
		propsStart.add(rowProp);
		propsStart.add(placeProp);
		
		propsFinish = new ArrayList<>();
		propsFinish.add(placeProp);;
		addTask("Посадка рассады в мат", 
				"Выберите номер ряда для начала работы и нажмите начать", 
				COLOR_WHITE, false, true, propsStart, propsFinish, user, task_group_meva);
		
		
		propsStart = new ArrayList<>();
		propsStart.add(rowProp);
		
		propsFinish = new ArrayList<>();
		addTask("Разрезание дренажных отверстий", 
				null, 
				COLOR_WHITE, false, true, propsStart, propsFinish, user, task_group_meva);
		
		propsStart = new ArrayList<>();
		propsStart.add(rowProp);
		
		propsFinish = new ArrayList<>();
		addTask("Установка крючков на шпалере", 
				null, 
				COLOR_WHITE, false, true, propsStart, propsFinish, user, task_group_meva);
		
		propsStart = new ArrayList<>();
		propsStart.add(rowProp);
		
		propsFinish = new ArrayList<>();
		addTask("Подвязка растений на шпагат", 
				null, 
				COLOR_WHITE, false, true, propsStart, propsFinish, user, task_group_meva);
		
		
		propsStart = new ArrayList<>();
		propsStart.add(rowProp2);
		
		propsFinish = new ArrayList<>();
		addTask("Верхний уход за  растениями", 
				"Подвяжите побеги и установите крепежи", 
				COLOR_WHITE_GREEN, false, true, propsStart, propsFinish, user, task_group_gunnar);
		
		propsStart = new ArrayList<>();
		propsStart.add(rowProp2);
		
		propsFinish = new ArrayList<>();
		addTask("Удаление листа", 
				"Выберите номер ряда для начала работы и нажмите начать", 
				COLOR_WHITE_GREEN, false, true, propsStart, propsFinish, user, task_group_gunnar);
		
		propsStart = new ArrayList<>();
		propsStart.add(rowProp2);
		
		propsFinish = new ArrayList<>();
		addTask("Вынос листа", 
				null, 
				COLOR_WHITE_GREEN, false, true, propsStart, propsFinish, user, task_group_gunnar);
		
		
		propsStart = new ArrayList<>();
		propsStart.add(rowProp2);
		propsStart.add(boxPropBC);
		
		propsFinish = new ArrayList<>();
		addTask("Сбор продукции без сортировки", 
				"Отсканируйте ящик для сбора продукции и укажите номер ряда с которого производится сбор", 
				COLOR_WHITE_GREEN, false, true, propsStart, propsFinish, user, task_group_gunnar);
		
		propsStart = new ArrayList<>();
		propsStart.add(rowProp2);
		
		propsFinish = new ArrayList<>();
		addTask("Уход за стеблями", 
				null, 
				COLOR_WHITE_GREEN, false, true, propsStart, propsFinish, user, task_group_gunnar);
		
		propsStart = new ArrayList<>();
		propsStart.add(rowProp);
		
		propsFinish = new ArrayList<>();
		addTask("Перекидка растений на другую шпалеру", 
				null, 
				COLOR_WHITE_YELLOW, false, true, propsStart, propsFinish, user, task_group_sv);
		
		propsStart = new ArrayList<>();
		propsStart.add(rowProp);
		
		propsFinish = new ArrayList<>();
		addTask("Срез растений с кубика", 
				null, 
				COLOR_WHITE_YELLOW, false, true, propsStart, propsFinish, user, task_group_sv);
		
		propsStart = new ArrayList<>();
		propsStart.add(rowProp);
		propsFinish = new ArrayList<>();
		addTask("Снятие крабов в мешок", 
				null, 
				COLOR_WHITE_YELLOW, false, true, propsStart, propsFinish, user, task_group_sv);
		
		propsStart = new ArrayList<>();
		propsStart.add(rowProp);
		propsFinish = new ArrayList<>();
		addTask("Срез растений у трубы роста и вынос на дорожку", 
				null, 
				COLOR_WHITE_YELLOW, false, true, propsStart, propsFinish, user, task_group_sv);
		
		propsStart = new ArrayList<>();
		propsStart.add(rowProp);
		propsFinish = new ArrayList<>();
		addTask("Прочистка лотков от зелени", 
				null, 
				COLOR_WHITE_YELLOW, false, true, propsStart, propsFinish, user, task_group_sv);
		
		propsStart = new ArrayList<>();
		propsStart.add(rowProp);
		propsFinish = new ArrayList<>();
		addTask("Подметание пленки от растительных остатков", 
				null, 
				COLOR_WHITE_YELLOW, false, true, propsStart, propsFinish, user, task_group_sv);
		
		propsStart = new ArrayList<>();
		propsStart.add(rowProp2);
		propsStart.add(placeProp);
		propsStart.add(photoProp);
		propsFinish = new ArrayList<>();
		addTask("Серая Гниль", 
				null, 
				null, false, false, propsStart, propsFinish, user, task_group_vr_tom);
		
		
		propsStart = new ArrayList<>();
		propsStart.add(rowProp2);
		propsStart.add(placeProp);
		propsStart.add(photoProp);
		propsFinish = new ArrayList<>();
		addTask("Фитофтороз", 
				null, 
				null, false, false, propsStart, propsFinish, user, task_group_vr_tom);
		
		propsStart = new ArrayList<>();
		propsStart.add(rowProp);
		propsStart.add(placeProp);
		propsStart.add(photoProp);
		propsFinish = new ArrayList<>();
		addTask("Тля", 
				null, 
				null, false, false, propsStart, propsFinish, user, task_group_vr_tom);
		
		propsStart = new ArrayList<>();
		propsStart.add(rowProp);
		propsStart.add(placeProp);
		propsStart.add(photoProp);
		propsFinish = new ArrayList<>();
		addTask("Белокрылка", 
				null, 
				null, false, false, propsStart, propsFinish, user, task_group_vr_tom);
		
		
		propsStart = new ArrayList<>();
		propsStart.add(rowPropBC);
		propsStart.add(placeProp);
		propsFinish = new ArrayList<>();
		addTask("Не работает подача воды", 
				null, 
				null, false, false, propsStart, propsFinish, user, task_group_nsp);
		
		propsStart = new ArrayList<>();
		propsStart.add(rowProp);
		propsStart.add(placeProp);
		propsFinish = new ArrayList<>();
		addTask("Перегорела лампа", 
				null, 
				null, false, false, propsStart, propsFinish, user, task_group_nsp);
		
		
		propsStart = new ArrayList<>();
		propsStart.add(rowProp);
		propsStart.add(placeProp);
		propsFinish = new ArrayList<>();
		addTask("Сломан столб", 
				null, 
				null, false, false, propsStart, propsFinish, user, task_group_nsp);
		
		propsStart = new ArrayList<>();
		propsStart.add(rowProp);
		propsStart.add(placeProp);
		propsFinish = new ArrayList<>();
		addTask("Сломан Капилляр", 
				null, 
				null, false, false, propsStart, propsFinish, user, task_group_nsp);
		
		propsStart = new ArrayList<>();
		propsStart.add(rowProp);
		propsStart.add(placeProp);
		propsFinish = new ArrayList<>();
		addTask("Не открывается форточка", 
				null, 
				null, false, false, propsStart, propsFinish, user, task_group_nsp);
		
		propsStart = new ArrayList<>();
		propsStart.add(doneProp);
		propsFinish = new ArrayList<>();
		addTask("Неправильная установка капилляра, Исправить", 
				"В ряду 1 неправильная установка капилляра", 
				COLOR_RED, true, false, propsStart, propsFinish, user, task_group_pers);
		
		
		
		//test task
		propsStart = new ArrayList<>();
		
		AdvancedProperty testProp1 = new AdvancedProperty();
		testProp1.description = "test num prop";
		testProp1.name = "num";
		NumberValue value = new NumberValue();
		value.setValue(123123123);
		testProp1.value = value;
		propsStart.add(testProp1);
		
		testProp1 = new AdvancedProperty();
		testProp1.description = "test barcode prop";
		testProp1.name = "barcode";
		BarcodeNumberValue barcodeNumberValue = new BarcodeNumberValue();
		barcodeNumberValue.setValue(1312312312123123l);
		testProp1.value = barcodeNumberValue;
		propsStart.add(testProp1);
		
		testProp1 = new AdvancedProperty();
		testProp1.description = "test binary prop";
		testProp1.name = "binary";
		BinaryValue binaryValue = new BinaryValue();
		binaryValue.setValue("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");
		testProp1.value = binaryValue;
		propsStart.add(testProp1);
		
		testProp1 = new AdvancedProperty();
		testProp1.description = "test boolean prop";
		testProp1.name = "boolean";
		BooleanValue booleanValue = new BooleanValue();
		booleanValue.setValue(true);
		testProp1.value = booleanValue;
		propsStart.add(testProp1);
		
		testProp1 = new AdvancedProperty();
		testProp1.description = "test date prop";
		testProp1.name = "date";
		DateValue dateValue = new DateValue();
		dateValue.setValue(System.currentTimeMillis());
		testProp1.value = dateValue;
		propsStart.add(testProp1);
		
		testProp1 = new AdvancedProperty();
		testProp1.description = "test list prop";
		testProp1.name = "list";
		StringFromListValue stringFromListValue = new StringFromListValue();
		stringFromListValue.avialableValues.put("test_val_1", "test value 1");
		stringFromListValue.avialableValues.put("test_val_2", "test value 2");
		stringFromListValue.setValue("test_val_1");
		testProp1.value = stringFromListValue;
		propsStart.add(testProp1);
		
		testProp1 = new AdvancedProperty();
		testProp1.description = "test QR prop";
		testProp1.name = "QR";
		StringQRValue stringQRValue = new StringQRValue();
		stringQRValue.setValue("test qr value");
		testProp1.value = stringQRValue;
		propsStart.add(testProp1);
		
		testProp1 = new AdvancedProperty();
		testProp1.description = "test QR prop";
		testProp1.name = "QR";
		StringValue stringValue = new StringValue();
		stringValue.setValue("test string value");
		testProp1.value = stringValue;
		propsStart.add(testProp1);
		
		
		propsFinish = new ArrayList<>();
		propsFinish.addAll(propsStart);
		ProxyObject task = addTask("Тестовая задача 1", 
				"Описание тестовой задачи", 
				COLOR_RED, true, false, propsStart, propsFinish, user, task_group_pers);
		
		task.setProperty("order", 1);
		task.setProperty("iconEncoded", "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");
		task.setProperty("personal_task", true);
		
		Map<String, Object> start = (Map<String, Object>) task.getProperties().get("start");
		start.put("date", System.currentTimeMillis());
		start.put("userUUID", user.getUuid());
		
		Map<String, Object> finish = (Map<String, Object>) task.getProperties().get("finish");
		finish.put("date", System.currentTimeMillis());
		finish.put("userUUID", user.getUuid());


	}
	
	
	@SuppressWarnings("unchecked")
	private ProxyObject addTask(String name, 
			String description, 
			String color, 
			Boolean requireOnlineStart, 
			Boolean requireFinish, 
			List<AdvancedProperty> startProps, List<AdvancedProperty> finishProps, 
			ProxyObject user, ProxyObject parentTaskGroup) throws Exception {
		ProxyObject task = metadataRepository.constructNewProxyObject("task");
		task.setProperty("name", name);
		task.setProperty("description", StringUtils.defaultString(description));
		task.setProperty("color", color);
		task.setProperty("requireOnlineStart", requireOnlineStart);
		task.setProperty("requireFinish", requireFinish);
		task.setProperty("parentTaskGroupUUID", parentTaskGroup.getUuid());
		
		
		Map<String, Object> start = (Map<String, Object>) task.getProperties().get("start");
		start.put("userUUID",  user.getUuid());
		
		start.put("properties",  startProps);
		
		Map<String, Object> finish = (Map<String, Object>) task.getProperties().get("finish");
		finish.put("properties",  finishProps);
		
		task.setProperty("start", start);
		task.setProperty("finish", finish);
		
		addNewObject(task);
		
		return task;
	}
	
	private void addNewObject(ProxyObject obj) {
		obj.setTimeUUID(TimeUUIDUtils.getTimeUUID(System.currentTimeMillis()));
		obj.setUuid(UUID.randomUUID());
		obj.setClientapp("mobile_app");
		try {
			obj.setProperty("uuid", obj.getUuid().toString());
			obj.setProperty("timeUUID", obj.getTimeUUID().toString());
			obj.setProperty("objectType", obj.getObjecttype());
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		objects.add(obj);
	}
	
	public List<ProxyObject> getList() {
		return objects;
	}


}
