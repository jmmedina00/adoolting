package io.github.jmmedina00.adoolting.repository.util;

import io.github.jmmedina00.adoolting.entity.util.ConfirmationToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfirmationTokenRepository
  extends JpaRepository<ConfirmationToken, Long> {
  ConfirmationToken findByToken(String token);
}
