package kopo.poly.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name="QUIZ")
public class QuizEntity {

    @Id
    @Column(name = "QUIZ_ID", length = 100)
    private String quizId; // 퀴즈 고유 ID (PK)

    @Column(name = "USER_ID", length = 100, nullable = false)
    private String userId; // 퀴즈 제작자 ID

    @Column(name = "QUIZ_TITLE", length = 1000, nullable = false)
    private String quizTitle; // 퀴즈 제목

    @Column(name = "QUIZ_CATEGORY")
    private String quizCategory;

    @Column(name = "DESCRIPTION", length = 4000)
    private String description; // 퀴즈 설명

    @Column(name = "QUIZ_TYPE", length = 100, nullable = false)
    private String quizType; // 퀴즈 유형 (IMAGE, AUDIO, MULTIPLE, AI)

    @Column(name = "CREATED_AT", length = 100)
    private String createdAt; // 등록일 (생성 시 null → 응답 시 채움)

    @Column(name = "QUIZ_CHG_ID", length = 100)
    private String quizChgId; // 최종 수정자 ID

    @Column(name = "QUIZ_COM", length = 4000)
    private String quizComment; // 퀴즈 댓글

    @Column(name = "QUIZ_CNT")
    private int quizCnt; // 퀴즈 조회수

    @Column(name = "THUMBNAIL_URL")
    private String thumbnailUrl;

    @Column(name = "VISIBILITY")
    private String visibility;  // "PUBLIC" or "PRIVATE"
    // ...필요 시 상태 변경 메서드...

}
