package kopo.poly.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kopo.poly.dto.MsgDTO;
import kopo.poly.dto.NoticeDTO;
import kopo.poly.service.INoticeService;
import kopo.poly.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;


/*
 * Controller 선언해야만 Spring 프레임워크에서 Controller인지 인식 가능
 * 자바 서블릿 역할 수행
 *
 * slf4j는 스프링 프레임워크에서 로그 처리하는 인터페이스 기술이며,
 * 로그처리 기술인 log4j와 logback과 인터페이스 역할 수행함
 * 스프링 프레임워크는 기본으로 logback을 채택해서 로그 처리함
 * */
@Slf4j
@RequestMapping(value = "/notice")
@RequiredArgsConstructor
@Controller
public class NoticeController {


    private final INoticeService noticeService;

    /**
     * 게시판 리스트 보여주기
     * <p>
     * GetMapping(value = "notice/noticeList") =>  GET방식을 통해 접속되는 URL이 notice/noticeList 경우 아래 함수를 실행함
     */
    @GetMapping(value = "quizNoticeList")
    public String quizNoticeList(HttpSession session,
                                 ModelMap model,
                                 @RequestParam(value = "page", defaultValue = "0") int page,
                                 @RequestParam(value = "size", defaultValue = "10") int size) throws Exception {

        log.info("{}.quizNoticeList Start!", this.getClass().getName());

        String sessionUserId = (String) session.getAttribute("SS_USER_ID");

        if (sessionUserId != null && !sessionUserId.isEmpty()) {
            log.info("로그인된 사용자 ID : {}", sessionUserId);
            model.addAttribute("SS_USER_ID", sessionUserId);
        } else {
            log.info("비로그인 사용자입니다.");
        }
        model.addAttribute("SS_USER_ID", sessionUserId);

        // 페이징 적용된 리스트 조회
        Page<NoticeDTO> noticePage = noticeService.getNoticeList(page, size);

        model.addAttribute("noticePage", noticePage); // 페이징된 리스트
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", noticePage.getTotalPages());

        log.info("{}.quizNoticeList End!", this.getClass().getName());

        return "notice/quizNoticeList"; // Thymeleaf 템플릿
    }


    /**
     * 게시판 작성 페이지 이동
     * <p>
     * 이 함수는 게시판 작성 페이지로 접근하기 위해 만듬
     * <p>
     * GetMapping(value = "notice/noticeReg") =>  GET방식을 통해 접속되는 URL이 notice/noticeReg 경우 아래 함수를 실행함
     */
    @GetMapping(value = "quizNoticeReg")
    public String quizNoticeReg(HttpSession session, ModelMap model) {

        log.info("{}.quizNoticeReg Start!", this.getClass().getName());

        String sessionUserId = (String) session.getAttribute("SS_USER_ID");

        if (sessionUserId == null || sessionUserId.isEmpty()) {
            log.info("비로그인 사용자 접근 - 메인 페이지로 리디렉션");
            return "redirect:/main/mainPage"; // 또는 redirect:/login
        }

        // 로그인된 사용자 ID Thymeleaf로 전달
        model.addAttribute("SS_USER_ID", sessionUserId);

        log.info("{}.quizNoticeReg End!", this.getClass().getName());

        log.info("{}.quizNoticeReg End!", this.getClass().getName());

        // 함수 처리가 끝나고 보여줄 HTML (Thymeleaf) 파일명
        // templates/notice/quizNoticeReg.html
        return "notice/quizNoticeReg";
    }

