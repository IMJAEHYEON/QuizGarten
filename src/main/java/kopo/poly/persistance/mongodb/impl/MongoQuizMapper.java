package kopo.poly.persistance.mongodb.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import kopo.poly.dto.MongoQuizDTO;
import kopo.poly.dto.MongoQuizQuestionDTO;
import kopo.poly.persistance.mongodb.IMongoQuizMapper;
import kopo.poly.util.MongoDBUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.mongodb.client.model.ReplaceOptions;

import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class MongoQuizMapper implements IMongoQuizMapper {

    private final ObjectMapper objectMapper;
    private final MongoDBUtil mongoDBUtil;

    // properties에서 주입
    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    private static final String STORED_IN_MONGO_YES = "Y";
    private static final String QUIZ_COLLECTION = "QuizData";

    private MongoCollection<Document> getCollection() {
        return mongoDBUtil.getMongoDB(databaseName).getCollection(QUIZ_COLLECTION);
    }

    @Override
    public void insertQuiz(MongoQuizDTO pDTO) throws Exception {
        log.info("insertQuiz Start!");
        Document doc = Document.parse(objectMapper.writeValueAsString(pDTO));
        getCollection().insertOne(doc);
        log.info("insertQuiz End!");
    }

    @Override
    public MongoQuizDTO getQuizById(String quizId) throws Exception {
        log.info("getQuizById Start - quizId: {}", quizId);
        Document result = getCollection().find(new Document("quizId", quizId)).first();
        if (result == null) {
            log.warn("No quiz found with quizId: {}", quizId);
            return null;
        }
        return objectMapper.readValue(result.toJson(), MongoQuizDTO.class);
    }

    @Override
    public void deleteQuizById(String quizId) throws Exception {
        log.info("deleteQuizById Start - quizId: {}", quizId);
        getCollection().deleteOne(new Document("quizId", quizId));
        log.info("deleteQuizById End - quizId: {}", quizId);
    }

    @Override
    public void upsertQuiz(MongoQuizDTO pDTO) throws Exception {
        log.info("upsertQuiz Start - quizId: {}", pDTO.quizId());
        Document doc = Document.parse(objectMapper.writeValueAsString(pDTO));
        getCollection().replaceOne(
                new Document("quizId", pDTO.quizId()),
                doc,
                new ReplaceOptions().upsert(true)
        );
        log.info("upsertQuiz End - quizId: {}", pDTO.quizId());
    }

    @Override
    public void updateQuiz(MongoQuizDTO pDTO) throws Exception {
        log.info("updateQuiz Start - quizId: {}", pDTO.quizId());
        Document updatedDoc = Document.parse(objectMapper.writeValueAsString(pDTO));
        getCollection().replaceOne(new Document("quizId", pDTO.quizId()), updatedDoc);
        log.info("MongoDB로 전송할 Document:\n{}", updatedDoc.toJson());
        log.info("updateQuiz 직렬화된 JSON = {}", objectMapper.writeValueAsString(pDTO));
        log.info("updateQuiz End - quizId: {}", pDTO.quizId());
    }

    @Override
    public void updateQuizInfo(MongoQuizDTO quizDTO) throws Exception {
        log.info("updateQuizInfo Start - quizId: {}", quizDTO.quizId());
        MongoQuizDTO existingDTO = getQuizById(quizDTO.quizId());
        if (existingDTO == null) {
            log.warn("Quiz not found for update - quizId: {}", quizDTO.quizId());
            return;
        }

        String resolvedThumbnailUrl = quizDTO.thumbnailUrl() != null ? quizDTO.thumbnailUrl() : existingDTO.thumbnailUrl();
        String resolvedVisibility = quizDTO.visibility() != null ? quizDTO.visibility() : existingDTO.visibility();

        MongoQuizDTO updatedDTO = existingDTO.toBuilder()
                .title(quizDTO.title())
                .description(quizDTO.description())
                .category(quizDTO.category())
                .storedInMongo(STORED_IN_MONGO_YES)
                .thumbnailUrl(resolvedThumbnailUrl)
                .visibility(resolvedVisibility)
                .build();

        Document newDoc = Document.parse(objectMapper.writeValueAsString(updatedDTO));
        getCollection().replaceOne(new Document("quizId", quizDTO.quizId()), newDoc);
        log.info("updateQuizInfo End - quizId: {}", quizDTO.quizId());
    }

    @Override
    public void updateQuizQuestions(String quizId, List<MongoQuizQuestionDTO> questions) throws Exception {
        log.info("updateQuizQuestions Start - quizId: {}", quizId);
        MongoQuizDTO existingDTO = getQuizById(quizId);
        if (existingDTO == null) {
            log.warn("No quiz found for updateQuestions - quizId: {}", quizId);
            return;
        }

        MongoQuizDTO updatedDTO = existingDTO.toBuilder()
                .questions(questions)
                .storedInMongo(STORED_IN_MONGO_YES)
                .thumbnailUrl(existingDTO.thumbnailUrl())
                .visibility(existingDTO.visibility())
                .build();

        Document newDoc = Document.parse(objectMapper.writeValueAsString(updatedDTO));
        getCollection().replaceOne(new Document("quizId", quizId), newDoc);
        log.info("updateQuizQuestions End - quizId: {}", quizId);
    }

    @Override
    public List<MongoQuizQuestionDTO> getAllQuizQuestions(String quizId) throws Exception {
        log.info("getAllQuizQuestions Start - quizId: {}", quizId);
        MongoQuizDTO dto = getQuizById(quizId);
        if (dto == null) {
            return Collections.emptyList();
        }
        return dto.questions();
    }

    @Override
    public void deleteQuizQuestion(String quizId, String questionId) throws Exception {
        log.info("deleteQuizQuestion Start - quizId: {}, questionId: {}", quizId, questionId);
        MongoQuizDTO quizDTO = getQuizById(quizId);
        if (quizDTO == null) {
            log.warn("No quiz found for deletion - quizId: {}", quizId);
            return;
        }

        List<MongoQuizQuestionDTO> updatedQuestions = quizDTO.questions().stream()
                .filter(q -> !q.quizDetailId().equals(questionId))
                .toList();

        MongoQuizDTO updatedDTO = quizDTO.toBuilder()
                .questions(updatedQuestions)
                .storedInMongo(STORED_IN_MONGO_YES)
                .thumbnailUrl(quizDTO.thumbnailUrl())
                .visibility(quizDTO.visibility())
                .build();

        Document newDoc = Document.parse(objectMapper.writeValueAsString(updatedDTO));
        getCollection().replaceOne(new Document("quizId", quizId), newDoc);
        log.info("deleteQuizQuestion End - quizId: {}, questionId: {}", quizId, questionId);
    }
}
