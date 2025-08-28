package com.multibank.notification_routing.service.channel;

import com.multibank.notification_routing.dto.EventsRequestDto;
import com.multibank.notification_routing.service.enums.Channel;
import org.springframework.stereotype.Component;

@Component
public class EmailChannel implements NotificationChannel {

    @Override
    public Channel channel() {
        return Channel.EMAIL;
    }

    @Override
    public Boolean send(EventsRequestDto event) {

            // Convert payload into message
            String message = "Welcome " + event.getRecipient() + "!";
            // Simulate email delivery (replace with actual SMTP/SES later)
            System.out.println("📧 Sending EMAIL to " + event.getRecipient() + " : " + message);
            return true;

    }
}