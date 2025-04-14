package kopo.poly.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import kopo.poly.service.IMailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService implements IMailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendPasswordResetMail(String toEmail, String resetUrl) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("[MyQuiz] 비밀번호 재설정 안내");
            helper.setText(buildEmailContent(resetUrl), true); // HTML 형식 허용

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("이메일 전송 실패", e);
        }
    }

    private String buildEmailContent(String resetUrl) {
        return "<h3>[MyQuiz] 비밀번호 재설정 안내</h3>" +
                "<p>아래 링크를 클릭하여 비밀번호를 재설정해 주세요:</p>" +
                "<p><a href=\"" + resetUrl + "\">비밀번호 재설정하기</a></p>" +
                "<p>본 메일은 30분 뒤 만료됩니다.</p>";
    }
}
