package kopo.poly.service;

public interface IMailService {

    void sendPasswordResetMail(String toEmail, String resetUrl);
}
