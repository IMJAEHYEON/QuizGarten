package kopo.poly.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
@Slf4j
@Controller
@RequestMapping("/quiz")
@RequiredArgsConstructor
public class QuizPageController {

    @GetMapping("/makeQuizStart")
    public String quizCreatePage() {
        return "quiz/quizCreate/makeQuizStart"; // 퀴즈 유형 선택 페이지
    }

    @GetMapping("/makeQuizInfo")
    public String quizCreateInfoPage() {
        return "quiz/quizCreate/makeQuizInfo"; // 퀴즈 제목/설명 입력 페이지
    }


    @GetMapping("/makeQuizEdit")
    public String quizEditQuestionPage() {
        return "quiz/quizEdit/makeQuizEdit"; // 문제 추가 페이지 (유형별로 다를 수 있음)
    }

    @GetMapping("/makeQuizImageQuestion")
    public String makeQuizImage() {
        return "quiz/quizCreate/makeQuizImageQuestion";
    }

    @GetMapping("/makeQuizAudioQuestion")
    public String makeQuizAudio() {
        return "quiz/quizCreate/makeQuizAudioQuestion";
    }

    @GetMapping("/makeQuizMultipleQuestion")
    public String makeQuizMulti() {
        return "quiz/quizCreate/makeQuizMultipleQuestion";
    }

    @GetMapping("/makeQuizAiQuestion")
    public String makeQuizAi() {
        return "quiz/quizCreate/makeQuizAiQuestion";
    }

    @GetMapping("/edit")
    public String quizEditPage() {
        return "quiz/quizEdit/makeQuizEdit";
    }


    @GetMapping("/playInfo")
    public String quizPlayInfoPage(@RequestParam String quiz) {
        log.info("quizPlayInfoPage called with quizId: {}", quiz);
        return "quiz/quizPlay/quizPlayInfo";
    }

    @GetMapping("/playPage")
    public String quizPlayPage(@RequestParam String quiz, @RequestParam int count) {
        log.info("quizPlayPage called with quizId: {}, count: {}", quiz, count);
        return "quiz/quizPlay/quizPlay";
    }

    @GetMapping("/playCorrect")
    public String quizPlayCorrectPage(
            @RequestParam String quiz,
            @RequestParam String quizDetailId,
            @RequestParam String userAnswer) {
        log.info("quizPlayCorrectPage called with quizId: {}, quizDetailId: {}, userAnswer: {}", quiz, quizDetailId, userAnswer);

        return "quiz/quizPlay/quizPlayCorrect";
    }

    @GetMapping("/complete")
    public String quizCompletePage(@RequestParam(name = "quiz", required = false) String quizId, Model model) {
        model.addAttribute("quizId", quizId);
        return "quiz/quizPlay/quizComplete";
    }

}
