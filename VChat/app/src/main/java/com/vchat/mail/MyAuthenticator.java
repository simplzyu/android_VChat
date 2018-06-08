package com.vchat.mail;

/**
 * Created by simplzy on 2017/5/20.
 */

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class MyAuthenticator extends Authenticator{

    private String userName = null;
    private String password = null;

    public MyAuthenticator(String userName, String password){
        this.userName = userName;
        this.password = password;
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {

        return new PasswordAuthentication(userName, password);
    }

}
