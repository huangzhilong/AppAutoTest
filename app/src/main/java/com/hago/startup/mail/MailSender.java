package com.hago.startup.mail;

import android.text.TextUtils;

import com.hago.startup.util.LogUtil;
import com.hago.startup.util.Utils;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

/**
 * Created by huangzhilong on 18/9/14.
 */

public class MailSender {

    /**
     * 以文本格式发送邮件
     * @param mailInfo 待发送的邮件的信息
     */
    public boolean sendTextMail(final MailInfo mailInfo) {

        // 判断是否需要身份认证
        Properties pro = mailInfo.getProperties();
        // 发件人的账号密码
        pro.put("mail.user", mailInfo.getUserName());
        pro.put("mail.password", mailInfo.getPassword());
        MyAuthenticator authenticator = new MyAuthenticator(mailInfo.getUserName(), mailInfo.getPassword());

        // 根据邮件会话属性和密码验证器构造一个发送邮件的session
        Session sendMailSession = Session.getDefaultInstance(pro, authenticator);

        try {
            // 根据session创建一个邮件消息
            Message mailMessage = new MimeMessage(sendMailSession);
            // 创建邮件发送者地址
            Address from = new InternetAddress(mailInfo.getUserName());
            // 设置邮件消息的发送者
            mailMessage.setFrom(from);
            // 创建邮件的接收者地址
            if (mailInfo.getToAddress() == null || mailInfo.getToAddress().length == 0) {
                LogUtil.logE("MailSender", "getToAddress is empty");
                return false;
            }
            InternetAddress [] toAddress = new InternetAddress[mailInfo.getToAddress().length];
            for (int i = 0; i < mailInfo.getToAddress().length; i++) {
                InternetAddress internetAddress = new InternetAddress(mailInfo.getToAddress()[i]);
                toAddress[i] = internetAddress;
            }
            mailMessage.setRecipients(Message.RecipientType.TO, toAddress);
            // 设置邮件消息的主题
            mailMessage.setSubject(mailInfo.getSubject());
            // 设置邮件消息发送的时间
            mailMessage.setSentDate(new Date());
            // 设置邮件消息的主要内容
            String mailContent = mailInfo.getContent();
            mailMessage.setText(mailContent); //设置了Content时会覆盖这个text

            BodyPart mBodyPart = new MimeBodyPart();
            Multipart mMultipart = new MimeMultipart();
            mBodyPart.setContent(mailContent, "text/html; charset=utf-8");
            mMultipart.addBodyPart(mBodyPart);
            // 附件
            if (!Utils.empty(mailInfo.getAttachFileNames())) {
                for (int i = 0; i < mailInfo.getAttachFileNames().size(); i++) {
                    String fileName = mailInfo.getAttachFileNames().get(i);
                    if (TextUtils.isEmpty(fileName)) {
                        continue;
                    }
                    MimeBodyPart bodyPart = new MimeBodyPart();
                    FileDataSource fds = new FileDataSource(fileName);
                    bodyPart.setDataHandler(new DataHandler(fds));
                    bodyPart.setFileName(MimeUtility.encodeText(fds.getName(), "UTF-8", "B"));
                    mMultipart.addBodyPart(bodyPart);
                }
            }
            mailMessage.setContent(mMultipart);
            // 发送邮件
            Transport.send(mailMessage);
            return true;
        } catch (MessagingException ex) {
            LogUtil.logE("MailSender", "sendMail MessagingException: %s", ex);
        } catch (UnsupportedEncodingException ex) {
            LogUtil.logE("MailSender", "sendMail UnsupportedEncodingException: %s", ex);
        }
        return false;
    }
}
