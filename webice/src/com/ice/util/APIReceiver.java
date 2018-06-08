package com.ice.util;

import org.json.JSONException;
import org.json.JSONObject;

public class APIReceiver {
	public static String getResult(String content) {
		JSONObject parm = new JSONObject();
		try {
			parm.put("key", Const.APIKEY);
			parm.put("info", content);
			String rec = HTTPSend.post(parm.toString(), Const.APIURL);
			JSONObject res = new JSONObject(rec);
			String msg = res.getString("text");
			return msg;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
