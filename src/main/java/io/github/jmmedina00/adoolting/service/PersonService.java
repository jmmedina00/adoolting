package io.github.jmmedina00.adoolting.service;

import io.github.jmmedina00.adoolting.dto.User;
import io.github.jmmedina00.adoolting.entity.Person;
import io.github.jmmedina00.adoolting.entity.util.PersonDetails;
import io.github.jmmedina00.adoolting.exception.EmailIsUsedException;
import io.github.jmmedina00.adoolting.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PersonService implements UserDetailsService {
  @Autowired
  private PersonRepository personRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private ConfirmationService confirmationService;

  public Person getPerson(Long personId) {
    return personRepository.findById(personId).orElse(null);
  }

  public Person changePersonPassword(Long personId, String newPassword) {
    Person person = personRepository.findById(personId).orElse(null);

    if (person == null) {
      return null;
    }

    person.setPassword(passwordEncoder.encode(newPassword));
    return personRepository.save(person);
  }

  public Person createPersonFromUser(User userDto) throws EmailIsUsedException {
    if (isEmailAlreadyUsed(userDto.getEmail())) {
      throw new EmailIsUsedException();
    }

    Person person = new Person();
    person.setFirstName(userDto.getFirstName());
    person.setLastName(userDto.getLastName());
    person.setEmail(userDto.getEmail());
    person.setBirthDate(userDto.getBirthday());
    person.setGender(userDto.getGender());
    person.setPassword(passwordEncoder.encode(userDto.getPassword()));

    Person saved = personRepository.save(person);
    confirmationService.createTokenforPerson(saved);
    return saved;
  }

  private boolean isEmailAlreadyUsed(String email) {
    return personRepository.findByEmail(email) != null;
  }

  @Override
  public UserDetails loadUserByUsername(String email)
    throws UsernameNotFoundException {
    Person person = personRepository.findByEmail(email);
    if (person == null) {
      throw new UsernameNotFoundException(email);
    }

    PersonDetails details = new PersonDetails(person);
    return details;
  }
}
