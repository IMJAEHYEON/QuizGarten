
package kopo.poly.service.impl;

import kopo.poly.dto.UserInfoDTO;
import kopo.poly.repository.UserInfoRepository;
import kopo.poly.repository.entity.UserInfoEntity;
import kopo.poly.service.IUserInfoService;
import kopo.poly.util.CmmUtil;
import kopo.poly.util.DateUtil;
import kopo.poly.util.EncryptUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserInfoService implements IUserInfoService {

    // 회원 관련 Repository
    private final UserInfoRepository userInfoRepository;

    @Override
    public UserInfoDTO getUserIdExists(@NonNull UserInfoDTO pDTO) throws Exception {
        log.info("{}.getUserIdExists Start!", this.getClass().getName());

        log.info("pDTO : {}", pDTO);

        String userId = CmmUtil.nvl(pDTO.userId()); // 아이디

        // DB에서 아이디 중복 여부 확인
        boolean exists = userInfoRepository.findByUserId(userId).isPresent();

        // 존재 여부에 따라 DTO 생성
        UserInfoDTO rDTO = UserInfoDTO.builder()
                .existsYn(exists ? "Y" : "N")
                .build();

        log.info("{}.getUserIdExists End!", this.getClass().getName());
        return rDTO;
    }

    @Override
    public UserInfoDTO getEmailExists(UserInfoDTO pDTO) throws Exception {
        log.info("{}.getEmailExists Start!", this.getClass().getName());

        // 이메일을 암호화해서 조회해야 함
        String email = EncryptUtil.encAES128CBC(CmmUtil.nvl(pDTO.email()));

        Optional<UserInfoEntity> rEntity = userInfoRepository.findByEmail(email);

        UserInfoDTO rDTO = rEntity.map(entity -> UserInfoDTO.builder()
                .existsYn("Y")
                .build()).orElseGet(() -> UserInfoDTO.builder()
                .existsYn("N")
                .build());

        log.info("{}.getEmailExists End!", this.getClass().getName());
        return rDTO;
    }

    @Override
    public UserInfoDTO getUserNameExists(UserInfoDTO pDTO) throws Exception {
        log.info("getUserNameExists Start!");

        String userName = CmmUtil.nvl(pDTO.userName());

        boolean exists = userInfoRepository.findByUserName(userName).isPresent();

        return UserInfoDTO.builder()
                .existsYn(exists ? "Y" : "N")
                .build();
    }


    @Override
    public int insertUserInfo(@NonNull UserInfoDTO pDTO) throws Exception {

        log.info("{}.insertUserInfo Start!", this.getClass().getName());

        log.info("pDTO : {}", pDTO); // Controller 에서 값 전달 잘 되었는지 확인하기

        // 회원가입 성공 : 1, 아이디 중복으로인한 가입 취소 : 2, 기타 에러 발생 : 0
        int res;

        String userId = CmmUtil.nvl(pDTO.userId()); // 아이디
        String userName = CmmUtil.nvl(pDTO.userName()); // 이름
        String password = CmmUtil.nvl(pDTO.password()); // 비밀번호
        String email = CmmUtil.nvl(pDTO.email()); // 이메일
        String addr1 = CmmUtil.nvl(pDTO.addr1()); // 주소
        String addr2 = CmmUtil.nvl(pDTO.addr2()); // 상세주소

        // 회원 가입 중복 방지를 위해 DB에서 데이터 조회
        Optional<UserInfoEntity> rEntity = userInfoRepository.findByUserId(userId);

        // 값이 존재한다면... (이미 회원가입된 아이디)
        if (rEntity.isPresent()) {
            res = 2;

        } else {

            // 회원가입을 위한 Entity 생성
            UserInfoEntity pEntity = UserInfoEntity.builder()
                    .userId(userId).userName(userName)
                    .password(password)
                    .email(email)
                    .addr1(addr1).addr2(addr2)
                    .regId(userId).regDt(DateUtil.getDateTime("yyyy-MM-dd hh:mm:ss"))
                    .chgId(userId).chgDt(DateUtil.getDateTime("yyyy-MM-dd hh:mm:ss"))
                    .build();

            // 회원정보 DB에 저장
            userInfoRepository.save(pEntity);

            // JPA의 save함수는 데이터 값에 따라 등록, 수정을 수행함
            // 물론 잘 저장되겠지만, 내가 실행한 save 함수가 DB에 등록이 잘 수행되었는지 100% 확신이 불가능함
            // 회원 가입후, 혹시 저장 안될 수 있기에 조회 수행함
            // 회원 가입 중복 방지를 위해 DB에서 데이터 조회
            res = userInfoRepository.findByUserId(userId).isPresent() ? 1 : 0;

        }

        log.info("{}.insertUserInfo End!", this.getClass().getName());

        return res;
    }

    /**
     * 로그인을 위해 아이디와 비밀번호가 일치하는지 확인하기
     *
     * @param pDTO 로그인을 위한 회원아이디, 비밀번호
     * @return 로그인된 회원아이디 정보
     */
    @Override
    public int getUserLogin(@NonNull UserInfoDTO pDTO) throws Exception {

        log.info("{}.getUserLoginCheck Start!", this.getClass().getName());

        String userId = CmmUtil.nvl(pDTO.userId()); // 아이디
        String inputPassword = CmmUtil.nvl(pDTO.password());

        log.info("userId : {}, 해시된 입력 비밀번호 : {}", userId, inputPassword);

        Optional<UserInfoEntity> rEntity = userInfoRepository.findByUserId(userId);

        if (rEntity.isPresent()) {
            String storedPassword = rEntity.get().getPassword(); // DB 비밀번호 SHA-256 저장된 값
            log.info("DB 저장된 password : {}", storedPassword);

            if (storedPassword.equals(inputPassword)) {
                log.info("비밀번호 일치!");
                return 1; // 로그인 성공
            } else {
                log.warn("비밀번호 불일치!");
            }
        } else {
            log.warn("아이디 없음!");
        }

        log.info("{}.getUserLoginCheck End!", this.getClass().getName());
        return 0; // 로그인 실패
    }


    @Override
    public int deleteUser(String userId) throws Exception {

        log.info("{}.deleteUser Start!", this.getClass().getName());

        int res = 0;

        Optional<UserInfoEntity> rEntity = userInfoRepository.findByUserId(userId);

        if (rEntity.isPresent()) {
            userInfoRepository.delete(rEntity.get()); //하드 삭제 (DB 에서 완전 삭제)
            res = 1;
        }

        log.info("{}.deleteUser End!", this.getClass().getName());

        return res;
    }

    /**
     * 비밀번호 재변경 메일을 보내기 위한 메일 존재 여부 체크
     *
     *
     */
    @Override
    public boolean existsByEmail(String email) throws Exception {
        log.info("existsByEmail Start!");

        String checkedEmail = CmmUtil.nvl(email); // null 방지
        boolean exists = userInfoRepository.findByEmail(checkedEmail).isPresent();

        log.info("existsByEmail End! exists = {}", exists);
        return exists;
    }

    @Override
    public UserInfoDTO getUserInfo(UserInfoDTO pDTO) throws Exception {
        log.info("getUserInfo Start!");

        String userId = CmmUtil.nvl(pDTO.userId());

        Optional<UserInfoEntity> rEntity = userInfoRepository.findByUserId(userId);

        UserInfoDTO rDTO = rEntity.map(entity -> UserInfoDTO.builder()
                .userId(entity.getUserId())
                .userName(entity.getUserName())
                .email(entity.getEmail())
                .addr1(entity.getAddr1())
                .addr2(entity.getAddr2())
                .build()).orElseGet(() -> UserInfoDTO.builder().build());

        log.info("getUserInfo End!");

        return rDTO;
    }

}
