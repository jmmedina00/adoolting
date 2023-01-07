package io.github.jmmedina00.adoolting.service.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.jmmedina00.adoolting.dto.person.NewMessage;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.entity.person.PrivateMessage;
import io.github.jmmedina00.adoolting.repository.person.PrivateMessageRepository;
import io.github.jmmedina00.adoolting.service.cache.PersonLatestMessagesService;
import io.github.jmmedina00.adoolting.util.MethodDoesThatNameGenerator;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayNameGeneration(MethodDoesThatNameGenerator.class)
public class PrivateMessageServiceTest {
  @MockBean
  private PrivateMessageRepository messageRepository;

  @MockBean
  private PersonLatestMessagesService latestMessagesService;

  @MockBean
  private PersonService personService;

  @Autowired
  private PrivateMessageService messageService;

  @Test
  public void ensureCacheIsUpToDateOnlySendsNonEmptyMessageListsToCacheService() {
    Person a = new Person();
    a.setId(1L);
    Person b = new Person();
    b.setId(2L);
    Person c = new Person();
    c.setId(3L);
    Person d = new Person();
    d.setId(4L);

    PrivateMessage foo = new PrivateMessage();
    PrivateMessage bar = new PrivateMessage();
    PrivateMessage baz = new PrivateMessage();

    List<PrivateMessage> forB = List.of(foo);
    List<PrivateMessage> forC = List.of(bar, baz);

    Mockito
      .when(personService.getAllActivePersons())
      .thenReturn(List.of(a, b, c, d));
    Mockito
      .when(messageRepository.findMessagesExchangedWithPerson(1L))
      .thenReturn(List.of());
    Mockito
      .when(messageRepository.findMessagesExchangedWithPerson(2L))
      .thenReturn(forB);
    Mockito
      .when(messageRepository.findMessagesExchangedWithPerson(3L))
      .thenReturn(forC);
    Mockito
      .when(messageRepository.findMessagesExchangedWithPerson(4L))
      .thenReturn(List.of());

    messageService.ensureCacheIsUpToDate();

    verify(latestMessagesService, never()).verifyPersonCache(eq(1L), anyList());
    verify(latestMessagesService, times(1)).verifyPersonCache(eq(2L), eq(forB));
    verify(latestMessagesService, times(1)).verifyPersonCache(eq(3L), eq(forC));
    verify(latestMessagesService, never()).verifyPersonCache(eq(4L), anyList());
  }

  @Test
  public void getMessagesBetweenPersonsSetsCacheOfSenderAsReadForReceiever() {
    PageRequest request = PageRequest.of(5, 12);

    messageService.getMessagesBetweenPersons(4L, 5L, request);

    verify(latestMessagesService, times(1)).setMessageFromPersonAsRead(4L, 5L);
    verify(messageRepository, times(1))
      .findMessagesByPersonIds(4L, 5L, request);
  }

  @Test
  public void sendMessageToPersonPopulatesPrivateMessageCorrectlyAndSendsItToCacheService() {
    Person foo = new Person();
    Person bar = new Person();

    NewMessage newMessage = new NewMessage();
    newMessage.setContents("Send me a message");

    Mockito.when(personService.getPerson(4L)).thenReturn(foo);
    Mockito.when(personService.getPerson(5L)).thenReturn(bar);
    Mockito
      .when(messageRepository.save(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));

    PrivateMessage message = messageService.sendMessageToPerson(
      4L,
      5L,
      newMessage
    );
    assertEquals(foo, message.getFromPerson());
    assertEquals(bar, message.getToPerson());
    assertEquals("Send me a message", message.getContents());

    verify(messageRepository, times(1)).save(message);
    verify(latestMessagesService, times(1)).saveMessageToCache(message);
  }
}
