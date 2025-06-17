package kopo.poly.dto;


import lombok.Builder;

import java.util.List;

@Builder(toBuilder = true)
public record AiQuizRequestDTO(

        String quizId,
        String quizDetailId,
        String quizType,
        String category,
        String questionText,
        String prompt,
        List<String> choices,
        List<String> answers
) {
}
