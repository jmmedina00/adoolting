package io.github.jmmedina00.adoolting.repository.util;

import io.github.jmmedina00.adoolting.entity.util.PasswordRestoreToken;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PasswordRestoreTokenRepository
  extends JpaRepository<PasswordRestoreToken, Long> {
  @Query(
    "SELECT t FROM PasswordRestoreToken t WHERE t.token=:token " +
    "AND t.usedAt IS NULL AND t.expiresAt > CURRENT_TIMESTAMP"
  )
  Optional<PasswordRestoreToken> findToken(@Param("token") String token);
}
