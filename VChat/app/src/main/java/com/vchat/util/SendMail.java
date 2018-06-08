package com.vchat.util;

import android.util.Log;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

/**
 * Created by simplzy on 2017/5/20.
 */

public class SendMail extends Thread {

    private String name;
    private String toAddr;
    private String info;

    public SendMail(String name, String toAddr, String info){
        this.name = name;
        this.toAddr = toAddr;
        this.info = info;
    }
    @Override
    public void run() {
        // TODO Auto-generated method stub
        super.run();
        try {
            //创建HtmlEmail类
            HtmlEmail email = new HtmlEmail();
            //填写邮件的主机明，我这里使用的是163
            email.setHostName("smtp.163.com");
//            email.setSmtpPort(465);
//            email.setTLS(true);
//            email.setSSL(true);
            //设置字符编码格式，防止中文乱码
            email.setCharset("gbk");
            //设置收件人的邮箱
            email.addTo(toAddr);
            //设置发件人的邮箱
            email.setFrom("vchat_register@163.com");
            //填写发件人的用户名和密码
            email.setAuthentication("vchat_register@163.com", "vchat123456");
            //填写邮件主题
            email.setSubject("验证");
            //填写邮件内容
            email.setMsg(info);
            //发送邮件
            email.send();
            Log.i("TAG", "successed!");

        } catch (EmailException e) {
            // TODO Auto-generated catch block
            Log.i("TAG", "---------------->"+e.getMessage());
        }
    }
}