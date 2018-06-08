package com.ice.test;

import java.io.UnsupportedEncodingException;

import org.jivesoftware.smack.XMPPConnection;

import com.ice.util.XmppConnectionManager;
import com.ice.util.XmppUtil;

public class Test {
	public static void main(String[] args) {
		XmppConnectionManager manager = new XmppConnectionManager();
		XMPPConnection connection = manager.init();
		
		XmppUtil.register(connection, "小微", "root");
		boolean isLogin = XmppUtil.login(connection, "小微", "root");
		System.out.println(isLogin);
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		new Runnable() {
			
			public void run() {
				// TODO Auto-generated method stub
				
			}
		};
	}
}
