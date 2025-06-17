package kopo.poly.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kopo.poly.dto.QuizDetailDTO;
import kopo.poly.service.IOpenAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class OpenAiService implements IOpenAiService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    @Override
    public List<QuizDetailDTO> generateQuizFromPrompt(String quizId, String prompt, String category) throws Exception {

        if (prompt == null || prompt.isBlank()) {
            prompt = String.format("""
                        '%s'에 대한 객관식 문제 1개를 아래 JSON 형식으로 출력해줘:
                        {
                          "questionText": "...",
                          "choices": ["...", "...", "...", "..."],
                          "answers": ["..."]
                        }
                        JSON 외의 설명이나 코드블럭 없이 순수 JSON만 반환해줘.
                    """, category);
        }

        log.info("GPT 요청 프롬프트: {}", prompt);

        String result = chatClient.prompt()
                .system("당신은 객관식 퀴즈 문제를 생성하는 AI입니다.")
                .user(prompt)
                .call()
                .content();

        log.info("GPT 응답 내용:\n{}", result);

        // GPT 응답에서 ```json ... ``` 제거
        result = result.strip();
        if (result.startsWith("```json")) {
            result = result.replaceFirst("```json", "").strip();
        }
        if (result.endsWith("```")) {
            result = result.substring(0, result.lastIndexOf("```")).strip();
        }

        try {
            // 단일 JSON 객체 파싱
            var node = objectMapper.readTree(result);
            String quizJson = objectMapper.writeValueAsString(node);

            List<String> answers = objectMapper.convertValue(
                    node.get("answers"), new TypeReference<>() {
                    }
            );
            List<String> choices = objectMapper.convertValue(
                    node.get("choices"), new TypeReference<>() {
                    }
            );

            String mainAnswer = answers.stream().findFirst().orElse("");

            QuizDetailDTO dto = QuizDetailDTO.builder()
                    .quizId(quizId)
                    .quizDetailId(UUID.randomUUID().toString())
                    .questionText(node.get("questionText").asText())
                    .quizJson(quizJson)
                    .quizAnswer(mainAnswer)
                    .quizCategory(category)
                    .quizMultiAi("Y")
                    .choices(choices)
                    .build();

            return List.of(dto);

        } catch (Exception e) {
            log.error("GPT 응답 파싱 실패", e);
            throw new RuntimeException("GPT 응답 파싱 실패: " + result);
        }

    }
}
