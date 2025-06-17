package kopo.poly.dto;

import lombok.Builder;

import java.util.List;
@Builder(toBuilder = true)
public record QuizSaveRequestDTO(

        QuizDTO quiz,
        List<QuizDetailDTO> details
) {
}
