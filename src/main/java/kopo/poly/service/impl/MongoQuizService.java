package kopo.poly.service.impl;

import static kopo.poly.util.DateUtil.getDateTime;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kopo.poly.dto.MongoQuizDTO;
import kopo.poly.dto.MongoQuizQuestionDTO;
import kopo.poly.dto.QuizDetailDTO;
import kopo.poly.persistance.mongodb.IMongoQuizMapper;
import kopo.poly.repository.QuizDetailRepository;
import kopo.poly.repository.QuizRepository;
import kopo.poly.repository.entity.QuizDetailEntity;
import kopo.poly.repository.entity.QuizEntity;
import kopo.poly.service.IMongoQuizService;
import kopo.poly.service.IQuizDetailService;
import kopo.poly.service.IS3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class MongoQuizService implements IMongoQuizService {

    private final QuizRepository quizRepository;
    private final QuizDetailRepository quizDetailRepository;
    private final IQuizDetailService quizDetailService;
    private final IMongoQuizMapper mongoQuizMapper;
    private final ObjectMapper objectMapper;
    private final IS3Service s3Service;

    @Override
    @Transactional
    public void makeMongoQuiz(String quizId) throws Exception {
        log.info("{}.makeMongoQuiz Start!", this.getClass().getName());

        // 1. 퀴즈 정보 조회
        Optional<QuizEntity> quizOpt = Optional.ofNullable(quizRepository.findByQuizId(quizId));
        if (quizOpt.isEmpty()) {
            log.warn("Quiz not found for quizId: {}", quizId);
            return;
        }

        QuizEntity quiz = quizOpt.get();

        // 2. 문제 리스트 조회
        List<QuizDetailEntity> detailList = quizDetailRepository.findAllByQuizId(quizId);
        if (detailList == null) detailList = Collections.emptyList();

        // 3. 문제 리스트 변환 (오류나 null 방지)
        List<MongoQuizQuestionDTO> newQuestions = detailList.stream()
                .map(detail -> {
                    try {
                        return convertToMongoQuestion(detail);
                    } catch (Exception e) {
                        log.error("문제 변환 실패 - quizDetailId: {}", detail.getQuizDetailId(), e);
                        return null; // 실패한 항목은 제외
                    }
                })
                .filter(Objects::nonNull)
                .toList();

        // 4. MongoQuizDTO 구성
        MongoQuizDTO.MongoQuizDTOBuilder builder = MongoQuizDTO.builder()
                .quizId(quiz.getQuizId())
                .title(quiz.getQuizTitle())
                .description(quiz.getDescription())
                .createdAt(quiz.getCreatedAt())
                .authorId(quiz.getUserId())
                .storedInMongo(!newQuestions.isEmpty() ? "Y" : "N")
                .visibility(quiz.getVisibility())
                .quizType(quiz.getQuizType())
                .questions(newQuestions);

        // 5. 썸네일 및 카테고리 처리
        if (!detailList.isEmpty() && detailList.get(0).getAwsImageUrl() != null) {
            builder.thumbnailUrl(detailList.get(0).getAwsImageUrl());
            builder.category(detailList.get(0).getQuizCategory());
        } else {
            builder.thumbnailUrl(quiz.getThumbnailUrl());
            builder.category(null);
        }

        // 6. MongoDB 저장
        mongoQuizMapper.upsertQuiz(builder.build());

        log.info("{}.makeMongoQuiz End!", this.getClass().getName());
    }


    private MongoQuizQuestionDTO convertToMongoQuestion(QuizDetailEntity detail) {
        try {
            Map<String, Object> quizJsonMap = objectMapper.readValue(detail.getQuizJson(), new TypeReference<>() {});

            // 1. 객관식 문제 설명
            String questionText = (String) quizJsonMap.getOrDefault("questionText", "");

            // 2. 객관식 선택지
            List<String> choices;
            if (quizJsonMap.containsKey("choices") && quizJsonMap.get("choices") instanceof List<?> rawList) {
                choices = rawList.stream().map(Object::toString).toList();
            } else {
                choices = List.of();
            }

            // 3. 정답
            List<String> answers;
            Object rawAnswer = quizJsonMap.containsKey("answers")
                    ? quizJsonMap.get("answers")
                    : quizJsonMap.getOrDefault("correctAnswer", detail.getQuizAnswer());

            if (rawAnswer instanceof List<?>) {
                answers = ((List<?>) rawAnswer).stream().map(Object::toString).toList();
            } else if (rawAnswer instanceof String str) {
                answers = List.of(str.split(","));
            } else {
                answers = List.of();
            }

            // 4. 오디오 URL 처리 (유튜브 링크만 해당)
            String audioUrl = null;
            if (detail.getAwsAudioUrl() != null && !detail.getAwsAudioUrl().isBlank()) {
                audioUrl = s3Service.readYoutubeLinkFromS3(detail.getAwsAudioUrl());
            }

            return MongoQuizQuestionDTO.builder()
                    .quizDetailId(detail.getQuizDetailId())
                    .imageUrl(detail.getAwsImageUrl())
                    .audioUrl(audioUrl)
                    .choices(choices)
                    .answers(answers)
                    .questionText(questionText)
                    .quizImageCut(detail.getAwsImageCut())
                    .audioStartTime(detail.getAudioStartTime())
                    .audioEndTime(detail.getAudioEndTime())
                    .build();

        } catch (Exception e) {
            log.error("{}.convertToMongoQuestion Error!", this.getClass().getName(), e);

            List<String> fallbackAnswers = detail.getQuizAnswer() != null
                    ? List.of(detail.getQuizAnswer().split(","))
                    : List.of();

            return MongoQuizQuestionDTO.builder()
                    .quizDetailId(detail.getQuizDetailId())
                    .imageUrl(detail.getAwsImageUrl())
                    .audioUrl(detail.getAwsAudioUrl()) // fallback은 S3 URL 그대로 사용
                    .choices(List.of())
                    .answers(fallbackAnswers)
                    .questionText("")
                    .quizImageCut(detail.getAwsImageCut())
                    .audioStartTime(detail.getAudioStartTime())
                    .audioEndTime(detail.getAudioEndTime())
                    .build();
        }
    }

    @Override
    public String getMongoQuizJson(String quizId) throws Exception {
        log.info("{}.getMongoQuizJson Start!", this.getClass().getName());
        MongoQuizDTO dto = mongoQuizMapper.getQuizById(quizId);
        if (dto == null) return null;
        return objectMapper.writeValueAsString(dto);
    }

    @Override
    public List<Map<String, Object>> getRandomQuizQuestions(String quizId, int count) throws Exception {
        log.info("{}.getRandomQuizQuestions Start! quizId: {}, count: {}", this.getClass().getName(), quizId, count);

        if (count <= 0) {
            log.warn("요청된 문제 수가 0 이하입니다. count: {}", count);
            return Collections.emptyList();
        }

        MongoQuizDTO dto = mongoQuizMapper.getQuizById(quizId);

        if (dto == null || dto.questions() == null) {
            log.warn("퀴즈 또는 문제 목록이 존재하지 않습니다. quizId: {}", quizId);
            return Collections.emptyList();
        }

        List<MongoQuizQuestionDTO> questions = new ArrayList<>(dto.questions());
        Collections.shuffle(questions);

        List<MongoQuizQuestionDTO> selected = questions.size() <= count ? questions : questions.subList(0, count);


        return selected.stream().map(q -> {
            Map<String, Object> map = new HashMap<>();
            map.put("quizDetailId", q.quizDetailId());
            map.put("imageUrl", q.imageUrl());
            map.put("audioUrl", q.audioUrl());
            map.put("choices", q.choices());
            map.put("questionText", q.questionText());
            map.put("answers", q.answers());
            map.put("quizAnswer", String.join(",", q.answers()));
            map.put("quizImageCut", q.quizImageCut());
            map.put("audioStartTime", q.audioStartTime());
            map.put("audioEndTime", q.audioEndTime());
            return map;
        }).collect(Collectors.toList());
    }



    @Override
    @Transactional
    public void addQuizQuestion(String quizId, MongoQuizQuestionDTO newQuestion,
                                MultipartFile imageFile, MultipartFile audioFile) throws Exception {
        log.info("{}.addQuizQuestion Start!", this.getClass().getName());
        MongoQuizDTO quizDTO = mongoQuizMapper.getQuizById(quizId);
        if (quizDTO == null) {
            log.warn("Quiz not found: {}", quizId);
            return;
        }
        List<MongoQuizQuestionDTO> updatedQuestions = new ArrayList<>(quizDTO.questions());
        updatedQuestions.add(newQuestion);
        updateQuizQuestionsAndStatus(quizDTO, updatedQuestions);
        log.info("{}.addQuizQuestion End!", this.getClass().getName());
    }

    @Override
    @Transactional
    public void deleteMongoQuiz(String quizId) throws Exception {
        log.info("deleteMongoQuiz Start!");

        MongoQuizDTO quizDTO = mongoQuizMapper.getQuizById(quizId);
        if (quizDTO != null) {
            String baseUrl = s3Service.getBaseUrlPrefix();

            // 썸네일 삭제
            if (StringUtils.hasText(quizDTO.thumbnailUrl()) && quizDTO.thumbnailUrl().startsWith(baseUrl)) {
                s3Service.delete(s3Service.extractS3FilePath(quizDTO.thumbnailUrl()));
            }

            // 문제별 S3 리소스 삭제
            for (MongoQuizQuestionDTO question : quizDTO.questions()) {

                // 이미지 삭제
                if (StringUtils.hasText(question.imageUrl()) && question.imageUrl().startsWith(baseUrl)) {
                    s3Service.delete(s3Service.extractS3FilePath(question.imageUrl()));
                }

                // 오디오 URL이 .txt 파일이라면 삭제
                if (StringUtils.hasText(question.audioUrl()) && question.audioUrl().startsWith(baseUrl)) {
                    String audioKey = s3Service.extractS3FilePath(question.audioUrl());
                    if (audioKey.endsWith(".txt")) {
                        s3Service.delete(audioKey);
                    } else {
                        s3Service.delete(audioKey);
                    }
                }
            }

            // 추가 확인: detail 테이블에 남아있는 .txt 파일도 삭제 (예외적으로 남아있을 경우)
            List<QuizDetailEntity> details = quizDetailRepository.findAllByQuizId(quizId);
            for (QuizDetailEntity detail : details) {
                String awsAudioUrl = detail.getAwsAudioUrl();
                if (StringUtils.hasText(awsAudioUrl) && awsAudioUrl.startsWith(baseUrl)) {
                    String audioKey = s3Service.extractS3FilePath(awsAudioUrl);
                    if (audioKey.endsWith(".txt")) {
                        s3Service.delete(audioKey);
                    }
                }
            }

            // DB 삭제
            mongoQuizMapper.deleteQuizById(quizId);
            quizDetailRepository.deleteAllByQuizId(quizId);
            quizRepository.deleteByQuizId(quizId);
        }

        log.info("deleteMongoQuiz End!");
    }


    @Override
    @Transactional
    public void deleteQuizQuestion(String quizId, String questionId) throws Exception {
        log.info("{}.deleteQuizQuestion Start! quizId={}, questionId={}", this.getClass().getName(), quizId, questionId);

        MongoQuizDTO quizDTO = mongoQuizMapper.getQuizById(quizId);
        if (quizDTO == null) {
            log.warn("Quiz not found: {}", quizId);
            return;
        }

        // 삭제 대상 문제 찾기
        MongoQuizQuestionDTO targetQuestion = Optional.ofNullable(quizDTO.questions())
                .orElseGet(ArrayList::new)
                .stream()
                .filter(q -> !q.quizDetailId().equals(questionId))
                .findFirst()
                .orElse(null);

        if (targetQuestion != null) {
            String baseUrl = s3Service.getBaseUrlPrefix();

            // 이미지 삭제
            if (StringUtils.hasText(targetQuestion.imageUrl()) && targetQuestion.imageUrl().startsWith(baseUrl)) {
                s3Service.delete(s3Service.extractS3FilePath(targetQuestion.imageUrl()));
            }

            // 오디오 삭제 (.txt 포함)
            if (StringUtils.hasText(targetQuestion.audioUrl())) {
                String audioKey = s3Service.extractS3FilePath(targetQuestion.audioUrl());
                if (targetQuestion.audioUrl().startsWith(baseUrl)) {
                    s3Service.delete(audioKey);
                }
            }
        }

        // MongoDB 질문 목록에서 해당 문제 제거
        List<MongoQuizQuestionDTO> filteredQuestions = Optional.ofNullable(quizDTO.questions())
                .orElseGet(ArrayList::new)
                .stream()
                .filter(q -> !q.quizDetailId().equals(questionId))
                .collect(Collectors.toList());

        // MongoDB에 업데이트
        updateQuizQuestionsAndStatus(quizDTO, filteredQuestions);

        // MariaDB에서도 문제 제거
        quizDetailRepository.deleteByQuizIdAndQuizDetailId(quizId, questionId);

        log.info("{}.deleteQuizQuestion End!", this.getClass().getName());
    }



    @Override
    public List<MongoQuizQuestionDTO> getAllQuizQuestions(String quizId) throws Exception {
        log.info("{}.getAllQuizQuestions Start!", this.getClass().getName());
        MongoQuizDTO dto = mongoQuizMapper.getQuizById(quizId);
        return dto != null && dto.questions() != null ? dto.questions() : Collections.emptyList();
    }

    @Override
    @Transactional
    public void updateQuizInfo(MongoQuizDTO quizDTO, MultipartFile thumbnailFile) throws Exception {
        log.info("updateQuizInfo Start!");

        // 기존 퀴즈 조회
        QuizEntity entity = quizRepository.findByQuizId(quizDTO.quizId());
        if (entity == null) {
            log.warn("해당 quizId에 대한 퀴즈가 존재하지 않음: {}", quizDTO.quizId());
            return;
        }

        // 썸네일 URL: 새 파일이 있으면 업로드, 아니면 기존 유지
        String updatedThumbnailUrl = entity.getThumbnailUrl();
        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            updatedThumbnailUrl = s3Service.replaceS3File(updatedThumbnailUrl, thumbnailFile);
        }

        // MongoDB 업데이트
        MongoQuizDTO updatedDTO = quizDTO.toBuilder()
                .title(quizDTO.title() != null && !quizDTO.title().isBlank() ? quizDTO.title() : entity.getQuizTitle())
                .description(quizDTO.description() != null && !quizDTO.description().isBlank() ? quizDTO.description() : entity.getDescription())
                .quizType(quizDTO.quizType() != null && !quizDTO.quizType().isBlank() ? quizDTO.quizType() : entity.getQuizType())
                .visibility(quizDTO.visibility() != null && !quizDTO.visibility().isBlank() ? quizDTO.visibility() : entity.getVisibility())
                .thumbnailUrl(updatedThumbnailUrl)
                .storedInMongo("Y")
                .build();

        mongoQuizMapper.updateQuizInfo(updatedDTO);

        // MariaDB 업데이트
        QuizEntity updatedEntity = entity.toBuilder()
                .quizTitle(updatedDTO.title())
                .description(updatedDTO.description())
                .quizType(updatedDTO.quizType())
                .visibility(updatedDTO.visibility())
                .thumbnailUrl(updatedThumbnailUrl)
                .quizChgId(quizDTO.authorId()) // 변경자 ID
                .createdAt(getDateTime("yyyy-MM-dd HH:mm:ss"))
                .build();

        quizRepository.save(updatedEntity);

        log.info("updateQuizInfo End!");
    }



    @Override
    @Transactional
    public void updateQuizQuestion(String quizId, MongoQuizQuestionDTO updatedQuestion,
                                   MultipartFile imageFile, MultipartFile audioFile) throws Exception {
        log.info("{}.updateQuizQuestion Start!", this.getClass().getName());

        MongoQuizDTO quizDTO = mongoQuizMapper.getQuizById(quizId);
        if (quizDTO == null) {
            log.warn("Quiz not found: {}", quizId);
            return;
        }

        MongoQuizQuestionDTO existingQuestion = quizDTO.questions().stream()
                .filter(q -> q.quizDetailId().equals(updatedQuestion.quizDetailId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("기존 질문을 찾을 수 없습니다. questionId: " + updatedQuestion.quizDetailId()));

        String updatedImageUrl = existingQuestion.imageUrl();
        if (imageFile != null && !imageFile.isEmpty()) {
            updatedImageUrl = s3Service.replaceS3File(existingQuestion.imageUrl(), imageFile);
        }

        String updatedAudioUrl = existingQuestion.audioUrl();
        if (audioFile != null && !audioFile.isEmpty()) {
            updatedAudioUrl = s3Service.replaceS3File(existingQuestion.audioUrl(), audioFile);
        }

        if (updatedQuestion.audioUrl() != null && updatedQuestion.audioUrl().startsWith("http") && updatedQuestion.audioUrl().contains("youtube")) {
            if (existingQuestion.audioUrl() != null && existingQuestion.audioUrl().endsWith(".txt")) {
                s3Service.delete(s3Service.extractS3FilePath(existingQuestion.audioUrl()));
            }
            updatedAudioUrl = s3Service.uploadYoutubeLinkAsText(updatedQuestion.quizDetailId(), updatedQuestion.audioUrl());
        }

        List<String> finalAnswers = (updatedQuestion.answers() != null && !updatedQuestion.answers().isEmpty())
                ? updatedQuestion.answers()
                : existingQuestion.answers();

        String finalQuestionText = StringUtils.hasText(updatedQuestion.questionText())
                ? updatedQuestion.questionText()
                : existingQuestion.questionText();

        List<String> finalChoices = (updatedQuestion.choices() != null && !updatedQuestion.choices().isEmpty())
                ? updatedQuestion.choices()
                : existingQuestion.choices();

        String finalCutUrl = StringUtils.hasText(updatedQuestion.quizImageCut())
                ? updatedQuestion.quizImageCut()
                : existingQuestion.quizImageCut();

        String finalAudioStart = StringUtils.hasText(updatedQuestion.audioStartTime())
                ? updatedQuestion.audioStartTime()
                : existingQuestion.audioStartTime();

        String finalAudioEnd = StringUtils.hasText(updatedQuestion.audioEndTime())
                ? updatedQuestion.audioEndTime()
                : existingQuestion.audioEndTime();

        MongoQuizQuestionDTO fullyUpdatedQuestion = updatedQuestion.toBuilder()
                .imageUrl(updatedImageUrl)
                .audioUrl(updatedAudioUrl)
                .answers(finalAnswers)
                .questionText(finalQuestionText)
                .choices(finalChoices)
                .quizImageCut(finalCutUrl)
                .audioStartTime(finalAudioStart)
                .audioEndTime(finalAudioEnd)
                .build();

        log.info("fullyUpdatedQuestion = {}", objectMapper.writeValueAsString(fullyUpdatedQuestion));
        log.info("최종 업데이트 대상 quizDTO (MongoQuizMapper로 전달 전):\n{}", objectMapper.writeValueAsString(quizDTO));

        List<MongoQuizQuestionDTO> updatedQuestions = quizDTO.questions().stream()
                .map(q -> q.quizDetailId().equals(updatedQuestion.quizDetailId()) ? fullyUpdatedQuestion : q)
                .collect(Collectors.toList());

        updateQuizQuestionsAndStatus(quizDTO, updatedQuestions);

        // MariaDB 동기화 추가 (image quiz 기준)
        QuizDetailDTO detailDTO = QuizDetailDTO.builder()
                .quizId(quizId)
                .quizDetailId(fullyUpdatedQuestion.quizDetailId())
                .userId(quizDTO.authorId())
                .awsImageUrl(updatedImageUrl)
                .awsImageCut(finalCutUrl)
                .quizAnswer(String.join(",", finalAnswers))
                .questionText(finalQuestionText)
                .audioStartTime(finalAudioStart)
                .audioEndTime(finalAudioEnd)
                .awsAudioUrl(updatedAudioUrl)
                .build();

        quizDetailService.saveImageQuizDetail(detailDTO, imageFile);

        log.info("{}.updateQuizQuestion End!", this.getClass().getName());
    }



    @Override
    @Transactional
    public void updateQuizQuestions(String quizId, List<MongoQuizQuestionDTO> questions) throws Exception {
        log.info("{}.updateQuizQuestions Start!", this.getClass().getName());
        MongoQuizDTO quizDTO = mongoQuizMapper.getQuizById(quizId);
        if (quizDTO == null) {
            log.warn("Quiz not found: {}", quizId);
            return;
        }
        updateQuizQuestionsAndStatus(quizDTO, questions);
        log.info("{}.updateQuizQuestions End!", this.getClass().getName());
    }

    private void updateQuizQuestionsAndStatus(MongoQuizDTO quizDTO, List<MongoQuizQuestionDTO> questions) throws Exception {
        MongoQuizDTO updatedQuiz = quizDTO.toBuilder()
                .questions(questions)
                .storedInMongo(questions.isEmpty() ? "N" : "Y")
                .thumbnailUrl(quizDTO.thumbnailUrl())
                .visibility(quizDTO.visibility())
                .build();
        mongoQuizMapper.updateQuiz(updatedQuiz);
    }

    @Override
    public MongoQuizDTO getMongoQuiz(String quizId) throws Exception {
        log.info("{}.getMongoQuiz Start!", this.getClass().getName());
        MongoQuizDTO dto = mongoQuizMapper.getQuizById(quizId);
        log.info("{}.getMongoQuiz End!", this.getClass().getName());
        return dto;
    }

}
