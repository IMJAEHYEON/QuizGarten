package kopo.poly.dto;
import lombok.Builder;


@Builder
public record PasswordResetDTO( // 비밀번호 재설정 이메일을 보낼때 사용

        String userId, // 유저 아이디
        String email // 이메일
) {
}
