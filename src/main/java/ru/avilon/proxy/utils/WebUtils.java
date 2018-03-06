package ru.avilon.proxy.utils;

import java.text.MessageFormat;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

public class WebUtils {

	public static String debugRequest(HttpServletRequest request) {
		return MessageFormat.format("request=[method=({7}) ip=({0}) x-real-ip=({1}) x-forwarded-for=({2}) cookie=({3}) ref=({4}) ua=({5}) path=({6})]", 
				request.getRemoteAddr(), 
				request.getHeader("X-Real-IP"),
				request.getHeader("X-Forwarded-For"), 
				request.getHeader("Cookie"), 
				request.getHeader("Referer"), 
				request.getHeader("User-Agent"),
				request.getRequestURL() + (StringUtils.isNotBlank(request.getQueryString()) ? "?"+ request.getQueryString() : ""),
				request.getMethod());
	}
}
