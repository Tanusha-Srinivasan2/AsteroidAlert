package com.star.notificationservice.service;

import com.star.notificationservice.entity.Notification;
import com.star.notificationservice.repository.NotificationRepository;
import com.star.notificationservice.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl; // Not always needed for direct use
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

@Service
@Slf4j
public class EmailService {

    // Corrected @Value syntax: use ${...} not $(...)
    @Value("${email.service.from.email}")
    private String fromEmail;

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;


    @Autowired
    public EmailService(NotificationRepository notificationRepository, UserRepository userRepository, JavaMailSender mailSender) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.mailSender = mailSender;
    }

    /**
     * Sends asteroid alert emails to all users who have notifications enabled.
     * This method is called by a scheduled job.
     *
     * @Async: Why?
     * The @Async annotation means that this method will be executed in a separate thread.
     * This is important because sending emails can be a time-consuming operation (network calls).
     * By making it @Async, the scheduled job (sendAlertingEmail in NotificationService)
     * doesn't have to wait for all emails to be sent before it completes. It can quickly
     * trigger this method and then move on, keeping your application responsive.
     * Without @Async, if sending emails took a long time, your scheduled job would block
     * for that duration, potentially causing delays in other scheduled tasks or even
     * impacting overall application performance.
     */
    @Async
    public void sendAsteroidAlertEmail(){
        final String text = createEmailTest();
        if(text==null){
            log.info("No new asteroids to send alerts for at {}", LocalTime.now());
            return;
        }

        // This query already filters for users with notificationEnabled = true
        final List<String> toEmails = userRepository.findAllEmailsAndNotificationEnabled();
        if(toEmails.isEmpty()){
            log.info("No users with notifications enabled to send email to.");
            return;
        }
        toEmails.forEach(toEmail -> sendEmail(toEmail,text));
        log.info("Email sent to: #{} users", toEmails.size());
    }

    private void sendEmail(final String toEmail, final String text) {
        // SimpleMailMessage: What is it?
        // SimpleMailMessage is a basic implementation of Spring's MailMessage interface.
        // It's used for creating simple email messages with text content, sender, recipients,
        // and subject. It does not support HTML content, attachments, or complex formatting.
        // For more complex emails, you would use MimeMessageHelper with JavaMailSender.
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setFrom(fromEmail);
        message.setSubject("Nasa Asteroid Collision Event");
        message.setText(text);
        try {
            mailSender.send(message);
            log.info("Successfully sent email to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }

    private String createEmailTest(){
        List<Notification> notificationList = notificationRepository.findByEmailSent(false);
        if(notificationList.isEmpty()){
            return null;
        }

        StringBuilder emailText =  new StringBuilder();
        emailText.append("Asteroid Alert: \n");
        emailText.append("=========================\n");
        notificationList.forEach(notification -> {
            emailText.append("Asteroid Name: ").append(notification.getAsteroidName()).append("\n");
            emailText.append("Close Approach Date: ").append(notification.getCloseAppraochDate()).append("\n");
            emailText.append("Estimated Diameter Avg Meters: ").append(notification.getEstimatedDiameterAvgMeters()).append("\n");
            emailText.append("Miss Distance Kilometers: ").append(notification.getMissDistanceKilometers()).append("\n");
            emailText.append("=========================\n");
            notification.setEmailSent(true);
            notificationRepository.save(notification); // Mark as sent
        });
        return emailText.toString(); // Why return?
        // It returns the constructed email body as a String. This String is then used by the sendAsteroidAlertEmail()
        // method to be sent to all recipients. This separation of concerns (creating content vs. sending)
        // makes the code cleaner and easier to test or modify.
    }
}