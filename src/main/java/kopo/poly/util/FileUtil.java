package kopo.poly.util;

import java.util.List;

public class FileUtil {

    // 파일 확장자 추출
    public static String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }

    // 이미지 / 오디오 / 기타 폴더 구분
    public static String getFileTypeFolder(String extension) {
        List<String> imageExts = List.of("jpg", "jpeg", "png", "gif", "bmp", "webp");
        List<String> audioExts = List.of("mp3", "wav", "ogg", "m4a", "flac");

        if (imageExts.contains(extension)) {
            return "images";
        } else if (audioExts.contains(extension)) {
            return "audios";
        } else {
            return "others";
        }
    }
}
