package kopo.poly.controller;

import jakarta.servlet.http.HttpSession;
import kopo.poly.dto.QuizListResponseDTO;
import kopo.poly.service.IQuizService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/main")
public class MainController {

    private final IQuizService quizService;

    @GetMapping("/mainPage")
    public String mainPage() {
        log.info("MainController.mainPage Start!");
        return "main/mainPage";
    }

    @GetMapping("/mainLoginSuccess")
    public String mainLoginSuccess() {
        log.info("MainController.mainLoginSuccess Start!");
        return "main/mainLoginSuccess";
    }

    /**
     * 마이페이지 퀴즈 목록 조회 (로그인 필요)
     */
    @GetMapping("/myPage")
    public List<QuizListResponseDTO> getMyQuizList(HttpSession session) throws Exception {
        String userId = (String) session.getAttribute("SS_USER_ID");
        if (userId == null) {
            throw new RuntimeException("로그인 후 사용해 주세요.");
        }
        return quizService.getMyQuizList(userId);
    }

    /**
     * 메인페이지 공개 퀴즈 목록 조회
     */
    @GetMapping("/quizList")
    public List<QuizListResponseDTO> getPublicQuizList() throws Exception {
        return quizService.getPublicQuizList();
    }

}
