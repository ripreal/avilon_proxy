package ru.avilon.proxy.utils;

public class ExceptionUtils {
	public static String toStringWithCause(Throwable ex) {
		return ex.toString() + ": " + (ex != null && ex.getStackTrace().length > 0 ? ex.getStackTrace()[0].toString() : "");
	}
}
