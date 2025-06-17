package kopo.poly.dto;

import lombok.Builder;

@Builder(toBuilder = true)
public record QuizDTO(
        String quizId, // 퀴즈 고유 아이디
        String userId, // 퀴즈 제작자 ID (작성자)
        String quizTitle, // 퀴즈 제목
        String quizCategory, // 퀴즈 카테고리 메타데이터 저장
        String description, // 퀴즈 설명
        String quizType, // 퀴즈 타입 설정 (QuizDetail - > Quiz)
        String createdAt, // 퀴즈 등록일 (생성 시 null -> 응답시 자동 채움)
        String quizChgId, // 최종 수정자 ID (관리용)
        String quizComment, // 퀴즈 댓글
        String thumbnailUrl,                      // 대표 이미지 썸네일 (main 페이지 카드에 사용)
        String visibility,                       // 공개,비공개 설정 (Public / Private)
        int quizCnt // 퀴즈 조회수



) {
}
