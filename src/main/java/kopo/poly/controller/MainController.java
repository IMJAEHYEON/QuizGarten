package kopo.poly.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/main")
public class MainController {

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
}
