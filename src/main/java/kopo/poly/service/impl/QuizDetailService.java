package kopo.poly.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import kopo.poly.dto.QuizDetailDTO;
import kopo.poly.repository.QuizDetailRepository;
import kopo.poly.repository.entity.QuizDetailEntity;
import kopo.poly.service.IQuizDetailService;
import kopo.poly.service.IS3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizDetailService implements IQuizDetailService {

    private final QuizDetailRepository quizDetailRepository;
    private final IS3Service s3Service;
    private final ObjectMapper objectMapper;


    @Override
    @Transactional
    public void deleteQuizDetailsByQuizId(String quizId) throws Exception {
        quizDetailRepository.deleteAllByQuizId(quizId);
    }

    @Override
    @Transactional
    public void saveImageQuizDetail(QuizDetailDTO detail, MultipartFile imageFile) throws Exception {
        String imageUrl = null;

        if (imageFile != null && !imageFile.isEmpty()) {
            imageUrl = s3Service.uploadFileToS3(imageFile);
        } else {
            QuizDetailEntity existing = quizDetailRepository.findByQuizIdAndQuizDetailId(
                    detail.quizId(), detail.quizDetailId()
            );
            imageUrl = existing != null ? existing.getAwsImageUrl() : null;
        }

        saveDetail(detail.toBuilder().awsImageUrl(imageUrl).build());
    }



    @Override
    @Transactional
    public void saveAudioQuizDetail(QuizDetailDTO detail, String audioUrl) throws Exception {
        saveDetail(detail.toBuilder().awsAudioUrl(audioUrl).build());
    }


    @Override
    @Transactional
    public void saveMultipleChoiceQuizDetail(QuizDetailDTO detail) throws Exception {
        saveDetail(detail); // 그대로 저장
    }

    @Override
    public List<QuizDetailDTO> getQuizDetailsByQuizId(String quizId) throws Exception {
        List<QuizDetailEntity> entityList = quizDetailRepository.findByQuizId(quizId);

        return entityList.stream()
                .map(e -> objectMapper.convertValue(e, QuizDetailDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * 공통 저장 로직
     */
    @Transactional
    public void saveDetail(QuizDetailDTO detail) throws Exception {
        log.info("saveDetail Start!");

        boolean isNew = (detail.quizDetailId() == null || detail.quizDetailId().isEmpty());
        String quizDetailId = isNew ? UUID.randomUUID().toString() : detail.quizDetailId();

        QuizDetailDTO finalDetail = detail.toBuilder().quizDetailId(quizDetailId).build();
        String quizJsonString = objectMapper.writeValueAsString(finalDetail);

        QuizDetailEntity entity = QuizDetailEntity.builder()
                .quizDetailId(quizDetailId)
                .quizId(detail.quizId())
                .userId(detail.userId())
                .quizCategory(detail.quizCategory())
                .quizJson(quizJsonString)
                .quizMongo(detail.quizMongo())
                .awsImageUrl(detail.awsImageUrl())
                .awsImageCut(detail.awsImageCut())
                .awsAudioUrl(detail.awsAudioUrl())
                .audioStartTime(detail.audioStartTime())
                .audioEndTime(detail.audioEndTime())
                .quizMultiAi(detail.quizMultiAi())
                .quizAnswer(detail.quizAnswer())
                .build();

        quizDetailRepository.save(entity);

        log.info("saveDetail End!");
    }


}
