package com.sunnysuperman.samrobot;

import com.sunnysuperman.samrobot.MailHelper.MailInfo;
import com.sunnysuperman.samrobot.MailHelper.SMTPInfo;

public class MailService {

    public static void send(SMTPInfo smtpInfo, String to, String content) {
        MailInfo mail = new MailInfo();
        mail.setToAddress(to);
        mail.setSubject("Sam信息");
        mail.setContent(content);
        mail.setFromAddress(smtpInfo.getUsername());

        while (true) {
            try {
                MailHelper.sendMail(smtpInfo, mail);
                break;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            try {
                Thread.sleep(1000);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void sendSuccessMail(SMTPInfo info, String to) {
        send(info, to, "恭喜你，抢到菜了");
    }

    public static void sendStartMail(SMTPInfo info, String to) {
        send(info, to, "开始抢菜");
    }
}
