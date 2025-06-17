package kopo.poly.service;

import kopo.poly.dto.MongoQuizDTO;
import kopo.poly.dto.MongoQuizQuestionDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface IMongoQuizService {

    // ======================================
    // 1. MongoDB 저장 및 삭제 관리
    // ======================================

    /**
     * MariaDB에 저장된 퀴즈 데이터를 기반으로
     * MongoDB에 퀴즈 전체(MongoQuizDTO 형태) 저장 (최초 생성 시 사용)
     *
     * @param quizId MariaDB에 저장된 퀴즈 ID
     */
    void makeMongoQuiz(String quizId) throws Exception;

    /**
     * MongoDB에서 퀴즈 전체 문서를 삭제
     *
     * @param quizId 삭제할 퀴즈의 고유 ID
     */
    void deleteMongoQuiz(String quizId) throws Exception;

    // ======================================
    // 2. 퀴즈 전체 조회 및 변환
    // ======================================

    /**
     * 특정 퀴즈를 JSON 문자열 형태로 반환
     * (프론트엔드에서 직접 렌더링할 때 유용)
     *
     * @param quizId 조회할 퀴즈 ID
     * @return JSON 문자열 형태의 퀴즈 정보
     */
    String getMongoQuizJson(String quizId) throws Exception;

    /**
     * 퀴즈에 포함된 전체 문제 리스트를 반환
     * (문제 수정/조회 시 활용)
     *
     * @param quizId 퀴즈 ID
     * @return 해당 퀴즈의 전체 문제 리스트
     */
    List<MongoQuizQuestionDTO> getAllQuizQuestions(String quizId) throws Exception;

    // ======================================
    // 3. 문제 단위 관리 (단일 문제 or 전체 문제)
    // ======================================

    /**
     * 퀴즈 플레이 시, 원하는 개수만큼 문제를 무작위로 추출
     *
     * @param quizId 퀴즈 ID
     * @param count  가져올 문제 수
     * @return 랜덤으로 추출된 문제 리스트
     */
    List<Map<String, Object>> getRandomQuizQuestions(String quizId, int count) throws Exception;

    /**
     * 문제 리스트에 새로운 문제 1개를 추가
     *
     * @param quizId 퀴즈 ID
     * @param newQuestion 추가할 문제 DTO
     */
    void addQuizQuestion(String quizId, MongoQuizQuestionDTO newQuestion, MultipartFile imageFile, MultipartFile audioFile) throws Exception;


    /**
     * 문제 리스트에서 특정 문제 1개를 삭제
     *
     * @param quizId 퀴즈 ID
     * @param questionId 삭제할 문제 ID
     */
    void deleteQuizQuestion(String quizId, String questionId) throws Exception;

    /**
     * 특정 문제 1개만 수정 (문제 ID로 찾아서 교체)
     *
     * @param quizId 퀴즈 ID
     * @param updatedQuestion 수정된 문제 정보 (questionId 포함되어야 함)
     */
    void updateQuizQuestion(String quizId, MongoQuizQuestionDTO updatedQuestion,
                            MultipartFile imageFile, MultipartFile audioFile) throws Exception;

    /**
     * 퀴즈 제목, 설명, 카테고리 등의 기본 정보만 수정
     * (문제 배열은 수정하지 않음)
     *
     * @param quizDTO 수정된 퀴즈 정보 (문제 리스트 제외)
     */
    void updateQuizInfo(MongoQuizDTO quizDTO, MultipartFile thumbnailFile) throws Exception;

    /**
     * 퀴즈 문제 배열 전체를 교체
     * (기존 문제 리스트를 덮어쓰기함)
     *
     * @param quizId 퀴즈 ID
     * @param questions 새롭게 구성된 문제 리스트
     */
    void updateQuizQuestions(String quizId, List<MongoQuizQuestionDTO> questions) throws Exception;

    MongoQuizDTO getMongoQuiz(String quizId) throws Exception;

}
