package kopo.poly.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IS3Service {

    String upload(MultipartFile multipartFile, String s3FileName) throws IOException;

    void delete(String keyName);

    String getPresignedURL(String keyName);

    ResponseEntity<?> submitFiles(List<MultipartFile> multipartFileList) throws IOException;

    String uploadText(String keyName, String content) throws IOException;

    String getBaseUrlPrefix();

    String extractS3FilePath(String fileUrl);

    String uploadFileToS3(MultipartFile file) throws Exception;
    String replaceS3File(String oldFileUrl, MultipartFile newFile) throws Exception;

    // 유튜브링크를 .txt 파일로 변환
    String uploadYoutubeLinkAsText(String quizId, String youtubeUrl) throws Exception;

    String readYoutubeLinkFromS3(String s3TxtUrl) throws Exception;

    String uploadFile(MultipartFile file) throws Exception;
    void deleteFile(String fileUrl) throws Exception;
}
