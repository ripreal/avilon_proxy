package ru.avilon.proxy.entities.properties;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.PROPERTY, property = "valueType")
@JsonSubTypes({ @Type(value = BarcodeNumberValue.class, name = "barcode_number"),
	@Type(value = BooleanValue.class, name = "boolean"),
	@Type(value = DateValue.class, name = "date"),
	@Type(value = NumberValue.class, name = "number"),
	@Type(value = BinaryValue.class, name = "binary"),
	@Type(value = StringFromListValue.class, name = "string_from_list"),
	@Type(value = StringQRValue.class, name = "qr_string"),
	@Type(value = StringValue.class, name = "string")})
public abstract class PropertyValue<T> {
	
	T value;
	
	Class<T> clazz;
	
	protected PropertyValue(Class<T> clazz) {
		this.clazz = clazz;
		//value = (T) DefaultValues.getDefault(clazz);
	}
	
	public void setValue(T value) {
		this.value = value;
	}
	
	public T getValue() {
		return value;
	}
}
