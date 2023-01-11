package io.github.jmmedina00.adoolting.entity.cache.simple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.jmmedina00.adoolting.entity.person.PrivateMessage;
import io.github.jmmedina00.adoolting.util.MethodDoesThatNameGenerator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayNameGeneration(MethodDoesThatNameGenerator.class)
public class SimpleMessageTest {

  @Test
  public void constructorTakesRelevantPrivateMessageInformationAndAssumesItIsRead() {
    PrivateMessage pm = new PrivateMessage();
    pm.setContents("This is a test");
    pm.setCreatedAt(new Date(500L));

    SimpleMessage message = new SimpleMessage(pm);
    assertEquals(pm.getContents(), message.getContents());
    assertEquals(pm.getCreatedAt(), message.getCreatedAt());
    assertTrue(message.getIsRead());
  }

  @Test
  public void compareToSortsSimpleMessagesByCreatedAtDateDescendingly() {
    SimpleMessage a = new SimpleMessage();
    a.setCreatedAt(new Date(500));
    SimpleMessage b = new SimpleMessage();
    b.setCreatedAt(new Date(700));
    SimpleMessage c = new SimpleMessage();
    c.setCreatedAt(new Date(200));
    SimpleMessage d = new SimpleMessage();
    d.setCreatedAt(new Date(600));

    List<SimpleMessage> expected = List.of(b, d, a, c);
    List<SimpleMessage> actual = new ArrayList<>(List.of(a, b, c, d));
    Collections.sort(actual);

    assertEquals(expected, actual);
  }
}
