package kopo.poly.repository;

import kopo.poly.repository.entity.PasswordResetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetRepository extends JpaRepository<PasswordResetEntity, String> {
}
