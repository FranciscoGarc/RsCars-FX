package com.rscars.taller.rscarsfx;

// --- IMPORTACIONES CORREGIDAS A 'jakarta.mail' ---
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.InputStream;
import java.util.Properties;
import java.util.Random;

public class EmailService {

    public static String enviarCodigoRestablecimiento(String destinatario) {
        String codigo = String.format("%06d", new Random().nextInt(999999));

        Properties props = new Properties();
        try (InputStream input = EmailService.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.err.println("Error: No se encontró el archivo config.properties.");
                return null;
            }
            props.load(input);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error: No se pudo cargar config.properties para el servicio de correo.");
            return null;
        }

        final String usuario = props.getProperty("mail.user");
        final String password = props.getProperty("mail.password");

        Properties mailProps = new Properties();
        mailProps.put("mail.smtp.host", props.getProperty("mail.smtp.host"));
        mailProps.put("mail.smtp.port", props.getProperty("mail.smtp.port"));
        mailProps.put("mail.smtp.auth", props.getProperty("mail.smtp.auth"));
        mailProps.put("mail.smtp.starttls.enable", props.getProperty("mail.smtp.starttls.enable"));

        Session session = Session.getInstance(mailProps, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(usuario, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(usuario));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            message.setSubject("Restablecimiento de Contraseña - RsCars");

            String contenidoHtml = "<h1>Restablecimiento de Contraseña - RsCars</h1>"
                    + "<p>Has solicitado restablecer tu contraseña. Usa el siguiente código para continuar:</p>"
                    + "<h2 style='color:blue;'>" + codigo + "</h2>"
                    + "<p>Si no solicitaste este código, por favor ignora este mensaje o contacta con soporte.</p>";
            message.setContent(contenidoHtml, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("Código de restablecimiento enviado exitosamente a " + destinatario);
            return codigo;

        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("Error al enviar el correo de restablecimiento. Revisa las credenciales y la configuración de la cuenta.");
            return null;
        }
    }

    public static String enviarCodigoVerificacion(String destinatario) {
        String codigo = String.format("%06d", new Random().nextInt(999999));

        Properties props = new Properties();
        try (InputStream input = EmailService.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.err.println("Error: No se encontró el archivo config.properties.");
                return null;
            }
            props.load(input);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error: No se pudo cargar config.properties para el servicio de correo.");
            return null;
        }

        final String usuario = props.getProperty("mail.user");
        final String password = props.getProperty("mail.password");

        Properties mailProps = new Properties();
        mailProps.put("mail.smtp.host", props.getProperty("mail.smtp.host"));
        mailProps.put("mail.smtp.port", props.getProperty("mail.smtp.port"));
        mailProps.put("mail.smtp.auth", props.getProperty("mail.smtp.auth"));
        mailProps.put("mail.smtp.starttls.enable", props.getProperty("mail.smtp.starttls.enable"));

        Session session = Session.getInstance(mailProps, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(usuario, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(usuario));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            message.setSubject("Tu Código de Verificación para RsCars");

            String contenidoHtml = "<h1>Verificación de Cuenta - RsCars</h1>"
                    + "<p>Gracias por registrarte. Usa el siguiente código para activar tu cuenta:</p>"
                    + "<h2 style='color:blue;'>" + codigo + "</h2>"
                    + "<p>Si no solicitaste este código, puedes ignorar este mensaje.</p>";
            message.setContent(contenidoHtml, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("Código de verificación enviado exitosamente a " + destinatario);
            return codigo;

        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("Error al enviar el correo de verificación. Revisa las credenciales y la configuración de la cuenta.");
            return null;
        }
    }
}