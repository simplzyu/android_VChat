package com.ice.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;

public class MyFilter extends HttpServlet implements Filter {  
	  
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String encoding=null;  
  
    public void doFilter(ServletRequest arg0, ServletResponse arg1,  
            FilterChain arg2) throws IOException, ServletException {  
  
        //一下两段代码如果可以理解就好 不可以理解的话 到时候用的时候直接拿来用就好了  
        arg0.setCharacterEncoding(encoding);   
        arg2.doFilter(arg0, arg1);  
    }  
  
    public void init(FilterConfig arg0) throws ServletException {  
        // TODO Auto-generated method stub  
        encoding = arg0.getInitParameter("encoding"); //获得配置文件中的encoding  
    }
}


