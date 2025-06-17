package kopo.poly.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import kopo.poly.dto.*;
import kopo.poly.repository.QuizDetailRepository;
import kopo.poly.repository.entity.QuizDetailEntity;
import kopo.poly.service.IQuizDetailService;
import kopo.poly.service.IQuizService;
import kopo.poly.service.IMongoQuizService;
import kopo.poly.service.impl.S3Service;
import kopo.poly.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/quiz")
public class QuizController {

    private final IQuizService quizService;
    private final IMongoQuizService mongoQuizService;
    private final S3Service s3Service;
    private final QuizDetailRepository quizDetailRepository;
    private final IQuizDetailService quizDetailService;


    /**
     * 1. MariaDB + MongoDB 퀴즈 정보 저장 (최초 등록)
     */
    @PostMapping("/quizCreate/saveInfo")
    public ResponseEntity<String> saveQuizInfo(
            @RequestPart("quizInfo") QuizSaveRequestDTO quizInfo,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnailFile,
            HttpSession session
    ) throws Exception {

        String userId = (String) session.getAttribute("SS_USER_ID");
        String now = DateUtil.getDateTime("yyyy-MM-dd HH:mm:ss");

        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        // 썸네일 S3 업로드 처리 (있으면 업로드, 없으면 전달된 URL 사용)
        String thumbnailUrl = quizInfo.quiz().thumbnailUrl();
        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            thumbnailUrl = s3Service.uploadFileToS3(thumbnailFile);
        }

        String quizId = UUID.randomUUID().toString();

        QuizDTO quizDTO = QuizDTO.builder()
                .quizId(quizId)
                .userId(userId)
                .quizTitle(quizInfo.quiz().quizTitle())
                .quizCategory(quizInfo.quiz().quizCategory())
                .description(quizInfo.quiz().description())
                .quizType(quizInfo.quiz().quizType())
                .quizChgId(quizInfo.quiz().quizChgId())
                .createdAt(now)
                .quizComment(quizInfo.quiz().quizComment())
                .thumbnailUrl(thumbnailUrl)
                .visibility(quizInfo.quiz().visibility())
                .build();

        quizService.saveQuizInfo(quizDTO);
        mongoQuizService.makeMongoQuiz(quizId);