    /**
     * 게시판 글 등록
     * <p>
     * 게시글 등록은 Ajax로 호출되기 때문에 결과는 JSON 구조로 전달해야만 함
     * JSON 구조로 결과 메시지를 전송하기 위해 @ResponseBody 어노테이션 추가함
     */
    @ResponseBody
    @PostMapping(value = "quizNoticeInsert")
    public MsgDTO quizNoticeInsert(HttpServletRequest request, HttpSession session) {

        log.info("{}.quizNoticeInsert Start!", this.getClass().getName());

        String msg = ""; // 메시지 내용

        MsgDTO dto; // 결과 메시지 구조

        try {
            // 로그인된 사용자 아이디를 가져오기
            String userId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));
            String title = CmmUtil.nvl(request.getParameter("title")); // 제목
            String noticeYn = CmmUtil.nvl(request.getParameter("noticeYn")); // 공지글 여부
            String contents = CmmUtil.nvl(request.getParameter("contents")); // 내용

            /*
             * ####################################################################################
             * 반드시, 값을 받았으면, 꼭 로그를 찍어서 값이 제대로 들어오는지 파악해야함 반드시 작성할 것
             * ####################################################################################
             */
            log.info("session user_id : {}", userId);
            log.info("title : {}", title);
            log.info("noticeYn : {}", noticeYn);
            log.info("contents : {}", contents);

            // 데이터 저장하기 위해 DTO에 저장하기
            NoticeDTO pDTO = NoticeDTO.builder().userId(userId).title(title)
                    .noticeYn(noticeYn).contents(contents).build();

            /*
             * 게시글 등록하기위한 비즈니스 로직을 호출
             */
            noticeService.insertNoticeInfo(pDTO);

