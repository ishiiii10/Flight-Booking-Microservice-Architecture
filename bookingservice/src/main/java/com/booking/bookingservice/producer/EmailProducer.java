package com.booking.bookingservice.producer;

import com.booking.bookingservice.config.MQConfig;
import com.booking.bookingservice.dto.EmailNotification;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class EmailProducer {
    private final RabbitTemplate rabbitTemplate;

    public EmailProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendEmail(EmailNotification notification) {
        rabbitTemplate.convertAndSend(MQConfig.EMAIL_QUEUE, notification);
    }
}
