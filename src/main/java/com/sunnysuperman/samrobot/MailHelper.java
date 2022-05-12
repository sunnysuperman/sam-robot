package com.sunnysuperman.samrobot;

import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class MailHelper {

    public static class UsernamePasswordAuthenticator extends Authenticator {
        String userName = null;
        String password = null;

        public UsernamePasswordAuthenticator(String username, String password) {
            this.userName = username;
            this.password = password;
        }

        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(userName, password);
        }
    }

    public static class SMTPInfo {
        private String host;
        private int port;
        private boolean ssl;
        private String username;
        private String password;

        public boolean isSsl() {
            return ssl;
        }

        public void setSsl(boolean ssl) {
            this.ssl = ssl;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public Properties getProperties() {
            Properties p = new Properties();
            p.put("mail.smtp.host", host);
            p.put("mail.smtp.port", port);
            if (ssl) {
                p.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                p.put("mail.smtp.socketFactory.port", port);
            }
            p.put("mail.smtp.auth", (username != null) ? "true" : "false");
            p.put("mail.smtp.connectiontimeout", 20000);
            p.put("mail.smtp.timeout", 30000);
            return p;
        }

        public Authenticator getAuthenticator() {
            return new UsernamePasswordAuthenticator(username, password);
        }
    }

    public static final class MailInfo {
        private String fromAddress;
        private String toAddress;
        private String[] toAddresses;
        private String subject;
        private String content;
        private String[] attachFileNames;

        public String getFromAddress() {
            return fromAddress;
        }

        public void setFromAddress(String fromAddress) {
            this.fromAddress = fromAddress;
        }

        public String getToAddress() {
            return toAddress;
        }

        public void setToAddress(String toAddress) {
            this.toAddress = toAddress;
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

        public String[] getAttachFileNames() {
            return attachFileNames;
        }

        public void setAttachFileNames(String[] attachFileNames) {
            this.attachFileNames = attachFileNames;
        }

        public String[] getToAddresses() {
            return toAddresses;
        }

        public void setToAddresses(String[] toAddresses) {
            this.toAddresses = toAddresses;
        }

    }

    public static void sendMail(SMTPInfo smtpInfo, MailInfo mailInfo) throws MessagingException {
        Session session = Session.getDefaultInstance(smtpInfo.getProperties(), smtpInfo.getAuthenticator());
        MimeMessage mail = new MimeMessage(session);
        Address from = new InternetAddress(
                mailInfo.getFromAddress() != null ? mailInfo.getFromAddress() : smtpInfo.getUsername());
        mail.setFrom(from);
        if (mailInfo.getToAddresses() != null) {
            String[] addrs = mailInfo.getToAddresses();
            Address[] addresses = new Address[addrs.length];
            for (int i = 0; i < addrs.length; i++) {
                addresses[i] = new InternetAddress(addrs[i]);
            }
            mail.setRecipients(Message.RecipientType.TO, addresses);
        } else {
            Address to = new InternetAddress(mailInfo.getToAddress());
            mail.setRecipient(Message.RecipientType.TO, to);
        }
        mail.setSubject(mailInfo.getSubject());
        if (mailInfo.getContent() != null) {
            Multipart content = new MimeMultipart();
            BodyPart part = new MimeBodyPart();
            part.setContent(mailInfo.getContent(), "text/html; charset=utf-8");
            content.addBodyPart(part);
            mail.setContent(content);
        }
        Transport.send(mail);
    }

}