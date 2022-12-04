package io.github.jmmedina00.adoolting.repository.cache;

import io.github.jmmedina00.adoolting.entity.cache.PersonLatestMessages;
import org.springframework.data.repository.CrudRepository;

public interface PersonLatestMessagesRepository
  extends CrudRepository<PersonLatestMessages, Long> {}
