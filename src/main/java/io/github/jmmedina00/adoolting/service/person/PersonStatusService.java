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

  @Autowired
  private PersonService personService;

  public PersonStatus updatePersonStatus(Long personId, String content) {
    Person person = personService.getPerson(personId);
    if (person == null) {
      return null;
    }

    PersonStatus status = new PersonStatus();
    status.setContent(content);
    status.setPerson(person);
    return statusRepository.save(status);
  }
}
