package com.tasks.organizer.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {

  private static final Logger log = LoggerFactory.getLogger(EmailService.class);
  private final JavaMailSender mailSender;

  @Value ("${app.mail.from-address}")
  private String fromAddress;

  @Value("${app.mail.is-testing}")
    private boolean isTesting;

  public EmailService(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  public void sendText(String to, String subject, String body) {
    //We add this to prevent sending emails during testing
    if (isTesting) {
      log.info("[TEST MODE] Suppressed email dispatch to={}. Content: {}", to, subject);
      return; 
    }

    try {
      SimpleMailMessage msg = new SimpleMailMessage();
      msg.setFrom(fromAddress);
      msg.setTo(to);
      msg.setSubject(subject);
      msg.setText(body);
      mailSender.send(msg);
    } catch (MailException e) {
      log.warn("Failed to send email: to={} subject={} error={}", to, subject, e.getMessage());
    }
  }
}