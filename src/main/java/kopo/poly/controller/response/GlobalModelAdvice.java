package kopo.poly.controller.response;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

@ControllerAdvice
public class GlobalModelAdvice {

    @ModelAttribute
    public void addSessionUserIdToModel(HttpSession session, Model model) {
        String sessionUserId = (String) session.getAttribute("SS_USER_ID");
        model.addAttribute("SS_USER_ID", sessionUserId);
    }
}
