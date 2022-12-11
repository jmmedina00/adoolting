package io.github.jmmedina00.adoolting.repository.util;

import io.github.jmmedina00.adoolting.entity.util.ConfirmationToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ConfirmationTokenRepository
  extends JpaRepository<ConfirmationToken, Long> {
  @Query(
    "SELECT t FROM ConfirmationToken t WHERE t.token=:token " +
    "AND t.confirmedAt IS NULL AND t.expiresAt > CURRENT_TIMESTAMP"
  )
  Optional<ConfirmationToken> findToken(String token);
}
