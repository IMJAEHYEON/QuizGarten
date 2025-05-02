package kopo.poly.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import kopo.poly.dto.MailDTO;
import kopo.poly.service.IMailService;
import kopo.poly.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService implements IMailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromMail;

    @Override
    public int doSendMail(MailDTO pDTO) {

        log.info("{}.doSendMail Start!", this.getClass().getName());

        int res = 1;

        if (pDTO == null) {
            log.error("MailDTO가 null입니다.");
            return 0;
        }

        String toMail = CmmUtil.nvl(pDTO.toMail());
        String title = CmmUtil.nvl(pDTO.title());
        String contents = CmmUtil.nvl(pDTO.contents());

        log.info("toMail : {} / title : {} / contents : {}", toMail, title, contents);

        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, true, "UTF-8");

            messageHelper.setTo(toMail);
            messageHelper.setFrom(fromMail);
            messageHelper.setSubject(title);
            messageHelper.setText(contents, true);

            mailSender.send(message);

        } catch (MessagingException e) {
            res = 0;
            log.info("[ERROR] doSendMail : {}", e.getMessage(), e);
        }

        log.info("{}.doSendMail End!", this.getClass().getName());

        return res;
    }
}
