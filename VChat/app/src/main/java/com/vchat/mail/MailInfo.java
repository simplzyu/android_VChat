package com.vchat.mail;

/**
 * Created by simplzy on 2017/5/20.
 */

import java.util.Properties;

public class MailInfo {

    private String host;
    private String port;
    private String userName;
    private String password;
    private String formAddr;
    private String toAddr;
    private String subject;
    private String content;

    public Properties getProperties(){
        Properties prop = new Properties();
        prop.setProperty("mail.smtp.host", this.host);
        prop.setProperty("mail.smtp.port", this.port);
        prop.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        prop.setProperty("mail.smtp.socketFactory.fallback", "false");
        prop.setProperty("mail.smtp.socketFactory.port", "465");
        prop.setProperty("mail.smtp.auth", "true");
        return prop;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFormAddr() {
        return formAddr;
    }

    public void setFormAddr(String formAddr) {
        this.formAddr = formAddr;
    }

    public String getToAddr() {
        return toAddr;
    }

    public void setToAddr(String toAddr) {
        this.toAddr = toAddr;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
