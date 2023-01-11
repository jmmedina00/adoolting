package io.github.jmmedina00.adoolting.entity.person;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.jmmedina00.adoolting.util.MethodDoesThatNameGenerator;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayNameGeneration(MethodDoesThatNameGenerator.class)
public class PersonTest {

  private static Stream<Arguments> nameCombinations() {
    return Stream.of(
      Arguments.of("Juan", "Medina", "Juan Medina"),
      Arguments.of("Bob", "Johnson", "Bob Johnson"),
      Arguments.of("José Manuel", "Perez Perez", "José Manuel Perez Perez"),
      Arguments.of("音操", "桜庭", "音操 桜庭")
    );
  }

  @ParameterizedTest
  @MethodSource("nameCombinations")
  public void getFullNameReturnsFirstAndLastNameCombined(
    String first,
    String last,
    String expected
  ) {
    Person person = new Person();
    person.setFirstName(first);
    person.setLastName(last);

    assertEquals(expected, person.getFullName());
  }
}
