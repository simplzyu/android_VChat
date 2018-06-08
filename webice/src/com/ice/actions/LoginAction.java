package com.ice.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.jivesoftware.smack.XMPPConnection;

import com.ice.forms.UserForm;
import com.ice.util.MapUtil;
import com.ice.util.XmppConnectionManager;
import com.ice.util.XmppUtil;

public class LoginAction extends Action {

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		UserForm userForm = (UserForm)form;
		String name = userForm.getUsername();
		String password = userForm.getPassword();
		System.out.println(userForm.getUsername() + " " + userForm.getPassword());
		
		XmppConnectionManager manager = new XmppConnectionManager();
		XMPPConnection connection = manager.init();
		
//		XmppUtil.register(connection, "小冰", "root");
		boolean isLogin = XmppUtil.login(connection, name, password);
		if(isLogin){
			MapUtil.put("name", name);
			return mapping.findForward("ok");
		}else{
			return mapping.findForward("err");
		}
	
	}
	
}
