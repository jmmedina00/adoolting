package io.github.jmmedina00.adoolting.repository.cache;

import io.github.jmmedina00.adoolting.entity.cache.EmailData;
import org.springframework.data.repository.CrudRepository;

public interface EmailDataRepository
  extends CrudRepository<EmailData, String> {}
