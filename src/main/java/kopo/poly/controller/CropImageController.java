package kopo.poly.controller;

import kopo.poly.service.IS3Service;
import kopo.poly.util.MultipartFileAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/crop")
public class CropImageController {

    private final IS3Service s3Service;

    @PostMapping("/image")
    public ResponseEntity<String> cropAndUploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("x") int x,
            @RequestParam("y") int y,
            @RequestParam("width") int width,
            @RequestParam("height") int height
    ) throws Exception {

        log.info("Start cropping image");

        // 원본 이미지 로드
        BufferedImage originalImage = ImageIO.read(file.getInputStream());

        // 자르기 범위 보정
        int validWidth = Math.min(width, originalImage.getWidth() - x);
        int validHeight = Math.min(height, originalImage.getHeight() - y);

        // 이미지 자르기
        BufferedImage croppedImage = originalImage.getSubimage(x, y, validWidth, validHeight);

        // 자른 이미지를 바이트 배열로 변환
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(croppedImage, "png", baos);
        byte[] imageBytes = baos.toByteArray();

        // MultipartFile 변환
        MultipartFile multipartFile = new MultipartFileAdapter(new ByteArrayInputStream(imageBytes), "cropped.png");

        // S3 업로드
        String uploadedUrl = s3Service.uploadFileToS3(multipartFile);

        log.info("Image uploaded to: {}", uploadedUrl);

        return ResponseEntity.ok(uploadedUrl);
    }
}
