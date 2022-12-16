package io.github.jmmedina00.adoolting.repository.cache;

import io.github.jmmedina00.adoolting.entity.cache.LinkInformation;
import org.springframework.data.repository.CrudRepository;

public interface LinkInformationRepository
  extends CrudRepository<LinkInformation, Long> {}
