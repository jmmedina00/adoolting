package io.github.jmmedina00.adoolting.entity.cache.simple;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.jmmedina00.adoolting.entity.enums.Gender;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.util.MethodDoesThatNameGenerator;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayNameGeneration(MethodDoesThatNameGenerator.class)
public class SimplePersonTest {

  @Test
  public void constructorTakesRelevantPersonInformation() {
    Person a = new Person();
    a.setId(13L);
    a.setFirstName("Juan");
    a.setLastName("Medina");
    a.setEmail("juanmi@test.local");
    a.setGender(Gender.HE);

    SimplePerson person = new SimplePerson(a);

    assertEquals(a.getId(), person.getId());
    assertEquals(a.getFirstName(), person.getFirstName());
    assertEquals(a.getLastName(), person.getLastName());
    assertEquals(a.getEmail(), person.getEmail());
    assertEquals(a.getGender(), person.getGender());
  }
}
