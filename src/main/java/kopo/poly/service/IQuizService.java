package kopo.poly.service;

import kopo.poly.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IQuizService {



    // 퀴즈 저장
    void saveQuizInfo(QuizDTO pDTO) throws Exception;

    // 퀴즈 ID로 상세 조회
    QuizDTO getQuizInfo(String quizId) throws Exception;

    List<QuizListResponseDTO> getPublicQuizList() throws Exception;

    List<QuizListResponseDTO> getMyQuizList(String userId) throws Exception;

    void saveQuizDetailInfo(QuizDetailDTO detail, MultipartFile imageFile, String audioUrl) throws Exception;

    void generateAiQuiz(AiQuizRequestDTO dto, String userId) throws Exception;

    // 메인페이지 생성시간 기준 전체 퀴즈 리스트 표시
    List<QuizDTO> getPublicQuizList(int page, int size) throws Exception;

    // 마이페이지 유저아이디 기준 퀴즈 리스트 표시
    List<QuizDTO> getUserQuizList(String userId, int page, int size) throws Exception;

}
