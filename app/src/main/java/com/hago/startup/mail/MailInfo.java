package com.hago.startup.mail;

import java.util.List;
import java.util.Properties;

/**
 * Created by huangzhilong on 18/9/14.
 */

public class MailInfo {

    private String[] toAddress; // 邮件接收者的地址
    private String userName; // 登陆邮件发送服务器的用户名
    private String password; // 登陆邮件发送服务器的密码
    private String subject;  // 邮件主题
    private String content;  // 邮件的文本内容
    private List<String> attachFileNames;// 邮件附件的文件名

    /**
     * 获得邮件会话属性
     */
    public Properties getProperties() {
        Properties props = new Properties();
        // props.put("mail.smtp.host", "smtp.qq.com");
        // props.put("mail.smtp.port", "587");
        props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.setProperty("mail.smtp.socketFactory.port", "465");
        // 表示SMTP发送邮件，需要进行身份验证
        props.setProperty("mail.transport.protocol", "smtp");// 设置传输协议
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", "smtp.qq.com");//QQ邮箱的服务器
        return props;
    }

    public String[] getToAddress() {
        return toAddress;
    }

    public void setToAddress(String[] toAddress) {
        this.toAddress = toAddress;
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

    public List<String> getAttachFileNames() {
        return attachFileNames;
    }

    public void setAttachFileNames(List<String> attachFileNames) {
        this.attachFileNames = attachFileNames;
    }
}
