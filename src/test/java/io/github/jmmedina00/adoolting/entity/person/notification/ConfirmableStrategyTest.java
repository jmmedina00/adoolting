package io.github.jmmedina00.adoolting.entity.person.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.jmmedina00.adoolting.entity.ConfirmableInteraction;
import io.github.jmmedina00.adoolting.entity.cache.EmailData;
import io.github.jmmedina00.adoolting.entity.enums.Gender;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.util.MethodDoesThatNameGenerator;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayNameGeneration(MethodDoesThatNameGenerator.class)
public class ConfirmableStrategyTest {
  ConfirmableStrategy strategy = new ConfirmableStrategy();
  Person person;

  @BeforeEach
  public void setUpPerson() {
    person = new Person();
    person.setId(13L);
    person.setFirstName("Juan");
    person.setLastName("Medina");
    person.setEmail("juanmi@test.local");
    person.setGender(Gender.HE);
  }

  @Test
  public void generateDataHasFriendAddendum() {
    Person commenter = Mockito.mock(Person.class);
    Mockito.when(commenter.getFullName()).thenReturn("Maria Hernandez");

    ConfirmableInteraction interaction = new ConfirmableInteraction();
    interaction.setId(18L);
    interaction.setConfirmedAt(null);
    interaction.setInteractor(commenter);
    interaction.setReceiverInteractor(person);

    EmailData data = strategy.generateData(interaction, person);
    assertEquals("friend", data.getSubjectAddendum());
    assertEquals(
      List.of("Maria Hernandez", "Juan Medina"),
      data.getSubjectArguments()
    );
  }

  @Test
  public void generateDataReversesArgumentsOrderIfInteractionIsAccepted() {
    Person commenter = Mockito.mock(Person.class);
    Mockito.when(commenter.getFullName()).thenReturn("Maria Hernandez");

    ConfirmableInteraction interaction = new ConfirmableInteraction();
    interaction.setId(18L);
    interaction.setConfirmedAt(new Date(500));
    interaction.setInteractor(person);
    interaction.setReceiverInteractor(commenter);

    EmailData data = strategy.generateData(interaction, person);
    assertEquals("friend", data.getSubjectAddendum());
    assertEquals(
      List.of("Maria Hernandez", "Juan Medina"),
      data.getSubjectArguments()
    );
  }
}
