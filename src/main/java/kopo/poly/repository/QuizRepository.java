package kopo.poly.repository;

import kopo.poly.repository.entity.QuizEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<QuizEntity, String> {

    // 퀴즈 ID 기준 조회 (PK)
    QuizEntity findByQuizId(String quizId);

    // 사용자의 모든 퀴즈 목록 조회
    List<QuizEntity> findAllByUserId(String userId);

    List<QuizEntity> findAllByVisibility(String visibility);

    // 퀴즈 ID에 해당하는 퀴즈 삭제
    void deleteByQuizId(String quizId);

    // [1] 공개 퀴즈만 가져오기
    Page<QuizEntity> findByVisibilityOrderByCreatedAtDesc(String visibility, Pageable pageable);

    // [2] 특정 유저 퀴즈만 가져오기
    Page<QuizEntity> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

}
