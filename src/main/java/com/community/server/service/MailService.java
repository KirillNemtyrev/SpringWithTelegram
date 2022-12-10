package com.community.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

@Service
public class MailService {

    @Value("${spring.mail.username}")
    public String mailFrom;

    @Autowired
    public JavaMailSender mailSender;

    public void sendEmail(String email, String subject, String payload) throws MessagingException {
        MimeMessage mail = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mail, true);

        helper.setFrom(new InternetAddress(mailFrom));
        helper.setTo(email);
        helper.setSubject(subject);
        helper.setText("text/html", payload);

        mailSender.send(mail);
    }
}
