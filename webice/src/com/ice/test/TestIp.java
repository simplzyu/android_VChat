package com.ice.test;

import org.json.JSONObject;

import com.ice.util.HTTPSend;

public class TestIp {
	public static void main(String[] args) throws Exception {
		
		String params = "110.52.250.126";
		String url = "http://ip.taobao.com/service/getIpInfo.php?ip=";
		String res = HTTPSend.get(url, params);
		JSONObject rec = new JSONObject(res);
		System.out.println(res);
		JSONObject data = new JSONObject(rec.getString("data"));
		System.out.println(data.getString("city"));
		
		
	}
}
