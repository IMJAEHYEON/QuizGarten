package kopo.poly.service.impl;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import kopo.poly.util.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class S3Service {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final AmazonS3 amazonS3;

    /* 1. 파일 업로드 */
    public String upload(MultipartFile multipartFile, String s3FileName) throws IOException {
        ObjectMetadata objMeta = new ObjectMetadata();
        objMeta.setContentLength(multipartFile.getInputStream().available());

        amazonS3.putObject(bucket, s3FileName, multipartFile.getInputStream(), objMeta);

        return URLDecoder.decode(amazonS3.getUrl(bucket, s3FileName).toString(), "utf-8");
    }

    /* 2. 파일 삭제 */
    public void delete(String keyName) {
        try {
            amazonS3.deleteObject(bucket, keyName);
        } catch (AmazonServiceException e) {
            log.error(e.toString());
        }
    }

    /* 3. presigned URL 발급 */
    public String getPresignedURL(String keyName) {
        String preSignedURL = "";
        Date expiration = new Date();
        expiration.setTime(expiration.getTime() + 1000 * 60 * 2); // 2분

        try {
            GeneratePresignedUrlRequest generatePresignedUrlRequest =
                    new GeneratePresignedUrlRequest(bucket, keyName)
                            .withMethod(HttpMethod.GET)
                            .withExpiration(expiration);

            URL url = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);
            preSignedURL = url.toString();

        } catch (Exception e) {
            log.error(e.toString());
        }

        return preSignedURL;
    }

    /* 4. submitFiles: 여러 파일 처리 */
    public ResponseEntity<?> submitFiles(List<MultipartFile> multipartFileList) throws IOException {

        List<String> uploadedUrls = new ArrayList<>();

        for (MultipartFile multipartFile : multipartFileList) {
            String originalFilename = multipartFile.getOriginalFilename();
            String fileExtension = FileUtil.getFileExtension(originalFilename);
            String fileTypeFolder = FileUtil.getFileTypeFolder(fileExtension);

            String uuidFileName = UUID.randomUUID() + "_" + originalFilename;
            String s3FilePath = fileTypeFolder + "/" + uuidFileName;

            String uploadedUrl = this.upload(multipartFile, s3FilePath);
            uploadedUrls.add(uploadedUrl);
        }

        return ResponseEntity.ok().body(uploadedUrls);
    }
}
