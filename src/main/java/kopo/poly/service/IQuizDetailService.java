package kopo.poly.service;

import kopo.poly.dto.AudioQuizRequestDTO;
import kopo.poly.dto.QuizDetailDTO;
import kopo.poly.repository.entity.QuizDetailEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IQuizDetailService {

    // 이미지 파일 , 오디오 URL , 객관식 타입간 개별 퀴즈 문제 저장
    void saveImageQuizDetail(QuizDetailDTO detail, MultipartFile imageFile) throws Exception;

    void saveAudioQuizDetail(QuizDetailDTO detail, String audioUrl) throws Exception;

    void saveMultipleChoiceQuizDetail(QuizDetailDTO detail) throws Exception;

    void deleteQuizDetailsByQuizId(String quizId) throws Exception;

    //주어진 퀴즈 ID로 모든 문제를 조회
    List<QuizDetailDTO> getQuizDetailsByQuizId(String quizId) throws Exception;


    // 퀴즈 문제 조회
    // QuizDetailDTO getQuizDetailInfo(String quizDetailId) throws Exception;
}
