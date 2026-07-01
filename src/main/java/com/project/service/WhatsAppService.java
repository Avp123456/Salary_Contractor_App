package com.project.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class WhatsAppService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.whatsapp.from}")
    private String fromNumber;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
    }

    public void sendMessage(String receiver, String messageContent) {
        try {
            System.out.println("Initiating WhatsApp Message...");
            
            // Format the receiver number correctly for Twilio WhatsApp API
            String formattedReceiver = receiver.trim();
            if (!formattedReceiver.startsWith("whatsapp:")) {
                // Assuming it's an Indian number if no country code is provided
                if (!formattedReceiver.startsWith("+")) {
                    formattedReceiver = "+91" + formattedReceiver;
                }
                formattedReceiver = "whatsapp:" + formattedReceiver;
            }

            Message message = Message.creator(
                    new PhoneNumber(formattedReceiver),
                    new PhoneNumber(fromNumber),
                    messageContent
            ).create();

            System.out.println("WhatsApp message sent successfully!");
            System.out.println("Message SID: " + message.getSid());
            
        } catch (Exception e) {
            System.err.println("Failed to send WhatsApp message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
