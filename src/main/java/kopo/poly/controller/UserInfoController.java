package kopo.poly.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import kopo.poly.dto.MsgDTO;
import kopo.poly.dto.PasswordChangeDTO;
import kopo.poly.dto.PasswordResetDTO;
import kopo.poly.dto.UserInfoDTO;
import kopo.poly.service.IPasswordResetService;
import kopo.poly.service.IUserInfoService;
import kopo.poly.util.CmmUtil;
import kopo.poly.util.EncryptUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RequestMapping(value = "/user")
@RequiredArgsConstructor
@Controller
public class UserInfoController {

    private final IUserInfoService userInfoService;

    private final IPasswordResetService passwordResetService;


    /**
     * 회원가입 화면으로 이동
     */
    @GetMapping(value = "/userRegForm")
    public String userRegForm() {
        log.info("{}.user/userRegForm Start!", this.getClass().getName());

        log.info("{}.user/userRegForm End!", this.getClass().getName());

        return "user/userRegForm";
    }


    /**
     * 회원 가입 전 아이디 중복체크하기(Ajax 를 통해 입력한 아이디 정보 받음)
     */
    @ResponseBody
    @PostMapping(value = "getUserIdExists")
    public UserInfoDTO getUserExists(HttpServletRequest request) throws Exception {

        log.info("{}.getUserIdExists Start!", this.getClass().getName());

        String userId = CmmUtil.nvl(request.getParameter("userId")); // 회원아이디

        log.info("userId : {}", userId);

        // Builder 통한 값 저장
        UserInfoDTO pDTO = UserInfoDTO.builder().userId(userId).build();

        // 회원아이디를 통해 중복된 아이디인지 조회
        UserInfoDTO rDTO = Optional.ofNullable(userInfoService.getUserIdExists(pDTO))
                .orElseGet(() -> UserInfoDTO.builder().build());

        log.info("{}.getUserIdExists End!", this.getClass().getName());

        return rDTO;
    }

    /**
     * 회원 가입 전 이메일 중복체크하기(Ajax 를 통해 입력한 아이디 정보 받음)
     */
    @ResponseBody
    @PostMapping(value = "getEmailExists")
    public UserInfoDTO getEmailExists(HttpServletRequest request) throws Exception {

        log.info("{}.getEmailExists Start!", this.getClass().getName());

        String email = CmmUtil.nvl(request.getParameter("email")); // 이메일 입력값

        log.info("email : {}", email);

        // Builder 통한 값 저장
        UserInfoDTO pDTO = UserInfoDTO.builder().email(email).build();

        // 이메일로 사용자 존재 여부 조회
        UserInfoDTO rDTO = Optional.ofNullable(userInfoService.getEmailExists(pDTO))
                .orElseGet(() -> UserInfoDTO.builder().build());

        log.info("{}.getEmailExists End!", this.getClass().getName());

        return rDTO;
    }

    @ResponseBody
    @PostMapping(value = "getUserNameExists")
    public UserInfoDTO getUserNameExists(HttpServletRequest request) throws Exception {
        log.info("{}.getUserNameExists Start!", this.getClass().getName());

        String userName = CmmUtil.nvl(request.getParameter("userName"));

        UserInfoDTO pDTO = UserInfoDTO.builder().userName(userName).build();

        UserInfoDTO rDTO = Optional.ofNullable(userInfoService.getUserNameExists(pDTO))
                .orElseGet(() -> UserInfoDTO.builder().build());

        log.info("{}.getUserNameExists End!", this.getClass().getName());

        return rDTO;
    }


