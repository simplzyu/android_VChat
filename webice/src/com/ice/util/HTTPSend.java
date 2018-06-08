package com.ice.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by XdaTk on 2014/12/21.
 * <p/>
 * HTTP请求工具类
 */
public class HTTPSend {

	public static String post(String postData, String postUrl) {
		try {

			// 发送POST请求
			URL url = new URL(postUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setUseCaches(false);
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Length", "" + postData.length());
			OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
			out.write(postData);
			out.flush();
			out.close();
			// 获取响应状态
			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				System.out.println("connect failed!");
				return "";
			}
			// 获取响应内容体
			String line, result = "";
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
			while ((line = in.readLine()) != null) {
				result += line + "\n";
			}
			in.close();
			return result;
		} catch (IOException e) {
			e.printStackTrace(System.out);
		}
		return "";
	}

	public static String get(String url, String param) throws IOException {
		StringBuffer buffer = new StringBuffer(); // 用来拼接参数
		StringBuffer result = new StringBuffer(); // 用来接受返回值
		URL httpUrl = null; // HTTP URL类 用这个类来创建连接
		URLConnection connection = null; // 创建的http连接
		BufferedReader bufferedReader = null; // 接受连接受的参数
		// 如果存在参数，我们才需要拼接参数 类似于 localhost/index.html?a=a&b=b
		if(param != null){
			url += param;
		}
		// 创建URL
		httpUrl = new URL(url);
		// 建立连接
		connection = httpUrl.openConnection();
		connection.setRequestProperty("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		connection.setRequestProperty("connection", "keep-alive");
		connection.setRequestProperty("user-agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:34.0) Gecko/20100101 Firefox/34.0");
		connection.connect();
		// 接受连接返回参数
		bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String line;
		while ((line = bufferedReader.readLine()) != null) {
			result.append(line);
		}
		bufferedReader.close();
		return result.toString();
	}


}