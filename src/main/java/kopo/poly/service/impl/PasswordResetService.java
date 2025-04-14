package kopo.poly.service.impl;

import jakarta.transaction.Transactional;
import kopo.poly.dto.PasswordResetDTO;
import kopo.poly.repository.UserInfoRepository;
import kopo.poly.repository.PasswordResetRepository;
import kopo.poly.repository.entity.PasswordResetEntity;
import kopo.poly.repository.entity.UserInfoEntity;
import kopo.poly.service.IPasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService implements IPasswordResetService {

    private final PasswordResetRepository tokenRepo;
    private final UserInfoRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public String createResetToken(String email) {
        String token = UUID.randomUUID().toString();

        PasswordResetEntity entity = PasswordResetEntity.builder()
                .token(token)
                .email(email)
                .expiry(LocalDateTime.now().plusMinutes(30))
                .used(false)
                .build();

        tokenRepo.save(entity);

        return token;
    }

    @Override
    public boolean isValidToken(String token) {
        return tokenRepo.findById(token)
                .filter(t -> !t.isUsed() && t.getExpiry().isAfter(LocalDateTime.now()))
                .isPresent();
    }

    @Override
    @Transactional
    public boolean resetPassword(PasswordResetDTO dto) {
        Optional<PasswordResetEntity> optional = tokenRepo.findById(dto.getToken());

        if (optional.isEmpty()) return false;

        PasswordResetEntity token = optional.get();
        if (token.isUsed() || token.getExpiry().isBefore(LocalDateTime.now())) return false;

        Optional<UserInfoEntity> userOpt = userRepo.findByEmail(token.getEmail());
        if (userOpt.isEmpty()) return false;

        UserInfoEntity user = userOpt.get();
        user.changePassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepo.save(user);

        token.setUsed(true);
        tokenRepo.save(token);

        return true;
    }
}
