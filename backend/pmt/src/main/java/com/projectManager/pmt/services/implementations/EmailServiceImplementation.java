package com.projectManager.pmt.services.implementations;

import com.projectManager.pmt.services.EmailService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImplementation implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void sendTaskAssignedEmail(String recipientEmail, String taskTitle, String projectName, String assignedBy) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipientEmail);
        message.setSubject("Nouvelle tâche assignée : " + taskTitle);
        message.setText(
                "Bonjour,\n\n" +
                        "Une nouvelle tâche vous a été assignée dans le projet \"" + projectName + "\".\n\n" +
                        "Tâche : " + taskTitle + "\n" +
                        "Assignée par : " + assignedBy + "\n\n" +
                        "Merci de consulter le tableau de bord pour plus d’informations.\n\n" +
                        "— Project Manager"
        );

        mailSender.send(message);
    }
}
