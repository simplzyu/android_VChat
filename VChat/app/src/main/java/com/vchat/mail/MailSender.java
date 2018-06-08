package com.vchat.mail;

/**
 * Created by simplzy on 2017/5/20.
 */


import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class MailSender {

    public boolean sendTextMail(MailInfo mailInfo){
        boolean b = false;
        MyAuthenticator authenticator  = new MyAuthenticator(mailInfo.getUserName(), mailInfo.getPassword());
        Properties props = mailInfo.getProperties();
        //建立会话
        Session session = Session.getDefaultInstance(props,authenticator);
        //建立消息
        Message msg = new MimeMessage(session);

        try {
            Address from = new InternetAddress(mailInfo.getFormAddr());
            msg.setFrom(from);

            Address to = new InternetAddress(mailInfo.getToAddr());
            msg.addRecipient(Message.RecipientType.TO, to);

            msg.setSubject(mailInfo.getSubject());
            msg.setText(mailInfo.getContent());

            msg.setSentDate(new Date());



            System.out.println("sending...");
            Transport.send(msg);
            System.out.println("send");
            b = true;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return b;
    }

    public boolean sendHtmlMail(MailInfo mailInfo){
        boolean b = false;
        MyAuthenticator authenticator = new MyAuthenticator(mailInfo.getUserName(),mailInfo.getPassword());
        Properties props = mailInfo.getProperties();
        Session session = Session.getDefaultInstance(props,authenticator);
        Message msg = new MimeMessage(session);

        try {
            Address from = new InternetAddress(mailInfo.getFormAddr());
            msg.setFrom(from);

            Address to = new InternetAddress(mailInfo.getToAddr());
            msg.addRecipient(Message.RecipientType.TO, to);

            msg.setSubject(mailInfo.getSubject());
            msg.setSentDate(new Date());

            Multipart part = new MimeMultipart();
            BodyPart html = new MimeBodyPart();
            html.setContent(mailInfo.getContent(),"text/html;charset=utf-8");
            part.addBodyPart(html);
            msg.setContent(part);
            System.out.println("sending");
            Transport.send(msg);
            System.out.println("send");
            b = true;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return b;
    }
}
