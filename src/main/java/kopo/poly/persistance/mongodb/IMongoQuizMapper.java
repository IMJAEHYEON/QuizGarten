package kopo.poly.persistance.mongodb;

import kopo.poly.dto.MongoQuizDTO;
import kopo.poly.dto.MongoQuizQuestionDTO;

import java.util.List;

public interface IMongoQuizMapper {

    // MongoDB에 퀴즈 전체 저장 (insert용)
    void insertQuiz(MongoQuizDTO pDTO) throws Exception;

    // 퀴즈ID가 없으면 생성, 있으면 업데이트
    void upsertQuiz(MongoQuizDTO pDTO) throws Exception;

    // quizId로 퀴즈 조회
    MongoQuizDTO getQuizById(String quizId) throws Exception;

    // quizId로 퀴즈 삭제
    void deleteQuizById(String quizId) throws Exception;

    // 퀴즈 ID로 전체 문제 리스트 반환
    List<MongoQuizQuestionDTO> getAllQuizQuestions(String quizId) throws Exception;

    // quizId로 기존 퀴즈 전체 업데이트 (questions 포함 전체 대체)
    void updateQuiz(MongoQuizDTO pDTO) throws Exception;


    // 퀴즈 기본 정보 수정
    void updateQuizInfo(MongoQuizDTO quizDTO) throws Exception;


    // 퀴즈 문제 전체 덮어쓰기
    void updateQuizQuestions(String quizId, List<MongoQuizQuestionDTO> questions) throws Exception;


    // 특정 퀴즈에서 특정 문제 삭제
    void deleteQuizQuestion(String quizId, String questionId) throws Exception;


}
