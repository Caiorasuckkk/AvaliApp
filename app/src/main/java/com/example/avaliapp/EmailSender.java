package com.example.avaliapp;

import android.content.Context;
import android.util.Log;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailSender {
    private final Context context;
    private final String username; // Seu e-mail
    private final String password; // Sua senha

    public EmailSender(Context context) {
        this.context = context;
        this.username = "avaliapp@outlook.com"; // Altere para seu e-mail
        this.password = "Caio@1839"; // Altere para sua senha
    }

    public void sendEmail(String recipientEmail, String subject, String body) {
        new Thread(() -> {
            try {
                Properties props = new Properties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true"); // Para TLS
                props.put("mail.smtp.host", "smtp-mail.outlook.com"); // ou smtp.office365.com
                props.put("mail.smtp.port", "587");

                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(username));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
                message.setSubject(subject);
                message.setText(body);

                Transport.send(message);
                Log.d("EmailSender", "Email enviado com sucesso para " + recipientEmail);
            } catch (Exception e) {
                Log.e("EmailSender", "Erro ao enviar e-mail: " + e.getMessage());
            }
        }).start();
    }
}
