package com.olh.feeds.core.email.service.impl;

import com.olh.feeds.core.email.exception.SetHelperFailedException;
import com.olh.feeds.core.email.service.MailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.UnsupportedEncodingException;

import static com.olh.feeds.core.email.constant.MailConstants.EMAIL_TITLE;
import static com.olh.feeds.core.email.constant.MailConstants.FROM_EMAIL;

@Slf4j
public class MailServiceImpl implements MailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public MailServiceImpl(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Override
    public void sendEmail(MimeMessage message) {
        mailSender.send(message);
    }

    @Override
    public void sendThresholdEmail(String recipientEmail, String title, String content, String link) {
        Context context = new Context();
        context.setVariable("title", title);
        context.setVariable("content", content);
        context.setVariable("link", link);
        String emailContent = templateEngine.process("email-template", context);
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        try {
            helper.setFrom(FROM_EMAIL, EMAIL_TITLE);
            helper.setTo(recipientEmail);
            helper.setSubject(title);
            helper.setText(emailContent, true); // Thêm tham số true ở đây
        } catch (MessagingException e) {
            log.error(e.getMessage(), e);
        } catch (UnsupportedEncodingException e) {
            throw new SetHelperFailedException();
        }
        mailSender.send(message);
    }
}

