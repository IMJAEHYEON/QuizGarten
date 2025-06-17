package kopo.poly.dto;

import lombok.Builder;

@Builder(toBuilder = true)
public record QuizListResponseDTO(
        String quizId,
        String title,
        String description,
        String thumbnailUrl,
        String quizType,
        String visibility,
        int quizCnt // 조회수
) {
}
