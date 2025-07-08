package com.shreettam.mini_google_drive.service;


import com.shreettam.mini_google_drive.model.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class NotificationService {

    private final JavaMailSender mailSender;
    private final SimpMessagingTemplate messagingTemplate;
    private final String appEmail;

    public NotificationService(JavaMailSender mailSender,
                             SimpMessagingTemplate messagingTemplate,
                             @Value("${app.email}") String appEmail) {
        this.mailSender = mailSender;
        this.messagingTemplate = messagingTemplate;
        this.appEmail = appEmail;
    }

    // Email Notifications
    @Async
    public void sendEmailNotification(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(appEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    // Real-Time WebSocket Notifications
    public void sendWebSocketNotification(Long userId, String eventType, Object payload) {
        Map<String, Object> message = new HashMap<>();
        message.put("event", eventType);
        message.put("data", payload);
        messagingTemplate.convertAndSendToUser(
            userId.toString(), 
            "/queue/notifications", 
            message
        );
    }

    // Specific Notification Methods
    @Async
    public void notifyFileShared(User recipient, File file, User sharedBy, String permission) {
        // Email
        String emailText = String.format(
            "%s shared '%s' with you (%s access).\n\n" +
            "Access the file: %s",
            sharedBy.getName(),
            file.getName(),
            permission,
            generateFileLink(file.getId())
        );
        sendEmailNotification(recipient.getEmail(), "New Shared File", emailText);

        // WebSocket
        Map<String, String> payload = new HashMap<>();
        payload.put("fileId", file.getId().toString());
        payload.put("fileName", file.getName());
        payload.put("sharedBy", sharedBy.getName());
        sendWebSocketNotification(recipient.getId(), "FILE_SHARED", payload);
    }

    @Async
    public void notifyStorageLimit(User user, long usedPercentage) {
        if (usedPercentage > 90) {
            String emailText = String.format(
                "Your storage is %d%% full. Please upgrade your plan or free up space.",
                usedPercentage
            );
            sendEmailNotification(user.getEmail(), "Storage Limit Alert", emailText);
            
            Map<String, Object> payload = Map.of(
                "usedPercentage", usedPercentage,
                "alertLevel", "CRITICAL"
            );
            sendWebSocketNotification(user.getId(), "STORAGE_ALERT", payload);
        }
    }
    
    //Welcome Email
    @Async
    public void sendWelcomeEmail(String to) {
        String subject = "Welcome to Mini Google Drive!";
        String text = "Hi, welcome to our cloud storage platform. Start uploading and sharing files now!";
        sendEmailNotification(to, subject, text);
    }
    
    //Login Email
    @Async
    public void sendLoginEmail(String to) {
        String subject = "Login Notification";

        // Format the time in IST
        String loginTime = java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Kolkata"))
                            .format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss z"));

        String text = String.format("Hello, your account was just logged in at %s", loginTime);

        sendEmailNotification(to, subject, text);
    }




    // Helper
    private String generateFileLink(Long fileId) {
        return "https://your-app.com/files/" + fileId;
    }

	public void sendStorageUpgradeEmail(String email, long additionalBytes) {
		// TODO Auto-generated method stub
		
	}

	public void sendGoodbyeEmail(String email) {
		String subject = "Goodbye to Mini Google Drive!";
        String text = "Goodbye";
        sendEmailNotification(email, subject, text);
		
	}

	public void sendShareNotification(String recipientEmail, String fileName, String permission, String ownerName) {
	    System.out.printf(
	        "Notification: %s shared the file '%s' with %s (Permission: %s)%n",
	        ownerName, fileName, recipientEmail, permission
	    );
	}


	
}
