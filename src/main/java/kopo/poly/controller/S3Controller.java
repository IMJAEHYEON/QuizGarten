package kopo.poly.controller;

import kopo.poly.service.impl.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/s3")
@RequiredArgsConstructor
public class S3Controller {

    private final S3Service s3Service;

    @PostMapping("/upload")
    public ResponseEntity<?> submitFiles(@RequestParam("files") List<MultipartFile> multipartFileList) throws Exception {
        return s3Service.submitFiles(multipartFileList);
    }
}
