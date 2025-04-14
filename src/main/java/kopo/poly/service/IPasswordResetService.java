package kopo.poly.service;

import kopo.poly.dto.PasswordResetDTO;

public interface IPasswordResetService {

    String createResetToken(String email);

    boolean resetPassword(PasswordResetDTO dto);

    boolean isValidToken(String token);
}
