package kopo.poly.service;

import kopo.poly.dto.QuizDetailDTO;

import java.util.List;

public interface IOpenAiService {

    List<QuizDetailDTO> generateQuizFromPrompt(String quizId, String prompt, String category) throws Exception;


}
