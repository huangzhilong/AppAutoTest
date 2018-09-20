package com.hago.startup.mail;

import android.support.annotation.NonNull;

import com.hago.startup.MonitorTaskInstance;
import com.hago.startup.bean.ResultInfo;

import java.io.File;

/**
 * Created by huangzhilong on 18/9/14.
 */

public class SendMailUtil {

    private final static String USER = "964123660@qq.com";
    private final static String PWD = "kihjwbrkdjjjbdhg";
    private final static String[] TO_ADDRESS = {"huangzhilong@yy.com"};


    public static void send(){
        final MailInfo mailInfo = createMail();
        final MailSender sms = new MailSender();
        MonitorTaskInstance.getInstance().executeRunnable(new Runnable() {
            @Override
            public void run() {
                sms.sendTextMail(mailInfo);
            }
        });
    }

    @NonNull
    private static MailInfo createMail() {
        final MailInfo mailInfo = new MailInfo();
        mailInfo.setUserName(USER); // 你的邮箱地址
        mailInfo.setPassword(PWD);// 您的邮箱密码
        mailInfo.setToAddress(TO_ADDRESS); // 发到哪个邮件去
        mailInfo.setSubject("Hello"); // 邮件主题
        mailInfo.setContent("Android 测试"); // 邮件文本
        return mailInfo;
    }
}


