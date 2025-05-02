package kopo.poly.repository;

import kopo.poly.repository.entity.UserInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserInfoRepository extends JpaRepository<UserInfoEntity, String> {

    Optional<UserInfoEntity> findByUserId(String userId); // DB 안에 존재하는 유저 아이디 확인

    Optional<UserInfoEntity> findByUserIdAndPassword(String userId, String password); // 로그인을 위한 DB 안에 존재하는 아이디, 비밀번호 찾기

    Optional<UserInfoEntity> findByEmail(String email); // DB 안에 존재하는 동일한 이메일 찾기

    Optional<UserInfoEntity> findByUserName(String userName); // DB 안에 존재하는 동일한 유저 이름 찾기

    Optional<UserInfoEntity> findByUserIdAndEmail(String userId, String email); // 임시비밀번호 발급에 필요한 유저아이디, 이메일 정보

}