    /**
     * 회원가입 로직 처리
     */
    @ResponseBody
    @PostMapping(value = "insertUserInfo")
    public MsgDTO insertUserInfo(HttpServletRequest request) throws Exception {

        log.info("{}.insertUserInfo start!", this.getClass().getName());

        String msg; //회원가입 결과에 대한 메시지를 전달할 변수

        String userId = CmmUtil.nvl(request.getParameter("userId")); //아이디
        String userName = CmmUtil.nvl(request.getParameter("userName")); //이름
        String password = CmmUtil.nvl(request.getParameter("password")); //비밀번호
        String email = CmmUtil.nvl(request.getParameter("email")); //이메일
        String addr1 = CmmUtil.nvl(request.getParameter("addr1")); //주소
        String addr2 = CmmUtil.nvl(request.getParameter("addr2")); //상세주소

        log.info("userId : {}, userName : {}, password : {}, email : {}, addr1 : {}, addr2 : {}",
                userId, userName, password, email, addr1, addr2);

        UserInfoDTO pDTO = UserInfoDTO.builder()
                .userId(userId)
                .userName(userName)
                .password(EncryptUtil.encHashSHA256(password))
                .email(EncryptUtil.encAES128CBC(email))
                .addr1(addr1)
                .addr2(addr2)
                .regId(userId)
                .chgId(userId)
                .build();

        int res = userInfoService.insertUserInfo(pDTO);

        log.info("회원가입 결과(res) : {}", res);

        if (res == 1) {
            msg = "회원가입되었습니다.";
        } else if (res == 2) {
            msg = "이미 가입된 아이디입니다.";
        } else {
            msg = "오류로 인해 회원가입이 실패하였습니다.";
        }

        MsgDTO dto = MsgDTO.builder().result(res).msg(msg).build();

        log.info("{}.insertUserInfo End!", this.getClass().getName());

        return dto;
    }

    /**
     * 로그인을 위한 입력 화면으로 이동
     */
    @GetMapping(value = "login")
    public String login() {
        log.info("{}.user/login Start!", this.getClass().getName());

        log.info("{}.user/login End!", this.getClass().getName());

        return "user/login";
    }


    /**
     * 로그인 처리 및 결과 알려주는 화면으로 이동
     */
    @ResponseBody
    @PostMapping(value = "loginProc")
    public MsgDTO loginProc(HttpServletRequest request, HttpSession session) throws Exception {

        log.info("{}.loginProc Start!", this.getClass().getName());

        String msg; //로그인 결과에 대한 메시지를 전달할 변수

        String userId = CmmUtil.nvl(request.getParameter("userId")); // 아이디
        String password = CmmUtil.nvl(request.getParameter("password")); //비밀번호

        log.info("user_id : {}, password : {}", userId, password);

        UserInfoDTO pDTO = UserInfoDTO.builder()
                .userId(userId)
                .password(EncryptUtil.encHashSHA256(password)).build();

        int res = userInfoService.getUserLogin(pDTO);

        log.info("res : {}", res);

        if (res == 1) { //로그인 성공
            msg = "로그인이 성공했습니다.";
            session.setAttribute("SS_USER_ID", userId);
        } else {
            msg = "아이디와 비밀번호가 올바르지 않습니다.";
        }

        MsgDTO dto = MsgDTO.builder().result(res).msg(msg).build();
        log.info("{}.loginProc End!", this.getClass().getName());

        return dto;
    }

    /**
     * 로그인 성공 페이지 이동
     */
    @GetMapping(value = "mainLoginSuccess")
    public String loginSuccess() {
        log.info("{}.main/loginSuccess Start!", this.getClass().getName());

        log.info("{}.main/loginSuccess End!", this.getClass().getName());

        return "main/mainLoginSuccess";
    }

