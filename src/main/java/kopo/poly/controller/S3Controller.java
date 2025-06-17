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

    /**
     * 다중 파일 업로드 (image/audio 분리됨)
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(@RequestParam("files") List<MultipartFile> multipartFileList) throws Exception {
        return s3Service.submitFiles(multipartFileList);
    }

    /**
     * 단일 파일 업로드 (image/audio 분리됨)
     */
    @PostMapping("/SingleUpload")
    public ResponseEntity<String> uploadFile(@RequestPart("thumbnail") MultipartFile file) throws Exception {
        String fileUrl = s3Service.uploadFileToS3(file);
        return ResponseEntity.ok(fileUrl);
    }



}
