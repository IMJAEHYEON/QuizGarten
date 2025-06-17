package kopo.poly.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Entity
@Builder(toBuilder = true)
@Table(name = "QUIZ_DETAIL")
public class QuizDetailEntity {

    @Id
    @Column(name = "QUIZ_DETAIL_ID", length = 100)
    private String quizDetailId;

    @Column(name = "QUIZ_ID", length = 100, nullable = false)
    private String quizId;

    @Column(name = "USER_ID", length = 100, nullable = false)
    private String userId;


    // 공통 정보

    @Column(name = "QUIZ_CATEGORY", length = 100)
    private String quizCategory;

    @Lob
    @Column(name = "QUIZ_JSON", columnDefinition = "TEXT")
    private String quizJson;

    @Column(name = "QUIZ_MONGO", length = 100)
    private String quizMongo;


    // 이미지 퀴즈
    @Column(name = "AWS_IMAGE_URL", length = 2500)
    private String awsImageUrl;

    @Column(name = "AWS_IMAGE_CUT", length = 2500)
    private String awsImageCut;


    // 오디오 퀴즈
    @Column(name = "AWS_AUDIO_URL", length = 2500)
    private String awsAudioUrl;

    @Column(name = "QUIZ_AUDIO_START_TIME", length = 100)
    private String audioStartTime;

    @Column(name = "QUIZ_AUDIO_END_TIME", length = 100)
    private String audioEndTime;


    // 객관식 퀴즈문제 설명
    @Column(name = "QUIZ_QUESTION_TEXT")
    private String questionText;
    // 객관식 AI 생성 여부
    @Column(name = "QUIZ_MULTI_AI", length = 100)
    private String quizMultiAi;

    // 퀴즈 정답
    @Column(name = "QUIZ_ANSWER", length = 1000)
    private String quizAnswer;

    // ...필요 시 상태 변경 메서드...

}
