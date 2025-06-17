package kopo.poly.util;

import kopo.poly.service.impl.S3Service; // S3Service의 상수 재활용을 위해 import

import java.util.List;

public class FileUtil {

    /**
     * 파일 확장자 추출
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) return "";
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }

    /**
     * 허용된 확장자인지 확인
     */
    public static boolean isAllowedExtension(String ext) {
        return getAllAllowedExtensions().contains(ext);
    }

    /**
     * 허용된 모든 확장자 목록
     */
    public static List<String> getAllAllowedExtensions() {
        return List.of("jpg", "jpeg", "png", "gif", "bmp", "webp", "mp3", "wav", "ogg", "m4a", "flac");
    }

    /**
     * 확장자 기반 업로드 폴더 반환 (S3Service 상수 재활용)
     */
    public static String getFileTypeFolder(String extension) {
        if (isAudioExtension(extension)) {
            return S3Service.AUDIO_FOLDER;
        } else {
            return S3Service.IMAGE_FOLDER;
        }
    }

    /**
     * 오디오 확장자인지 여부
     */
    public static boolean isAudioExtension(String extension) {
        List<String> audioExts = List.of("mp3", "wav", "ogg", "m4a", "flac");
        return audioExts.contains(extension);
    }
}
