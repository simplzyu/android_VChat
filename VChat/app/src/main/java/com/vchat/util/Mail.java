package com.vchat.util;

/**
 * Created by simplzy on 2017/5/20.
 */

import java.text.SimpleDateFormat;
import java.util.Date;

import com.vchat.mail.MailInfo;
import com.vchat.mail.MailSender;

public class Mail {
    public static String hostName="smtp.163.com";//smtp.163.com
    public static String sendmail_username="vchat_register@163.com";
    public static String sendmail_password="vchat123456";


    public static void main(String[] args) {
    }

    //info:激活该账号 or 启用新密码
    public static void sendMail(String name, String toAddr, String url, String info){
//		System.out.println(name + " " + toAddr);
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");//可以方便地修改日期格式
        String timeString = dateFormat.format( now );

        MailInfo mailInfo = new MailInfo();
        MailSender ms = new MailSender();
        mailInfo.setHost(hostName);
        mailInfo.setPort("465");
        mailInfo.setUserName(sendmail_username);
        mailInfo.setPassword(sendmail_password);
        mailInfo.setSubject("软微云课堂：欢迎您的注册，请验证邮箱");
        mailInfo.setContent("<body bgcolor=#CACCF9><h3>尊敬的"+name+":</h3><center>您好！您于 "
                + timeString
                + "注册了软微云课堂，请点击如下链接" + info
                + url
                + " (如果您无法点击此链接，请将其复制到浏览器地址栏后访问)"
                + "为了保障您帐号的安全性，请在24小时内完成账号注册，此链接将在您注册后失效！</center></body>");
        mailInfo.setFormAddr(sendmail_username);
        mailInfo.setToAddr(toAddr);
        ms.sendHtmlMail(mailInfo);
    }

    public void sendMail2(String name, String toAddr, String url, String info){
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");//可以方便地修改日期格式
        String timeString = dateFormat.format( now );

        MailInfo mailInfo = new MailInfo();
        MailSender ms = new MailSender();
        mailInfo.setHost(hostName);
        mailInfo.setPort("465");
        mailInfo.setUserName(sendmail_username);
        mailInfo.setPassword(sendmail_password);
        mailInfo.setSubject("软微云课堂：找回密码");
        mailInfo.setContent("<body bgcolor=#CACCF9><h3>尊敬的"+name+":</h3><center>您好！您于 "
                + timeString
                + "在软微云课堂进行了【找回密码操作】，请点击如下链接" + info
                + url
                + " (如果您无法点击此链接，请将其复制到浏览器地址栏后访问)"
                + "为了保障您帐号的安全性，请在24小时内完成账号注册，此链接将在您注册后失效！</center></body>");
        mailInfo.setFormAddr(sendmail_username);
        mailInfo.setToAddr(toAddr);
        ms.sendHtmlMail(mailInfo);
    }

}