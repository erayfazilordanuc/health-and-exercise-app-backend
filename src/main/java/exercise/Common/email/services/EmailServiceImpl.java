package exercise.Common.email.services;

import exercise.Common.email.entities.EmailDetails;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

  @Autowired
  private JavaMailSender javaMailSender;

  @Value("${spring.mail.username}")
  private String sender;

  // public String sendSimpleMail(EmailDetails details) {
  // try {
  // SimpleMailMessage mailMessage = new SimpleMailMessage();

  // mailMessage.setFrom(sender);
  // mailMessage.setTo(details.getRecipient());
  // mailMessage.setText(details.getMsgBody());
  // mailMessage.setSubject(details.getSubject());

  // javaMailSender.send(mailMessage);
  // return "Mail Sent Successfully";
  // }

  // catch (Exception e) {
  // return "Error while Sending Mail";
  // }
  // }

  public String sendSimpleMail(EmailDetails details) {
    try {
      MimeMessage mimeMessage = javaMailSender.createMimeMessage();

      MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

      helper.setFrom(new InternetAddress(sender, "HopeMove, egzersiz takip ve sağlık uygulaması"));

      helper.setTo(details.getRecipient());
      helper.setSubject(details.getSubject());
      helper.setText(details.getMsgBody(), /* html? */ false);

      javaMailSender.send(mimeMessage);
      return "Mail sent successfully";
    } catch (Exception ex) {
      ex.printStackTrace();
      return "Error while sending mail";
    }
  }

  public String sendMailWithAttachment(EmailDetails details) {
    MimeMessage mimeMessage = javaMailSender.createMimeMessage();
    MimeMessageHelper mimeMessageHelper;

    try {
      mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
      mimeMessageHelper.setFrom(sender);
      mimeMessageHelper.setTo(details.getRecipient());
      mimeMessageHelper.setText(details.getMsgBody());
      mimeMessageHelper.setSubject(
          details.getSubject());

      FileSystemResource file = new FileSystemResource(
          new File(details.getAttachment()));

      mimeMessageHelper.addAttachment(
          file.getFilename(), file);

      javaMailSender.send(mimeMessage);
      return "Mail sent Successfully";
    }

    catch (MessagingException e) {
      return "Error while sending mail";
    }
  }
}