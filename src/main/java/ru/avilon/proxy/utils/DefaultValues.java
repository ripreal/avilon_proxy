package ru.avilon.proxy.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultValues {
	static Map<Class<?>,Object>map=new HashMap<Class<?>,Object>();
	static {
		// Only add to this map via put(Map, Class<T>, T)

		put(map, boolean.class, false);
		put(map, Boolean.class, false);
		put(map, char.class, '\0');
		put(map, Character.class, '\0');
		put(map, byte.class, (byte) 0);
		put(map, Byte.class, (byte) 0);
		put(map, short.class, (short) 0);
		put(map, Short.class, (short) 0);
		put(map, int.class, 0);
		put(map, Integer.class, 0);
		put(map, long.class, 0L);
		put(map, Long.class, 0L);
		put(map, float.class, 0f);
		put(map, Float.class, 0f);
		put(map, double.class, 0d);
		put(map, Double.class, 0d);
		put(map, String.class, "");
		put(map, List.class, new ArrayList<Object>());
	}

	private static <T> void put(Map<Class<?>, Object> map, Class<T> type, T value) {
		map.put(type, value);
	}
	
	public static Object getDefault(Class<?> clazz) {
		return map.get(clazz);
	}
}