    @ResponseBody
    @PostMapping("/getUserInfo")
    public UserInfoDTO getUserInfo(HttpSession session) throws Exception {
        log.info("getUserInfo Start!");

        String userId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));
        log.info("세션에서 가져온 SS_USER_ID : {}", userId);

        UserInfoDTO pDTO = UserInfoDTO.builder().userId(userId).build();

        UserInfoDTO rDTO = Optional.ofNullable(userInfoService.getUserInfo(pDTO))
                .orElseGet(() -> UserInfoDTO.builder().userName("사용자").build());

        log.info("getUserInfo End!");

        return rDTO;
    }


    /**
     * 로그아웃 처리하기
     */
    @ResponseBody
    @PostMapping(value = "logout")
    public MsgDTO logout(HttpSession session) {

        log.info("{}.logout Start!", this.getClass().getName());

        session.setAttribute("SS_USER_ID", "");
        session.removeAttribute("SS_USER_ID");

        MsgDTO dto = MsgDTO.builder().result(1).msg("로그아웃하였습니다").build();

        log.info("{}.logout End!", this.getClass().getName());

        return dto;
    }

    /**
     * 회원 탈퇴 시 비밀번호 재입력 확인 후 탈퇴 결정
     */
    @ResponseBody
    @PostMapping(value = "deleteUserSecure")
    public MsgDTO deleteUserSecure(HttpServletRequest request, HttpSession session) throws Exception {

        log.info("{}.deleteUserSecure Start!", this.getClass().getName());

        String userId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));
        String password = CmmUtil.nvl(request.getParameter("password"));

        // 비밀번호 암호화 후 DTO 구성
        UserInfoDTO pDTO = UserInfoDTO.builder()
                .userId(userId)
                .password(EncryptUtil.encHashSHA256(password))
                .build();

        // 로그인 확인 (아이디 + 비밀번호)
        int loginCheck = userInfoService.getUserLogin(pDTO);

        String msg;
        int res = 0;

        if (loginCheck == 1) {
            // 비밀번호 일치 시 회원탈퇴 진행
            res = userInfoService.deleteUser(userId);
            msg = (res == 1) ? "회원 탈퇴가 완료되었습니다." : "회원 탈퇴에 실패하였습니다.";

            // 세션 초기화
            session.removeAttribute("SS_USER_ID");
        } else {
            msg = "비밀번호가 일치하지 않습니다.";
        }

        log.info("{}.deleteUserSecure End!", this.getClass().getName());

        return MsgDTO.builder().result(res).msg(msg).build();

    }

    @GetMapping(value = "findPassword")
    public String findPasswordPage() {
        log.info("findPasswordPage Start!");

        log.info("findPasswordPage End!");
        return "user/findPassword"; // templates/password/findPassword.html
    }

    /**
     * 비밀번호 재설정 - 임시 비밀번호 발송
     */
    @ResponseBody
    @PostMapping(value = "findPassword")
    public MsgDTO findPassword(@RequestBody PasswordResetDTO pDTO) throws Exception {

        log.info("{}.findPassword Start!", this.getClass().getName());

        int res = 0;
        String msg;

        boolean result = passwordResetService.sendTempPassword(pDTO);

        if (result) {
            res = 1;
            msg = "임시 비밀번호가 이메일로 발송되었습니다.";
        } else {
            msg = "입력하신 정보와 일치하는 회원이 없습니다.";
        }

        log.info("{}.findPassword End!", this.getClass().getName());

        return MsgDTO.builder().result(res).msg(msg).build();
    }

    /**
     * 비밀번호 변경 페이지 이동 (로그인 필수)
     */
    @GetMapping(value = "changePassword")
    public String changePasswordPage(HttpSession session) {
        log.info("changePasswordPage Start!");

        String userId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));
        log.info("세션 userId : {}", userId); //

        if (userId == null || userId.isEmpty()) {
            log.info("로그인 필요 - 세션 없음");
            return "redirect:/user/login"; // 세션 없으면 로그인 페이지로 리다이렉트
        }

        log.info("changePasswordPage End!");
        return "user/changePassword"; // 비밀번호 변경 HTML 페이지
    }

    /**
     * 비밀번호 변경 페이지 접근 권한 확인 (로그인 필수)
     */
    @ResponseBody
    @GetMapping(value = "changePasswordCheck")
    public MsgDTO changePasswordCheck(HttpSession session) {
        log.info("changePasswordCheck Start!");

        String userId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));
        log.info("세션 userId : {}", userId); //
        int res;
        String msg;

        if (userId == null || userId.isEmpty()) {
            res = 0;
            msg = "로그인 후 사용 가능한 기능입니다.";
        } else {
            res = 1;
            msg = "비밀번호 변경 페이지로 이동합니다.";
        }

        log.info("changePasswordCheck End!");
        return MsgDTO.builder().result(res).msg(msg).build();
    }

    @ResponseBody
    @PostMapping(value = "changePassword")
    public MsgDTO changePassword(HttpSession session, @RequestBody PasswordChangeDTO pDTO) throws Exception {
        log.info("changePassword Start!");

        String userId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));
        int res = 0;
        String msg;

        if (userId == null || userId.isEmpty()) {
            msg = "로그인이 필요합니다.";
        } else {
            PasswordChangeDTO dto = PasswordChangeDTO.builder()
                    .userId(userId)
                    .password(pDTO.password())
                    .build();

            boolean result = passwordResetService.changePassword(dto);

            if (result) {
                res = 1;
                msg = "비밀번호가 변경되었습니다.";
            } else {
                msg = "비밀번호 변경에 실패했습니다.";
            }
        }

        log.info("changePassword End!");
        return MsgDTO.builder().result(res).msg(msg).build();
    }
}