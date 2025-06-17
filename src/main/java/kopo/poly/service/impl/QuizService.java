package kopo.poly.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import kopo.poly.dto.*;
import kopo.poly.repository.QuizDetailRepository;
import kopo.poly.repository.QuizRepository;
import kopo.poly.repository.entity.QuizDetailEntity;
import kopo.poly.repository.entity.QuizEntity;
import kopo.poly.service.IOpenAiService;
import kopo.poly.service.IQuizDetailService;
import kopo.poly.service.IQuizService;
import kopo.poly.service.IS3Service;
import kopo.poly.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService implements IQuizService {

    private final QuizRepository quizRepository;
    private final QuizDetailRepository quizDetailRepository;
    private final IS3Service s3Service;
    private final IQuizDetailService quizDetailService;
    private final IOpenAiService openAiService;
    private final MongoQuizService mongoQuizService;
    private static final Set<String> runningQuizIds = ConcurrentHashMap.newKeySet();

    /**
     * 퀴즈 정보 MariaDB 저장
     */
    @Override
    @Transactional
    public void saveQuizInfo(QuizDTO pDTO) throws Exception {
        log.info("QuizService.saveQuizInfo Start!");
        log.info("pDTO.userId() : {}", pDTO.userId());
        String now = DateUtil.getDateTime("yyyy-MM-dd HH:mm:ss");

        QuizEntity entity = QuizEntity.builder()
                .quizId(pDTO.quizId())
                .userId(pDTO.userId())
                .quizTitle(pDTO.quizTitle())
                .quizCategory(pDTO.quizCategory())
                .description(pDTO.description())
                .quizType(pDTO.quizType())
                .createdAt(now)
                .quizChgId(pDTO.quizChgId())
                .quizComment(pDTO.quizComment())
                .quizCnt(pDTO.quizCnt())
                .thumbnailUrl(pDTO.thumbnailUrl())
                .visibility(pDTO.visibility())
                .build();

        quizRepository.save(entity);
        log.info("QuizService.saveQuizInfo End!");
    }

    /**
     * 퀴즈 정보 조회
     */
    @Override
    public QuizDTO getQuizInfo(String quizId) throws Exception {
        log.info("QuizService.getQuizInfo Start!");

        QuizEntity entity = quizRepository.findByQuizId(quizId);

        if (entity == null) {
            return null;
        }

        return QuizDTO.builder()
                .quizId(entity.getQuizId())
                .userId(entity.getUserId())
                .quizTitle(entity.getQuizTitle())
                .quizCategory(entity.getQuizCategory())
                .description(entity.getDescription())
                .quizType(entity.getQuizType())
                .createdAt(entity.getCreatedAt())
                .quizChgId(entity.getQuizChgId())
                .quizComment(entity.getQuizComment())
                .quizCnt(entity.getQuizCnt())
                .thumbnailUrl(entity.getThumbnailUrl())
                .visibility(entity.getVisibility())
                .build();
    }

    /**
     * 퀴즈 정보 및 S3 썸네일 삭제
     */
    @Transactional
    public void deleteQuizInfo(String quizId) throws Exception {
        log.info("QuizService.deleteQuizInfo Start!");

        QuizEntity entity = quizRepository.findByQuizId(quizId);

        if (entity != null) {
            String thumbnailUrl = entity.getThumbnailUrl();
            String baseUrl = s3Service.getBaseUrlPrefix();

            if (thumbnailUrl != null && thumbnailUrl.startsWith(baseUrl)) {
                String s3FilePath = s3Service.extractS3FilePath(thumbnailUrl);
                s3Service.delete(s3FilePath);
            }

            quizRepository.delete(entity);
        }

        log.info("QuizService.deleteQuizInfo End!");
    }

    @Override
    public List<QuizListResponseDTO> getPublicQuizList() {
        return quizRepository.findAllByVisibility("Y").stream()
                .map(e -> QuizListResponseDTO.builder()
                        .quizId(e.getQuizId())
                        .title(e.getQuizTitle())
                        .description(e.getDescription())
                        .thumbnailUrl(e.getThumbnailUrl())
                        .quizType(e.getQuizType())
                        .visibility(e.getVisibility())
                        .quizCnt(e.getQuizCnt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<QuizListResponseDTO> getMyQuizList(String userId) {
        return quizRepository.findAllByUserId(userId).stream()
                .map(e -> QuizListResponseDTO.builder()
                        .quizId(e.getQuizId())
                        .title(e.getQuizTitle())
                        .description(e.getDescription())
                        .thumbnailUrl(e.getThumbnailUrl())
                        .quizType(e.getQuizType())
                        .visibility(e.getVisibility())
                        .quizCnt(e.getQuizCnt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void saveQuizDetailInfo(QuizDetailDTO detail, MultipartFile imageFile, String audioUrl) throws Exception {
        QuizDTO quiz = this.getQuizInfo(detail.quizId());
        String quizType = quiz.quizType();

        if ("image".equalsIgnoreCase(quizType)) {
            quizDetailService.saveImageQuizDetail(detail, imageFile);
        } else if ("audio".equalsIgnoreCase(quizType)) {
            quizDetailService.saveAudioQuizDetail(detail, audioUrl);
        } else if ("multiple".equalsIgnoreCase(quizType) || "ai".equalsIgnoreCase(quizType)) {
            quizDetailService.saveMultipleChoiceQuizDetail(detail);
        } else {
            throw new IllegalArgumentException("Unsupported quiz type: " + quizType);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateAiQuiz(AiQuizRequestDTO dto, String userId) throws Exception {

        //  중복 생성 요청 방지
        if (!runningQuizIds.add(dto.quizId())) {
            log.warn("현재 quizId={} 에 대해 이미 생성 중입니다. 요청 무시.", dto.quizId());
            return;
        }

        try {
            log.info("generateAiQuiz Start - quizId: {}, prompt: {}", dto.quizId(), dto.prompt());

            List<QuizDetailDTO> aiQuestions = openAiService.generateQuizFromPrompt(
                    dto.quizId(),
                    dto.prompt(),
                    dto.category()
            );

            // userId 주입
            aiQuestions = aiQuestions.stream()
                    .map(q -> q.toBuilder().userId(userId).build())
                    .toList();

            for (QuizDetailDTO q : aiQuestions) {
                boolean isDuplicate = quizDetailRepository.existsByQuizIdAndQuestionText(dto.quizId(), q.questionText());
                if (isDuplicate) {
                    log.warn("중복된 문제로 저장 생략: {}", q.questionText());
                    continue;
                }

                String quizDetailId = UUID.randomUUID().toString();

                QuizDetailEntity entity = QuizDetailEntity.builder()
                        .quizDetailId(quizDetailId)
                        .quizId(dto.quizId())
                        .userId(userId)
                        .questionText(q.questionText())
                        .quizAnswer(String.join(",", q.quizAnswer()))
                        .quizJson(new ObjectMapper().writeValueAsString(q))
                        .quizCategory(q.quizCategory())
                        .quizMultiAi("Y")
                        .build();

                quizDetailRepository.save(entity);
                log.info("AI 문제 저장됨: {}", quizDetailId);
            }

            mongoQuizService.makeMongoQuiz(dto.quizId());

            log.info("generateAiQuiz End - 문제 수: {}", aiQuestions.size());

        } finally {
            // 생성이 끝난 후 반드시 제거 (에러 발생해도 무조건 해제)
            runningQuizIds.remove(dto.quizId());
        }
    }



    @Override
    public List<QuizDTO> getPublicQuizList(int page, int size) throws Exception {
        log.info("getPublicQuizList Start!");

        Pageable pageable = PageRequest.of(page, size);
        Page<QuizEntity> quizPage = quizRepository.findByVisibilityOrderByCreatedAtDesc("Y", pageable);

        List<QuizDTO> quizList = quizPage.stream().map(entity ->
                QuizDTO.builder()
                        .quizId(entity.getQuizId())
                        .quizTitle(entity.getQuizTitle())
                        .description(entity.getDescription())
                        .quizCategory(entity.getQuizCategory())
                        .quizType(entity.getQuizType())
                        .thumbnailUrl(entity.getThumbnailUrl())
                        .createdAt(entity.getCreatedAt())
                        .visibility(entity.getVisibility())
                        .userId(entity.getUserId())
                        .build()
        ).collect(Collectors.toList());

        return quizList;
    }

    @Override
    public List<QuizDTO> getUserQuizList(String userId, int page, int size) throws Exception {
        log.info("getUserQuizList Start - userId: {}", userId);

        Pageable pageable = PageRequest.of(page, size);
        Page<QuizEntity> quizPage = quizRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<QuizDTO> quizList = quizPage.stream().map(entity ->
                QuizDTO.builder()
                        .quizId(entity.getQuizId())
                        .quizTitle(entity.getQuizTitle())
                        .description(entity.getDescription())
                        .quizCategory(entity.getQuizCategory())
                        .quizType(entity.getQuizType())
                        .thumbnailUrl(entity.getThumbnailUrl())
                        .createdAt(entity.getCreatedAt())
                        .visibility(entity.getVisibility())
                        .userId(entity.getUserId())
                        .build()
        ).collect(Collectors.toList());

        return quizList;
    }

}
