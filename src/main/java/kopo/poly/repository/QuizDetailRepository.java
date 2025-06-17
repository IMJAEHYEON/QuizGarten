package kopo.poly.repository;

import kopo.poly.repository.entity.QuizDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizDetailRepository extends JpaRepository<QuizDetailEntity, String> {

    // 퀴즈 ID로 상세 목록 조회
    List<QuizDetailEntity> findAllByQuizId(String quizId);

    // 특정 사용자가 만든 특정 퀴즈 상세 정보 목록
    List<QuizDetailEntity> findAllByQuizIdAndUserId(String quizId, String userId);

    QuizDetailEntity findByQuizDetailId(String quizDetailId);

    List<QuizDetailEntity> findByQuizId(String quizId);

    // 퀴즈 ID에 해당하는 모든 문제 삭제
    void deleteAllByQuizId(String quizId);

    void deleteByQuizIdAndQuizDetailId(String quizId, String quizDetailId);

    QuizDetailEntity findByQuizIdAndQuizDetailId(String quizId, String questionId);

    boolean existsByQuizIdAndQuestionText(String quizId, String questionText);
}