        return ResponseEntity.ok(quizId);
    }


    /**
     * 1 - 2. MariaDB 퀴즈 단일 정보 조회 (타입, 제목 등)
     */
    @GetMapping("/info/{quizId}")
    public ResponseEntity<QuizDTO> getQuizInfo(@PathVariable String quizId) throws Exception {
        QuizDTO quizInfo = quizService.getQuizInfo(quizId);

        if (quizInfo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(quizInfo);
    }
    /**
     * 1 - 3. MongoDB 퀴즈 단일 정보 조회 (타입, 제목 등)
     */
    @GetMapping("/mongo/{quizId}/info")
    public ResponseEntity<MongoQuizDTO> getMongoQuizInfo(@PathVariable String quizId) throws Exception {
        MongoQuizDTO quizInfo = mongoQuizService.getMongoQuiz(quizId);
        if (quizInfo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(quizInfo);
    }

    /**
     * 2. MongoDB 퀴즈 삭제
     */
    @DeleteMapping("/mongo/{quizId}")
    public void deleteMongoQuiz(@PathVariable String quizId) throws Exception {
        mongoQuizService.deleteMongoQuiz(quizId);
    }

    /**
     * 3. MongoDB 퀴즈 조회 (JSON 반환)
     */
    @GetMapping("/mongo/{quizId}")
    public String getMongoQuizJson(@PathVariable String quizId) throws Exception {
        return mongoQuizService.getMongoQuizJson(quizId);
    }

    /**
     * 4. MongoDB 퀴즈 상태 확인
     */
    @GetMapping("/mongo/{quizId}/status")
    public String getMongoQuizStatus(@PathVariable String quizId) throws Exception {
        MongoQuizDTO dto = mongoQuizService.getMongoQuiz(quizId);
        return dto != null ? dto.storedInMongo() : "NOT FOUND";
    }


    /**
     * 5. 모든 문제 조회
     */
    @GetMapping("/mongo/{quizId}/questions")
    public List<MongoQuizQuestionDTO> getAllQuizQuestions(@PathVariable String quizId) throws Exception {
        return mongoQuizService.getAllQuizQuestions(quizId);
    }

    /**
     * 6. 문제 추가 (이미지 업로드만 지원)
     */
    @PostMapping("/mongo/{quizId}/question")
    public void addQuizQuestion(@PathVariable String quizId,
                                @RequestPart("question") MongoQuizQuestionDTO newQuestion,
                                @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) throws Exception {
        mongoQuizService.addQuizQuestion(quizId, newQuestion, imageFile, null);
    }

    /**
     * 7. 문제 삭제
     */
    @DeleteMapping("/mongo/{quizId}/question/{questionId}")
    public void deleteQuizQuestion(@PathVariable String quizId,
                                   @PathVariable String questionId) throws Exception {
        mongoQuizService.deleteQuizQuestion(quizId, questionId);
    }

    /**
     * 8. 문제 수정 (이미지 교체만 지원)
     */
    @PutMapping("/mongo/{quizId}/question")
    public void updateQuizQuestion(@PathVariable String quizId,
                                   @RequestPart("question") MongoQuizQuestionDTO updatedQuestion,
                                   @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) throws Exception {
        mongoQuizService.updateQuizQuestion(quizId, updatedQuestion, imageFile, null);
    }

    /**
     * 9. 전체 문제 일괄 수정
     */
    @PutMapping("/mongo/{quizId}/questions")
    public void updateQuizQuestions(@PathVariable String quizId,
                                    @RequestBody List<MongoQuizQuestionDTO> questions) throws Exception {
        mongoQuizService.updateQuizQuestions(quizId, questions);
    }

    /**
     * 10. 퀴즈 정보 수정 (썸네일 교체 지원)
     */
    @PutMapping(value = "/mongo/info", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void updateQuizInfo(@RequestPart("quiz") MongoQuizDTO quizDTO,
                               @RequestPart(value = "thumbnailFile", required = false) MultipartFile thumbnailFile)
    throws Exception {
        mongoQuizService.updateQuizInfo(quizDTO, thumbnailFile);
    }


    /**
     * 이미지 퀴즈 문제 저장 (신규/수정 통합)
     */
    @PostMapping("/mongo/{quizId}/image-question")
    public ResponseEntity<String> saveImageQuizQuestion(
            @PathVariable String quizId,
            @RequestPart(value = "originalFile", required = false) MultipartFile originalFile,
            @RequestPart(value = "croppedFile", required = false) MultipartFile croppedFile,
            @RequestParam("answers") String answersJson,
            @RequestParam(value = "quizDetailId", required = false) String quizDetailId,
            HttpSession session
    ) throws Exception {

        String userId = (String) session.getAttribute("SS_USER_ID");

        // 1. 퀴즈 메타 확인
        QuizDTO quiz = quizService.getQuizInfo(quizId);
        if (quiz == null) {
            throw new IllegalArgumentException("해당 quizId에 대한 퀴즈 정보가 없습니다: " + quizId);
        }

        // 2. 기존 문제 정보 조회 (있으면 수정 모드)
        QuizDetailEntity existingDetail = null;
        if (quizDetailId != null && !quizDetailId.isBlank()) {
            existingDetail = quizDetailRepository.findByQuizIdAndQuizDetailId(quizId, quizDetailId);
            if (existingDetail == null) {
                throw new IllegalArgumentException("해당 문제를 찾을 수 없습니다: " + quizDetailId);
            }
        }

        // 3. 이미지 업로드 또는 기존 이미지 유지
        String originalUrl = (originalFile != null && !originalFile.isEmpty())
                ? s3Service.uploadFileToS3(originalFile)
                : (existingDetail != null ? existingDetail.getAwsImageUrl() : null);

        String croppedUrl = (croppedFile != null && !croppedFile.isEmpty())
                ? s3Service.uploadFileToS3(croppedFile)
                : (existingDetail != null ? existingDetail.getAwsImageCut() : null);

        // 4. 정답 파싱
        List<String> answers = new ObjectMapper().readValue(answersJson, new TypeReference<>() {});

        // 5. DTO 생성
        QuizDetailDTO detailDTO = QuizDetailDTO.builder()
                .quizDetailId(quizDetailId) // null이면 내부에서 생성됨
                .quizId(quizId)
                .userId(userId)
                .awsImageUrl(originalUrl)
                .awsImageCut(croppedUrl)
                .quizAnswer(String.join(",", answers))
                .build();

        // 6. 저장 처리
        quizService.saveQuizDetailInfo(detailDTO, originalFile, null);

        // 7. MongoDB 동기화
        mongoQuizService.makeMongoQuiz(quizId);

        return ResponseEntity.ok("이미지 퀴즈 문제 저장 완료");
    }


    /**
     * 오디오 퀴즈 문제 저장 (신규/수정 통합)
     */
    @PostMapping("/mongo/{quizId}/audio-question")
    public ResponseEntity<String> saveAudioQuizQuestion(
            @PathVariable String quizId,
            @RequestBody AudioQuizRequestDTO pDTO,
            HttpSession session
    ) throws Exception {

        String userId = (String) session.getAttribute("SS_USER_ID");
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        // 퀴즈 존재 여부 확인
        QuizDTO quiz = quizService.getQuizInfo(quizId);
        if (quiz == null) {
            throw new IllegalArgumentException("해당 quizId에 대한 퀴즈 정보가 없습니다: " + quizId);
        }

        // 1. S3에 YouTube 링크를 .txt 파일로 업로드
        String uniqueKey = "audio-links/" + quizId + "_" + UUID.randomUUID() + ".txt";
        String s3TxtUrl = s3Service.uploadText(uniqueKey, pDTO.youtubeUrl());

        // 2. 신규 ID 생성 또는 기존 값 유지
        String quizDetailId = (pDTO.quizDetailId() != null && !pDTO.quizDetailId().isBlank())
                ? pDTO.quizDetailId()
                : UUID.randomUUID().toString();

        // 3. JSON 직렬화용 구조 생성 (quizDetailId 기준)
        AudioQuizRequestDTO enrichedDto = pDTO.toBuilder()
                .quizDetailId(quizDetailId)
                .build();
        String quizJson = new ObjectMapper().writeValueAsString(enrichedDto);

        // 4. DTO 생성
        QuizDetailDTO detailDTO = QuizDetailDTO.builder()
                .quizDetailId(quizDetailId)
                .quizId(quizId)
                .userId(userId)
                .awsAudioUrl(s3TxtUrl)
                .audioStartTime(String.valueOf(pDTO.audioStartTime()))
                .audioEndTime(String.valueOf(pDTO.audioEndTime()))
                .quizAnswer(String.join(",", pDTO.answers()))
                .quizJson(quizJson)
                .build();

        // 5. 수정 or 신규 저장
        QuizDetailEntity existing = quizDetailRepository.findByQuizIdAndQuizDetailId(
                detailDTO.quizId(), detailDTO.quizDetailId());

        if (existing != null) {
            QuizDetailEntity updated = existing.toBuilder()
                    .audioStartTime(detailDTO.audioStartTime())
                    .audioEndTime(detailDTO.audioEndTime())
                    .quizAnswer(detailDTO.quizAnswer())
                    .awsAudioUrl(s3TxtUrl)
                    .quizJson(detailDTO.quizJson())
                    .build();
            quizDetailRepository.save(updated);
            log.info("기존 오디오 퀴즈 수정 완료: {}", quizDetailId);

        } else {
            QuizDetailEntity entity = QuizDetailEntity.builder()
                    .quizDetailId(detailDTO.quizDetailId())
                    .quizId(detailDTO.quizId())
                    .userId(detailDTO.userId())
                    .awsAudioUrl(detailDTO.awsAudioUrl())
                    .audioStartTime(detailDTO.audioStartTime())
                    .audioEndTime(detailDTO.audioEndTime())
                    .quizAnswer(detailDTO.quizAnswer())
                    .quizJson(detailDTO.quizJson())
                    .build();
            quizDetailRepository.save(entity);
            log.info("신규 오디오 퀴즈 저장 완료: {}", quizDetailId);
        }

        // 6. MongoDB 이관
        mongoQuizService.makeMongoQuiz(quizId);
        log.info("MongoDB 이관 완료");

        log.info("saveAudioQuizQuestion End!");
        return ResponseEntity.ok("오디오 퀴즈 문제 저장 완료");
    }


    /**
     * 객관식 퀴즈 문제 저장 (신규/수정 통합)
     */
    @PostMapping("/mongo/{quizId}/multiple-question")
    public ResponseEntity<String> saveMultipleChoiceQuizQuestion(
            @PathVariable String quizId,
            @RequestBody MultipleChoiceRequestDTO pDTO,
            HttpSession session
    ) throws Exception {

        log.info("saveMultipleChoiceQuizQuestion Start!");

        String userId = (String) session.getAttribute("SS_USER_ID");
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        // 퀴즈 존재 여부 확인
        QuizDTO quiz = quizService.getQuizInfo(quizId);
        if (quiz == null) {
            throw new IllegalArgumentException("해당 quizId에 대한 퀴즈 정보가 없습니다: " + quizId);
        }

        // 1. 퀴즈 문제 ID (기존 수정 or 신규 생성)
        String quizDetailId = (pDTO.quizDetailId() != null && !pDTO.quizDetailId().isBlank())
                ? pDTO.quizDetailId()
                : UUID.randomUUID().toString();

        // 2. JSON 직렬화 (DTO 자체를 저장)
        MultipleChoiceRequestDTO enrichedDto = pDTO.toBuilder()
                .quizDetailId(quizDetailId)
                .build();
        String quizJson = new ObjectMapper().writeValueAsString(enrichedDto);

        // 3. MariaDB 기존 데이터 확인
        QuizDetailEntity existing = quizDetailRepository.findByQuizIdAndQuizDetailId(quizId, quizDetailId);

        if (existing != null) {
            // 수정 처리
            QuizDetailEntity updated = existing.toBuilder()
                    .questionText(pDTO.questionText())
                    .quizAnswer(String.join(",", pDTO.answers()))
                    .quizJson(quizJson)
                    .build();

            quizDetailRepository.save(updated);
            log.info("기존 객관식 퀴즈 수정 완료: {}", quizDetailId);

        } else {
            // 신규 저장 처리
            QuizDetailEntity entity = QuizDetailEntity.builder()
                    .quizDetailId(quizDetailId)
                    .quizId(quizId)
                    .userId(userId)
                    .questionText(pDTO.questionText())
                    .quizAnswer(String.join(",", pDTO.answers()))
                    .quizJson(quizJson)
                    .build();

            quizDetailRepository.save(entity);
            log.info("신규 객관식 퀴즈 저장 완료: {}", quizDetailId);
        }

        // 4. MongoDB 동기화
        mongoQuizService.makeMongoQuiz(quizId);
        log.info("MongoDB 이관 완료");

        log.info("saveMultipleChoiceQuizQuestion End!");
        log.info("수신 DTO: {}", new ObjectMapper().writeValueAsString(pDTO));
        log.info("직렬화된 quizJson: {}", quizJson);
        return ResponseEntity.ok("객관식 퀴즈 문제 저장 완료");
    }



    // .txt에 저장된 S3 오디오 URL을 호출하는 기능
    @GetMapping("/mongo/{quizId}/question/{questionId}/youtube-id")
    public ResponseEntity<String> getYouTubeVideoId(
            @PathVariable String quizId,
            @PathVariable String questionId) throws Exception {

        // 1. 상세 문제 조회
        QuizDetailEntity detail = quizDetailRepository.findByQuizIdAndQuizDetailId(quizId, questionId);
        if (detail == null) {
            log.warn("퀴즈 상세 정보가 존재하지 않음: quizId={}, questionId={}", quizId, questionId);
            return ResponseEntity.notFound().build();
        }

        // 2. S3 저장된 .txt 경로 확인
        String txtS3Url = detail.getAwsAudioUrl();
        if (txtS3Url == null || !txtS3Url.endsWith(".txt")) {
            log.warn("유효하지 않은 .txt 오디오 경로: {}", txtS3Url);
            return ResponseEntity.badRequest().body("invalid_audio_url");
        }

        // 3. S3에서 유튜브 URL 추출
        String youtubeUrl;
        try {
            youtubeUrl = s3Service.readYoutubeLinkFromS3(txtS3Url);
        } catch (Exception e) {
            log.error("S3에서 유튜브 URL 읽기 실패: {}", txtS3Url, e);
            return ResponseEntity.status(500).body("s3_read_error");
        }

        if (youtubeUrl == null || youtubeUrl.isBlank()) {
            log.warn("S3에서 추출한 유튜브 URL이 비어있음: {}", txtS3Url);
            return ResponseEntity.badRequest().body("empty_url");
        }

        // 4. 유튜브 videoId 추출
        String videoId = extractVideoIdFromUrl(youtubeUrl);
        if (videoId == null) {
            log.warn("유튜브 videoId 추출 실패: {}", youtubeUrl);
            return ResponseEntity.badRequest().body("invalid_youtube_url");
        }

        return ResponseEntity.ok(videoId);
    }

    private String extractVideoIdFromUrl(String url) {
        try {
            URL parsed = new URL(url);
            String host = parsed.getHost();

            if (host.contains("youtu.be")) {
                return parsed.getPath().substring(1); // /abc123 → abc123
            }

            if (host.contains("youtube.com")) {
                String query = parsed.getQuery();
                if (query != null) {
                    for (String param : query.split("&")) {
                        if (param.startsWith("v=")) {
                            return param.substring(2);
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.warn("유튜브 URL 파싱 실패: {}", url, e);
        }

        return null;
    }


    // 기존 문제 수정을 위한 조회 기능
    @GetMapping("/mongo/{quizId}/question/{questionId}")
    public ResponseEntity<MongoQuizQuestionDTO> getQuizQuestion(
            @PathVariable String quizId,
            @PathVariable String questionId) throws Exception {
        List<MongoQuizQuestionDTO> allQuestions = mongoQuizService.getAllQuizQuestions(quizId);
        return allQuestions.stream()
                .filter(q -> q.quizDetailId().equals(questionId))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 기존 이미지 퀴즈 문제 수정을 위한 수정 기능
    @PutMapping("/mongo/{quizId}/question/{questionId}")
    public void updateQuizQuestion(
            @PathVariable String quizId,
            @PathVariable String questionId,
            @RequestPart("question") MongoQuizQuestionDTO updatedQuestion,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile
    ) throws Exception {

        updatedQuestion = updatedQuestion.toBuilder().quizDetailId(questionId).build();
        mongoQuizService.updateQuizQuestion(quizId, updatedQuestion, imageFile, null);
    }

    @PostMapping("/generate-ai")
    @ResponseBody
    public ResponseEntity<String> generateAiQuiz(@RequestBody AiQuizRequestDTO dto, HttpSession session) throws Exception {
        log.info("generateAiQuiz Controller 호출: {}", dto);

        String userId = (String) session.getAttribute("SS_USER_ID");
        if (userId == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        quizService.generateAiQuiz(dto, userId);

        return ResponseEntity.ok("AI 퀴즈가 생성되었습니다.");
    }

    // 메인페이지 퀴즈 리스트 페이징
    @GetMapping("/list/main")
    @ResponseBody
    public ResponseEntity<List<QuizDTO>> getPublicQuizList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) throws Exception {

        log.info("getPublicQuizList API called - page: {}, size: {}", page, size);

        List<QuizDTO> quizList = quizService.getPublicQuizList(page, size);
        return ResponseEntity.ok(quizList);
    }


    // 마이페이지 유저아이디 기반 퀴즈 리스트 페이징
    @GetMapping("/list/mypage")
    @ResponseBody
    public ResponseEntity<List<QuizDTO>> getUserQuizList(
            @RequestParam String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) throws Exception {

        log.info("getUserQuizList API called - userId: {}, page: {}, size: {}", userId, page, size);

        List<QuizDTO> quizList = quizService.getUserQuizList(userId, page, size);
        return ResponseEntity.ok(quizList);
    }

    /**
     *
     *  퀴즈 플레이 기능 컨트롤러
     *
     */

    @GetMapping("/play/info/{quizId}")
    public ResponseEntity<QuizDTO> getQuizPlayInfo(@PathVariable String quizId) throws Exception {
        QuizDTO rDTO = quizService.getQuizInfo(quizId); // 메타데이터 (MariaDB)
        return ResponseEntity.ok(rDTO);
    }

    @GetMapping("/play/{quizId}")
    public ResponseEntity<List<MongoQuizQuestionDTO>> getQuizPlayQuestions(
            @PathVariable String quizId,
            @RequestParam int count) throws Exception {

        List<MongoQuizQuestionDTO> allQuestions = mongoQuizService.getAllQuizQuestions(quizId);
        Collections.shuffle(allQuestions);
        return ResponseEntity.ok(allQuestions.stream().limit(count).toList());
    }

    @GetMapping("/mongo/{quizId}/questions/count")
    public ResponseEntity<Integer> getQuizQuestionCount(@PathVariable String quizId) throws Exception {
        int count = mongoQuizService.getAllQuizQuestions(quizId).size();
        return ResponseEntity.ok(count);
    }

    /**
     * 랜덤 문제 출제 (MongoDB 기반)
     */
    @GetMapping("/mongo/{quizId}/random")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getRandomMongoQuizQuestions(
            @PathVariable String quizId,
            @RequestParam int count) throws Exception {

        log.info("getRandomMongoQuizQuestions - quizId: {}, count: {}", quizId, count);

        List<Map<String, Object>> randomQuestions = mongoQuizService.getRandomQuizQuestions(quizId, count);

        if (randomQuestions.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(randomQuestions);
    }

}
