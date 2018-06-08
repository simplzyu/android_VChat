package com.ice.util;

import java.util.HashMap;

public class MapUtil {
	/**
	 * 普通字段存放地址
	 */
	public static HashMap<String, String> hm = new HashMap<String, String>();
			
	public static void put(String key, String value){
		hm.put(key, value);
	}
	
	public static String get(String key){
		return hm.get(key);
	}
}
