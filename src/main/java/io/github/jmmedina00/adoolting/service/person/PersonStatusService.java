package io.github.jmmedina00.adoolting.service.person;

import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.entity.person.PersonStatus;
import io.github.jmmedina00.adoolting.repository.person.PersonStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PersonStatusService {
  @Autowired
  private PersonStatusRepository statusRepository;

  public PersonStatus getPersonStatus(Long personId) {
    return statusRepository.findFirst1ByPersonIdOrderByCreatedAtDesc(personId);
  }

  public PersonStatus updatePersonStatus(Person person, String content) {
    if (content.isEmpty()) {
      return null;
    }

    PersonStatus status = new PersonStatus();
    status.setContent(content);
    status.setPerson(person);
    return statusRepository.save(status);
  }
}
