package kopo.poly.service.impl;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import kopo.poly.service.IS3Service;
import kopo.poly.util.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.net.URL;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;


@Slf4j
@RequiredArgsConstructor
@Service
public class S3Service implements IS3Service {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final AmazonS3 amazonS3;

    // S3 관련 상수
    public static final String AUDIO_FOLDER = "audios/";
    public static final String IMAGE_FOLDER = "images/";
    public static final String S3_BASE_URL_PREFIX = "https://s3.amazonaws.com/";

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB 제한

    @Override
    public String upload(MultipartFile multipartFile, String s3FileName) throws IOException {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getInputStream().available());
        amazonS3.putObject(bucket, s3FileName, multipartFile.getInputStream(), metadata);
        return URLDecoder.decode(amazonS3.getUrl(bucket, s3FileName).toString(), "utf-8");
    }

    @Override
    public String uploadText(String keyName, String content) throws IOException {
        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(contentBytes.length);
        metadata.setContentType("text/plain");

        try (InputStream stream = new ByteArrayInputStream(contentBytes)) {
            amazonS3.putObject(bucket, keyName, stream, metadata);
        }

        return amazonS3.getUrl(bucket, keyName).toString();
    }

    @Override
    public void delete(String keyName) {
        try {
            amazonS3.deleteObject(bucket, keyName);
            log.info("S3 삭제 성공: {}", keyName);
        } catch (AmazonServiceException e) {
            log.error("S3 삭제 오류: {}", e.getMessage());
        }
    }

    /**
     * S3 URL에서 파일 경로 추출 (리전 포함 URL 지원)
     * 예: https://my-quizbucket.s3.ap-northeast-2.amazonaws.com/audio-links/abc.txt
     *     → audio-links/abc.txt
     */
    public String extractS3FilePath(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            return url.getPath().substring(1); // "/audio-links/abc.txt" → "audio-links/abc.txt"
        } catch (Exception e) {
            log.error("S3 URL 파싱 실패: {}", fileUrl, e);
            throw new IllegalArgumentException("S3 URL 형식이 올바르지 않습니다.");
        }
    }


    @Override
    public String getPresignedURL(String keyName) {
        Date expiration = new Date(System.currentTimeMillis() + 1000 * 60 * 2); // 2분

        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, keyName)
                .withMethod(HttpMethod.GET)
                .withExpiration(expiration);

        return amazonS3.generatePresignedUrl(request).toString();
    }

    @Override
    public ResponseEntity<?> submitFiles(List<MultipartFile> multipartFileList) throws IOException {
        List<String> uploadedUrls = new ArrayList<>();

        for (MultipartFile file : multipartFileList) {
            String originalFilename = Optional.ofNullable(file.getOriginalFilename()).orElse("");
            String extension = FileUtil.getFileExtension(originalFilename);

            if (!FileUtil.isAllowedExtension(extension)) {
                log.warn("허용되지 않은 확장자 업로드 시도: {}", extension);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("허용되지 않은 파일 유형입니다.");
            }

            if (file.getSize() > MAX_FILE_SIZE) {
                log.warn("파일 크기 초과: {} bytes", file.getSize());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("5MB 이하의 파일만 업로드 가능합니다.");
            }

            String uuid = UUID.randomUUID().toString();
            String fileTypeFolder = getFolderByExtension(extension);
            String s3FilePath = fileTypeFolder + uuid + "_" + originalFilename;

            String uploadedUrl = this.upload(file, s3FilePath);
            uploadedUrls.add(uploadedUrl);
        }

        return ResponseEntity.ok(uploadedUrls);
    }

    /**
     * 확장자 기반으로 S3 폴더 경로 결정
     * @param extension 파일 확장자
     * @return 폴더 경로 (audios/ 또는 images/)
     */
    private String getFolderByExtension(String extension) {
        if (FileUtil.isAudioExtension(extension)) {
            return AUDIO_FOLDER;
        } else {
            return IMAGE_FOLDER;
        }
    }
    @Override
    public String getBaseUrlPrefix() {
        return S3_BASE_URL_PREFIX + bucket + "/";
    }

    @Override
    public String uploadFileToS3(MultipartFile file) throws Exception {
        if (file != null && !file.isEmpty()) {
            String extension = FileUtil.getFileExtension(file.getOriginalFilename());
            String folderPath = FileUtil.getFileTypeFolder(extension);
            String uniqueFileName = folderPath + UUID.randomUUID() + "_" + file.getOriginalFilename();
            return this.upload(file, uniqueFileName);
        }
        return null;
    }

    @Override
    public String uploadFile(MultipartFile file) throws Exception {
        log.info("uploadFile Start!");

        String fileName = "images/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        amazonS3.putObject(bucket, fileName, file.getInputStream(), metadata);

        String fileUrl = amazonS3.getUrl(bucket, fileName).toString();

        log.info("uploadFile End! URL: {}", fileUrl);
        return fileUrl;
    }

    @Override
    public void deleteFile(String fileUrl) throws Exception {
        log.info("deleteFile Start! fileUrl: {}", fileUrl);

        if (fileUrl == null || !fileUrl.contains("/")) {
            log.warn("Invalid fileUrl: {}", fileUrl);
            return;
        }

        String key = fileUrl.substring(fileUrl.indexOf("amazonaws.com/") + "amazonaws.com/".length());

        amazonS3.deleteObject(bucket, key);

        log.info("deleteFile End! key: {}", key);
    }

    @Override
    @Transactional
    public String replaceS3File(String oldFileUrl, MultipartFile newFile) throws Exception {
        log.info("replaceS3File Start - oldFileUrl: {}", oldFileUrl);

        if (StringUtils.hasText(oldFileUrl)) {
            deleteFile(oldFileUrl); // 기존 파일 삭제
        }

        String newUrl = uploadFile(newFile); // 새 파일 업로드
        log.info("replaceS3File End - newUrl: {}", newUrl);
        return newUrl;
    }


    @Override // S3에 유튜브 링크를 .txt 로 변환하고 audios 객체로 저장
    public String uploadYoutubeLinkAsText(String quizId, String youtubeUrl) throws Exception {
        log.info("uploadYoutubeLinkAsText Start!");

        byte[] contentBytes = youtubeUrl.getBytes(StandardCharsets.UTF_8);
        InputStream inputStream = new ByteArrayInputStream(contentBytes);

        String fileKey = "audio-links/" + quizId + ".txt";
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(contentBytes.length);
        metadata.setContentType("text/plain");

        amazonS3.putObject(bucket, fileKey, inputStream, metadata);

        String uploadedUrl = amazonS3.getUrl(bucket, fileKey).toString();

        log.info("uploadYoutubeLinkAsText End! : {}", uploadedUrl);
        return uploadedUrl; // 유튜브링크를 .txt 파일로 바꿔서 S3에 업로드
    }

    @Override
    public String readYoutubeLinkFromS3(String s3TxtUrl) throws Exception {
        log.info("readYoutubeLinkFromS3: {}", s3TxtUrl);

        String key = extractS3FilePath(s3TxtUrl);

        S3Object s3Object = amazonS3.getObject(bucket, key);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(s3Object.getObjectContent(), StandardCharsets.UTF_8))) {

            String youtubeUrl = reader.readLine();
            log.info("Read YouTube URL from S3: {}", youtubeUrl);
            return youtubeUrl;
        }
    }


}
