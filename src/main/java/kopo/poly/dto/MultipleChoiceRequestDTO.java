package kopo.poly.dto;


import lombok.Builder;

import java.util.List;

@Builder(toBuilder = true)
public record MultipleChoiceRequestDTO(
        String quizDetailId,
        String quizId,
        String questionText, // 퀴즈문제 내용
        List<String> choices, // 선택지 총 4개
        List<String> answers, // 정답을 리스트로 처리 (1개만 선택)
        String quizCategory // 퀴즈 카테고리
) {
}
