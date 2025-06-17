package kopo.poly.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY) // null과 빈 리스트 모두 저장 대상
@Builder(toBuilder = true)
public record MongoQuizQuestionDTO(
        String quizDetailId,         // 문제 고유 ID (MariaDB 기준 PK와 통일)
        String imageUrl,             // 이미지 퀴즈일 경우 (문제용 전체 이미지)
        String audioUrl,             // 오디오 퀴즈일 경우

        List<String> choices,        // 객관식 선택지 (객관식 퀴즈일 경우만)
        String questionText,          // 객관식 퀴즈 문제 설명 (객관식 퀴즈일 경우만)
        List<String> answers,               // 복수 정답 입력지원

        String quizImageCut,         // 이미지 퀴즈용 잘린 이미지 좌표 JSON (optional)
        String audioStartTime,       // 오디오 퀴즈 시작 시간
        String audioEndTime          // 오디오 퀴즈 종료 시간
) {}
