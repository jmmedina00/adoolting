package io.github.jmmedina00.adoolting.service.person;

import io.github.jmmedina00.adoolting.dto.PersonInfo;
import io.github.jmmedina00.adoolting.dto.User;
import io.github.jmmedina00.adoolting.dto.util.SecureDeletion;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.entity.util.PersonDetails;
import io.github.jmmedina00.adoolting.repository.PersonRepository;
import io.github.jmmedina00.adoolting.service.cache.PersonLocaleConfigService;
import io.github.jmmedina00.adoolting.service.util.ConfirmationService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindException;
import org.springframework.validation.DirectFieldBindingResult;

@Service
public class PersonService implements UserDetailsService {
  @Autowired
  private PersonRepository personRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private ConfirmationService confirmationService;

  @Autowired
  private PersonSettingsService settingsService;

  @Autowired
  private PersonLocaleConfigService localeConfigService;

  @Autowired
  private PersonStatusService statusService;

  private static final Logger logger = LoggerFactory.getLogger(
    PersonService.class
  );

  public Person getPerson(Long personId) {
    return personRepository.findActivePerson(personId).orElseThrow();
  }

  public List<Person> getAllActivePersons() {
    return personRepository.findConfirmedPersons();
  }

  public List<Person> getPersonsWhoLikedPage(Long pageId) {
    return personRepository.findPersonsWhoLikedPage(pageId);
  }

  public PersonInfo getPersonInfo(Long personId) {
    Person person = getPerson(personId);
    PersonInfo info = new PersonInfo();
    info.setFirstName(person.getFirstName());
    info.setLastName(person.getLastName());
    info.setGender(person.getGender());
    info.setAbout(person.getAbout());

    logger.info("Fetched person {} form info", personId);
    return info;
  }

  public Person getPersonWithMatchingPassword(
    Long personId,
    SecureDeletion confirmation
  )
    throws Exception {
    Person person = getPerson(personId);
    String encodedPassword = person.getPassword();
    if (
      passwordEncoder.matches(confirmation.getPassword(), encodedPassword)
    ) return person;

    logger.debug(
      "Supplied password for person {} is not correct. Preparing rejected value Exception",
      personId
    );

    DirectFieldBindingResult result = new DirectFieldBindingResult(
      confirmation,
      "confirm"
    );
    result.rejectValue("password", "error.password.incorrect");
    throw new BindException(result);
  }

  public Person updatePerson(Long personId, PersonInfo info) {
    Person person = getPerson(personId);

    person.setFirstName(info.getFirstName());
    person.setLastName(info.getLastName());
    person.setGender(info.getGender());
    person.setAbout(info.getAbout());

    Person saved = personRepository.save(person);
    logger.info("Updated person {} information", personId);
    statusService.updatePersonStatus(person, info.getStatus());
    return saved;
  }

  public Person changePersonPassword(Long personId, String newPassword) {
    Person person = getPerson(personId);
    person.setPassword(passwordEncoder.encode(newPassword));

    logger.info("Password for person {} has been updated", personId);
    return personRepository.save(person);
  }

  public Person createPersonFromUser(User userDto) throws BindException {
    if (isEmailAlreadyUsed(userDto.getEmail())) {
      logger.debug(
        "Email {} is used, preparing rejected value exception",
        userDto.getEmail()
      );
      DirectFieldBindingResult result = new DirectFieldBindingResult(
        userDto,
        "user"
      );
      result.rejectValue("email", "error.email.used");
      throw new BindException(result);
    }

    logger.debug(
      "Email {} is correct, proceeding with creation",
      userDto.getEmail()
    );

    Person person = new Person();
    person.setFirstName(userDto.getFirstName());
    person.setLastName(userDto.getLastName());
    person.setEmail(userDto.getEmail());
    person.setBirthDate(userDto.getBirthday());
    person.setGender(userDto.getGender());
    person.setPassword(passwordEncoder.encode(userDto.getPassword()));

    Person saved = personRepository.save(person);
    logger.info("New person has registered, id is {}", saved.getId());
    confirmationService.createTokenforPerson(saved);
    settingsService.createSettingsForPerson(saved);
    localeConfigService.refreshForPerson(saved.getId());
    return saved;
  }

  private boolean isEmailAlreadyUsed(String email) {
    return personRepository.findByEmail(email) != null;
  }

  @Override
  public UserDetails loadUserByUsername(String email)
    throws UsernameNotFoundException {
    logger.debug("Loading details for person with email {}", email);
    Person person = personRepository.findByEmail(email);
    if (person == null) {
      logger.debug("Email {} not found", email);
      throw new UsernameNotFoundException(email);
    }

    logger.debug("Email {} found. Providing details", email);
    PersonDetails details = new PersonDetails(person);
    return details;
  }
}
