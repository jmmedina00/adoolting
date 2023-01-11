package io.github.jmmedina00.adoolting.entity.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.jmmedina00.adoolting.entity.cache.EmailData;
import io.github.jmmedina00.adoolting.entity.enums.Gender;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.util.MethodDoesThatNameGenerator;
import java.util.Map;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayNameGeneration(MethodDoesThatNameGenerator.class)
public class ConfirmationTokenTest {

  @Test
  public void getEmailDataCreatesEmailDataWithPersonInfoAndToken() {
    ConfirmationToken token = new ConfirmationToken();
    token.setToken("ThisIsAToken");

    Person person = new Person();
    person.setId(13L);
    person.setFirstName("Juan");
    person.setLastName("Medina");
    person.setEmail("juanmi@test.local");
    person.setGender(Gender.HE);

    token.setPerson(person);

    EmailData data = token.getEmailData();
    assertEquals(person.getId(), data.getPerson().getId()); // Person to SimplePerson transform, assuming in place
    assertEquals(
      Map.of("name", "Juan", "token", "ThisIsAToken"),
      data.getParameters()
    );
  }
}
