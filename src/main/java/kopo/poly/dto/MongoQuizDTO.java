package kopo.poly.dto;

import lombok.Builder;

import java.util.List;

@Builder(toBuilder = true)
public record MongoQuizDTO(
        String quizId,                           // 퀴즈 고유 ID (MariaDB 연동용)
        String title,                            // 퀴즈 제목
        String description,                      // 퀴즈 설명
        String authorId,                         // 퀴즈 제작자 ID
        String createdAt,                        // 생성일자
        List<MongoQuizQuestionDTO> questions,    // 문제 리스트

        String quizType,                         // 퀴즈 타입 (image, audio, multiple)
        String category,                         // 퀴즈 카테고리 (ex. 동물, 과자 AI 퀴즈 제작시에만 사용)
        String storedInMongo,                     // MongoDB 저장 여부 플래그 ("Y", "N")
        String thumbnailUrl,                      // 대표 이미지 썸네일 (main 페이지 카드에 사용)
        String visibility                       // 공개,비공개 설정 (Public / Private)

) {}
