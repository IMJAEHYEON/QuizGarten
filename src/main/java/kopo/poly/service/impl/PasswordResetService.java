package kopo.poly.service.impl;

import kopo.poly.util.EncryptUtil;
import kopo.poly.dto.MailDTO;
import kopo.poly.dto.PasswordChangeDTO;
import kopo.poly.dto.PasswordResetDTO;
import kopo.poly.repository.UserInfoRepository;
import kopo.poly.repository.entity.UserInfoEntity;
import kopo.poly.service.IMailService;
import kopo.poly.service.IPasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService implements IPasswordResetService {

    private final UserInfoRepository userInfoRepository;

    private final IMailService mailService;


    /**
     * 비밀번호 재설정을 위해서 이메일로 임시비밀번호 발송하는 기능
     *
     */
    @Override
    @Transactional
    public boolean sendTempPassword(PasswordResetDTO pDTO) throws Exception {

        Optional<UserInfoEntity> rEntity = userInfoRepository.findById(pDTO.userId());

        if (rEntity.isEmpty()) {
            log.warn("일치하는 사용자 없음. userId: {}", pDTO.userId());
            return false;
        }

        UserInfoEntity entity = rEntity.get();

        String encryptedEmail = entity.getEmail(); // 암호화된 이메일
        String decryptedEmail = EncryptUtil.decAES128CBC(encryptedEmail); // 복호화

        if (!decryptedEmail.equals(pDTO.email())) {
            log.warn("이메일 불일치. userId: {}, 입력 이메일: {}, 저장된 이메일: {}", pDTO.userId(), pDTO.email(), decryptedEmail);
            return false;
        }

        // 1️⃣ 임시 비밀번호 생성
        RandomStringGenerator generator = new RandomStringGenerator.Builder()
                .withinRange('0', 'z')
                .filteredBy(Character::isLetterOrDigit)
                .build();

        String tempPw = generator.generate(10);

        // 2️⃣ SHA-256 해시로 암호화
        String encPw = EncryptUtil.encHashSHA256(tempPw);

        entity.changePassword(encPw);
        userInfoRepository.save(entity);

        String subject = "[MyQuiz] 임시 비밀번호 발급 안내";
        String content = "임시 비밀번호는 " + tempPw + " 입니다.\n로그인 후 반드시 비밀번호를 변경해주세요.";

        MailDTO mailDTO = MailDTO.builder()
                .toMail(pDTO.email())
                .title(subject)
                .contents(content)
                .build();

        int mailRes = mailService.doSendMail(mailDTO);

        if (mailRes == 0) {
            log.error("메일 발송 실패");
            return false;
        }

        return true;
    }

    @Override
    @Transactional
    public boolean changePassword(PasswordChangeDTO pDTO) throws Exception {
        Optional<UserInfoEntity> rEntity = userInfoRepository.findByUserId(pDTO.userId());

        if (rEntity.isEmpty()) {
            log.warn("사용자 정보 없음 - userId: {}", pDTO.userId());
            return false;
        }

        // 1️⃣ SHA-256 해시로 암호화
        String encPw = EncryptUtil.encHashSHA256(pDTO.password());

        UserInfoEntity entity = rEntity.get();
        entity.changePassword(encPw); // 비밀번호 업데이트

        userInfoRepository.save(entity); // 저장

        log.info("비밀번호 변경 완료 - userId: {}", pDTO.userId());
        return true;
    }

}
