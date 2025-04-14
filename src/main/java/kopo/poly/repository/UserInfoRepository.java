package kopo.poly.repository;

import kopo.poly.repository.entity.UserInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserInfoRepository extends JpaRepository<UserInfoEntity, String> {

    Optional<UserInfoEntity> findByUserId(String userId);

    Optional<UserInfoEntity> findByUserIdAndPassword(String userId, String password);
}
