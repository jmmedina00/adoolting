package io.github.jmmedina00.adoolting.repository.util;

import io.github.jmmedina00.adoolting.entity.util.PasswordRestoreToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordRestoreTokenRepository
  extends JpaRepository<PasswordRestoreToken, Long> {
  PasswordRestoreToken findByToken(String token);
}
