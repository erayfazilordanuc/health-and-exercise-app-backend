package exercise.Common.email.services;

import exercise.Common.email.entities.EmailDetails;

public interface EmailService {

  String sendSimpleMail(EmailDetails details);

  String sendMailWithAttachment(EmailDetails details);
}