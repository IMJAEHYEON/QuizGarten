package kopo.poly.service;

import kopo.poly.dto.UserInfoDTO;

public interface IUserInfoService {

    /**
     * 아이디 중복 체크
     *
     * @param pDTO 회원 가입을 위한 아이디
     * @return 아이디 중복 여부 결과
     */

    UserInfoDTO getUserIdExists(UserInfoDTO pDTO) throws Exception;

    /**
     * 이메일 중복 체크
     *
     * @param pDTO 회원 가입을 위한 이메일
     * @return 이메일 중복 여부 결과
     */

    UserInfoDTO getEmailExists(UserInfoDTO pDTO) throws Exception;


    /**
     * 유저닉네임 중복 체크
     *
     * @param pDTO 회원 가입을 위한 유저닉네임
     * @return 유저닉네임 중복 여부 결과
     */
    UserInfoDTO getUserNameExists(UserInfoDTO pDTO) throws Exception;



    UserInfoDTO getUserInfo(UserInfoDTO pDTO) throws Exception;

    /**
     * 회원 가입하기(회원정보 등록하기)
     *
     * @param pDTO 회원 가입을 위한 회원정보
     * @return 회원가입 결과
     */
    int insertUserInfo(UserInfoDTO pDTO) throws Exception;

    /**
     * 로그인을 위해 아이디와 비밀번호가 일치하는지 확인하기
     *
     * @param pDTO 로그인을 위한 회원정보
     * @return 회원가입 결과
     */
    int getUserLogin(UserInfoDTO pDTO) throws Exception;


    /**
     * 회원 탈퇴 (비밀번호 확인 후 진행)
     *
     * @param userId 탈퇴 대상 아이디
     * @return 성공 1 / 실패 0
     */
    int deleteUser(String userId) throws Exception;

    /**
     *
     * 이메일 존재 여부 확인
     *
     * @param email
     * @return
     */
    boolean existsByEmail(String email) throws Exception;
}