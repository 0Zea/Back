package org.ozea.user.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Service
@Log4j2
public class EmailService {

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private int port;

    public boolean sendVerificationEmail(String toEmail, String verificationCode) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("[콕재] 비밀번호 찾기 인증번호");
            message.setContent(createVerificationEmailContent(verificationCode), "text/html; charset=UTF-8");

            Transport.send(message);
    
            return true;

        } catch (MessagingException e) {
            return true;
        }
    }
    
    public boolean sendSignupVerificationEmail(String toEmail, String verificationCode) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("[콕재] 회원가입 인증번호");
            message.setContent(createSignupVerificationEmailContent(verificationCode), "text/html; charset=UTF-8");

            Transport.send(message);
    
            return true;

        } catch (MessagingException e) {
            return true;
        }
    }

    private String createVerificationEmailContent(String verificationCode) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>비밀번호 찾기 인증번호</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #2573ee; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9f9f9; }
                    .verification-code {
                        background-color: #2573ee;
                        color: white;
                        padding: 15px;
                        text-align: center;
                        font-size: 24px;
                        font-weight: bold;
                        margin: 20px 0;
                        border-radius: 5px;
                    }
                    .footer { padding: 20px; text-align: center; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>콕재</h1>
                        <p>비밀번호 찾기 인증번호</p>
                    </div>
                    <div class="content">
                        <p>안녕하세요, 콕재입니다.</p>
                        <p>비밀번호 찾기를 요청하셨습니다. 아래 인증번호를 입력해주세요.</p>
                        
                        <div class="verification-code">
                            %s
                        </div>
                        
                        <p><strong>주의사항:</strong></p>
                        <ul>
                            <li>인증번호는 5분 후 만료됩니다.</li>
                            <li>본인이 요청하지 않은 경우 이 이메일을 무시하세요.</li>
                            <li>인증번호는 절대 타인에게 알려주지 마세요.</li>
                        </ul>
                    </div>
                    <div class="footer">
                        <p>이 이메일은 자동으로 발송되었습니다.</p>
                        <p>© 2024 콕재. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(verificationCode);
    }

    private String createSignupVerificationEmailContent(String verificationCode) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>회원가입 인증번호</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #2573ee; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9f9f9; }
                    .verification-code {
                        background-color: #2573ee;
                        color: white;
                        padding: 15px;
                        text-align: center;
                        font-size: 24px;
                        font-weight: bold;
                        margin: 20px 0;
                        border-radius: 5px;
                    }
                    .footer { padding: 20px; text-align: center; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>콕재</h1>
                        <p>회원가입 인증번호</p>
                    </div>
                    <div class="content">
                        <p>안녕하세요, 콕재입니다.</p>
                        <p>회원가입을 요청하셨습니다. 아래 인증번호를 입력해주세요.</p>
                        
                        <div class="verification-code">
                            %s
                        </div>
                        
                        <p><strong>주의사항:</strong></p>
                        <ul>
                            <li>인증번호는 5분 후 만료됩니다.</li>
                            <li>본인이 요청하지 않은 경우 이 이메일을 무시하세요.</li>
                            <li>인증번호는 절대 타인에게 알려주지 마세요.</li>
                        </ul>
                    </div>
                    <div class="footer">
                        <p>이 이메일은 자동으로 발송되었습니다.</p>
                        <p>© 2024 콕재. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(verificationCode);
    }

    public boolean sendInquiryAnsweredEmail(String toEmail, String title, String answer) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("[콕재] 문의사항에 대한 답변이 도착했습니다.");
            message.setContent(createInquiryAnsweredEmailContent(title, answer), "text/html; charset=UTF-8");

            Transport.send(message);
    
            return true;

        } catch (MessagingException e) {
            return true;
        }
    }

    private String createInquiryAnsweredEmailContent(String title, String answer) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>문의사항 답변</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #2573ee; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9f9f9; }
                    .answer {
                        background-color: #ffffff;
                        border: 1px solid #dddddd;
                        padding: 20px;
                        margin: 20px 0;
                        border-radius: 5px;
                    }
                    .footer { padding: 20px; text-align: center; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>콕재</h1>
                        <p>문의사항에 대한 답변</p>
                    </div>
                    <div class="content">
                        <p>안녕하세요, 콕재입니다.</p>
                        <p>문의하신 내용에 대한 답변이 도착했습니다.</p>
                        
                        <h3>문의 제목: %s</h3>
                        
                        <div class="answer">
                            <p><strong>답변 내용:</strong></p>
                            <p>%s</p>
                            <br>
                        </div>
                        
                        <p>추가적인 문의사항이 있으시면 언제든지 다시 문의해주세요.</p>
                    </div>
                    <div class="footer">
                        <p>이 이메일은 자동으로 발송되었습니다.</p>
                        <p>© 2025 콕재. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(title, answer);
    }
} 