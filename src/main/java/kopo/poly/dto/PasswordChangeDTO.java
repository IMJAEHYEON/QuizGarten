package kopo.poly.dto;

import lombok.Builder;

@Builder(toBuilder = true)
public record PasswordChangeDTO( // 비밀번호 변경을 위한 DTO

        String userId,    // 세션에서 가져올 유저 아이디
        String password   // 새 비밀번호
) {
}
