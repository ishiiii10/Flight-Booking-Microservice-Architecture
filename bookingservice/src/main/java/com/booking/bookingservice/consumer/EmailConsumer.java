package com.booking.bookingservice.consumer;

import com.booking.bookingservice.config.MQConfig;
import com.booking.bookingservice.dto.EmailNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailConsumer {

    private static final Logger log = LoggerFactory.getLogger(EmailConsumer.class);
    private final JavaMailSender mailSender;

    public EmailConsumer(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @RabbitListener(queues = MQConfig.EMAIL_QUEUE)
    public void receive(EmailNotification notification) {
        // send actual email
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(notification.getTo());
            msg.setSubject(notification.getSubject());
            msg.setText(notification.getBody());
            mailSender.send(msg);
            log.info("Email sent to {}", notification.getTo());
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", notification.getTo(), e.getMessage());
            // consider retry or dead-lettering in prod
        }
    }
}
