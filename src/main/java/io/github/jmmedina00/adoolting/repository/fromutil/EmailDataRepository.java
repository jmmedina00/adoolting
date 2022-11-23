package io.github.jmmedina00.adoolting.repository.fromutil;

import io.github.jmmedina00.adoolting.entity.util.EmailData;
import org.springframework.data.repository.CrudRepository;

public interface EmailDataRepository
  extends CrudRepository<EmailData, String> {}