            // 저장이 완료되면 사용자에게 보여줄 메시지
            msg = "등록되었습니다.";

        } catch (Exception e) {

            // 저장이 실패되면 사용자에게 보여줄 메시지
            msg = "실패하였습니다. : " + e.getMessage();
            log.info(e.toString());

        } finally {
            // 결과 메시지 전달하기
            dto = MsgDTO.builder().msg(msg).build();

            log.info("{}.quizNoticeInsert End!", this.getClass().getName());
        }

        return dto;
    }

    /**
     * 게시판 상세보기
     */
    @GetMapping(value = "quizNoticeInfo")
    public String quizNoticeInfo(HttpServletRequest request, ModelMap model) throws Exception {

        log.info("{}.quizNoticeInfo Start!", this.getClass().getName());

        String nSeq = CmmUtil.nvl(request.getParameter("nSeq"), "0");

        log.info("nSeq : {}", nSeq);

        NoticeDTO pDTO = NoticeDTO.builder().noticeSeq(Long.parseLong(nSeq)).build();

        // 조회수 증가 포함한 상세 정보 가져오기
        NoticeDTO rDTO = Optional.ofNullable(noticeService.getNoticeInfo(pDTO, true))
                .orElseGet(() -> NoticeDTO.builder().build());

        model.addAttribute("rDTO", rDTO);

        log.info("{}.noticeInfo End!", this.getClass().getName());

        return "notice/quizNoticeInfo";
    }


    /**
     * 게시판 수정 보기
     */
    @GetMapping(value = "quizNoticeEditInfo")
    public String quizNoticeEditInfo(HttpServletRequest request,HttpSession session, ModelMap model) throws Exception {

        log.info("{}.quizNoticeEditInfo Start!", this.getClass().getName());

        String nSeq = CmmUtil.nvl(request.getParameter("nSeq")); // 공지글번호(PK)
        String sessionUserId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));

        /*
         * ####################################################################################
         * 반드시, 값을 받았으면, 꼭 로그를 찍어서 값이 제대로 들어오는지 파악해야함 반드시 작성할 것
         * ####################################################################################
         */
        log.info("nSeq : {}", nSeq);

        /*
         * 값 전달은 반드시 DTO 객체를 이용해서 처리함 전달 받은 값을 DTO 객체에 넣는다.
         */
        NoticeDTO pDTO = NoticeDTO.builder().noticeSeq(Long.parseLong(nSeq)).build();

        // Java 8부터 제공되는 Optional 활용하여 NPE(Null Pointer Exception) 처리
        NoticeDTO rDTO = Optional.ofNullable(noticeService.getNoticeInfo(pDTO, false))
                .orElseGet(() -> NoticeDTO.builder().build());

        model.addAttribute("SS_USER_ID", sessionUserId);
        // 조회된 리스트 결과값 넣어주기
        model.addAttribute("rDTO", rDTO);

        log.info("{}.quizNoticeEditInfo End!", this.getClass().getName());

        return "notice/quizNoticeEditInfo";
    }

    /**
     * 게시판 글 수정
     */
    @ResponseBody
    @PostMapping(value = "noticeUpdate")
    public MsgDTO quizNoticeUpdate(HttpSession session, HttpServletRequest request) {

        log.info("{}.quizNoticeUpdate Start!", this.getClass().getName());

        String msg = ""; // 메시지 내용
        MsgDTO dto; // 결과 메시지 구조

        try {
            String userId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID")); // 아이디
            String nSeq = CmmUtil.nvl(request.getParameter("nSeq")); // 글번호(PK)
            String title = CmmUtil.nvl(request.getParameter("title")); // 제목
            String noticeYn = CmmUtil.nvl(request.getParameter("noticeYn")); // 공지글 여부
            String contents = CmmUtil.nvl(request.getParameter("contents")); // 내용

            /*
             * ####################################################################################
             * 반드시, 값을 받았으면, 꼭 로그를 찍어서 값이 제대로 들어오는지 파악해야함 반드시 작성할 것
             * ####################################################################################
             */
            log.info("userId : {}", userId);
            log.info("nSeq : {}", nSeq);
            log.info("title : {}", title);
            log.info("noticeYn : {}", noticeYn);
            log.info("contents : {}", contents);

            /*
             * 값 전달은 반드시 DTO 객체를 이용해서 처리함 전달 받은 값을 DTO 객체에 넣는다.
             */
            NoticeDTO pDTO = NoticeDTO.builder().userId(userId).noticeSeq(Long.parseLong(nSeq))
                    .title(title).noticeYn(noticeYn).contents(contents).build();

            // 게시글 수정하기 DB
            noticeService.updateNoticeInfo(pDTO);

            msg = "수정되었습니다.";

        } catch (Exception e) {
            msg = "실패하였습니다. : " + e.getMessage();
            log.info(e.toString());

        } finally {

            // 결과 메시지 전달하기
            dto = MsgDTO.builder().msg(msg).build();

            log.info("{}.quizNoticeUpdate End!", this.getClass().getName());

        }

        return dto;
    }

    /**
     * 게시판 글 삭제
     */
    @ResponseBody
    @PostMapping(value = "noticeDelete")
    public MsgDTO quizNoticeDelete(HttpServletRequest request) {

        log.info("{}.quizNoticeDelete Start!", this.getClass().getName());

        String msg = ""; // 메시지 내용
        MsgDTO dto; // 결과 메시지 구조

        try {
            String nSeq = CmmUtil.nvl(request.getParameter("nSeq")); // 글번호(PK)

            /*
             * ####################################################################################
             * 반드시, 값을 받았으면, 꼭 로그를 찍어서 값이 제대로 들어오는지 파악해야함 반드시 작성할 것
             * ####################################################################################
             */
            log.info("nSeq : {}", nSeq);

            /*
             * 값 전달은 반드시 DTO 객체를 이용해서 처리함 전달 받은 값을 DTO 객체에 넣는다.
             */
            NoticeDTO pDTO = NoticeDTO.builder().noticeSeq(Long.parseLong(nSeq)).build();

            // 게시글 삭제하기 DB
            noticeService.deleteNoticeInfo(pDTO);

            msg = "삭제되었습니다.";

        } catch (Exception e) {
            msg = "실패하였습니다. : " + e.getMessage();
            log.info(e.toString());

        } finally {

            // 결과 메시지 전달하기
            dto = MsgDTO.builder().msg(msg).build();

            log.info("{}.quizNoticeDelete End!", this.getClass().getName());

        }

        return dto;
    }

}