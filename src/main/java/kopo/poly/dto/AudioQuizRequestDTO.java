package kopo.poly.dto;

import lombok.Builder;

import java.util.List;
@Builder(toBuilder = true)
public record AudioQuizRequestDTO(

        String quizDetailId,
        String youtubeUrl, // 프론트에서 전달되는 유튜브 링크
        double audioStartTime,  // 시작 시간 (초 단위)
        double audioEndTime, // 끝 시간
        List<String> answers // 정답 리스트 (콤마 구분 X)
) {
}
