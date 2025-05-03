package com.olh.feeds.core.email.service;

import jakarta.mail.internet.MimeMessage;

public interface MailService {
  void sendEmail(MimeMessage message);

  void sendThresholdEmail(String recipientEmail, String title, String content, String link);
}
