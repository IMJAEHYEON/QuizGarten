package kopo.poly.dto;

import lombok.Builder;

import java.util.List;


@Builder(toBuilder = true)
public record QuizDetailDTO(


        // 퀴즈 상세 정보 ID , DB 연동 및 문제 단위 추적
        String quizDetailId, // 퀴즈 상세정보 ID
        String quizId, // 퀴즈 고유 ID (FK)
        String userId, // 퀴즈 작성자 ID

        // 퀴즈 공통 정보 , MariaDB + MongoDB 동시 저장 구조 대응
        String quizCategory, // AI 퀴즈 제작시 퀴즈 주제 ex) 동물, 과자
        String quizJson, // MariaDB 저장용 JSON 구조 복수형 퀴즈 데이터
        String quizMongo, // MongoDB Object ID

        // 이미지 퀴즈
        String awsImageUrl, // AWS S3에 저장된 이미지 URL (전체이미지,정답이미지 표시할때 사용)
        String awsImageCut, // 잘린 사진 퀴즈에서 사용할 프론트엔드에서 잘라낸 이미지 좌표 JSON

        //오디오 퀴즈
        String awsAudioUrl,// AWS S3에 저장된 오디오 URL
        String audioStartTime, // 영상 퀴즈 시작 시점 (Youtube Player API 활용)
        String audioEndTime, // 영상 퀴즈 종료 시점 (Youtube Player API 활용)
        List<String> choices,
        // AI 객관식 퀴즈
        String questionText,
        String quizMultiAi, // 객관식 퀴즈 AI를 활용한 문제인지 확인
        // 퀴즈 공통 정답. 문자열형 or JSON 배열 가능
        String quizAnswer

) {
}
