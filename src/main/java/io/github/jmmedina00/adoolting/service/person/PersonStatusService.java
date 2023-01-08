package io.github.jmmedina00.adoolting.service.person;

import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.entity.person.PersonStatus;
import io.github.jmmedina00.adoolting.repository.person.PersonStatusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PersonStatusService {
  @Autowired
  private PersonStatusRepository statusRepository;

  private static final Logger logger = LoggerFactory.getLogger(
    PersonStatusService.class
  );

  public PersonStatus getPersonStatus(Long personId) {
    return statusRepository.findFirst1ByPersonIdOrderByCreatedAtDesc(personId);
  }

  public PersonStatus updatePersonStatus(Person person, String content) {
    if (content.isEmpty()) {
      logger.debug(
        "Provided update for person {} is empty. Skipping.",
        person.getId()
      );
      return null;
    }

    PersonStatus status = new PersonStatus();
    status.setContent(content);
    status.setPerson(person);
    PersonStatus saved = statusRepository.save(status);
    logger.info(
      "Status has been updated for person {}, id {}",
      person.getId(),
      saved.getId()
    );
    return saved;
  }
}
