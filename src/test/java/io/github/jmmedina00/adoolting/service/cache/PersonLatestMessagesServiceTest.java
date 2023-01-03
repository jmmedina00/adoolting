package io.github.jmmedina00.adoolting.service.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.jmmedina00.adoolting.entity.cache.PersonLatestMessages;
import io.github.jmmedina00.adoolting.entity.cache.simple.SimpleMessage;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.entity.person.PrivateMessage;
import io.github.jmmedina00.adoolting.repository.cache.PersonLatestMessagesRepository;
import io.github.jmmedina00.adoolting.service.person.PersonService;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class PersonLatestMessagesServiceTest {
  @MockBean
  private PersonLatestMessagesRepository latestMessagesRepository;

  @MockBean
  private PersonService personService;

  @Autowired
  private PersonLatestMessagesService latestMessagesService;

  @Captor
  private ArgumentCaptor<PersonLatestMessages> messagesCaptor;

  @Test
  public void verifyPersonCacheRebuildsCacheIfCurrentCacheIsOutOfDate() {
    Person target = new Person();
    target.setId(4L);
    Person foo = new Person();
    foo.setId(5L);
    Person bar = new Person();
    bar.setId(7L);

    PrivateMessage a = new PrivateMessage();
    a.setCreatedAt(new Date(100L));
    a.setFromPerson(target);
    a.setToPerson(foo);
    a.setContents("This is A");

    PrivateMessage b = new PrivateMessage();
    b.setCreatedAt(new Date(500L));
    b.setFromPerson(foo);
    b.setToPerson(target);
    b.setContents("This is B");

    PrivateMessage c = new PrivateMessage();
    c.setCreatedAt(new Date(200L));
    c.setFromPerson(target);
    c.setToPerson(bar);
    c.setContents("This is C");

    PrivateMessage d = new PrivateMessage();
    d.setCreatedAt(new Date(50L));
    d.setFromPerson(bar);
    d.setToPerson(target);
    d.setContents("This is D");

    PersonLatestMessages badMessages = new PersonLatestMessages();
    badMessages.setUpdatedAt(new Date(150L));

    Mockito
      .when(latestMessagesRepository.findById(4L))
      .thenReturn(Optional.of(badMessages));

    latestMessagesService.verifyPersonCache(
      4L,
      List
        .of(a, b, c, d)
        .stream()
        .sorted((one, two) -> -one.getCreatedAt().compareTo(two.getCreatedAt()))
        .toList()
    );

    verify(latestMessagesRepository, times(1)).save(messagesCaptor.capture());

    PersonLatestMessages messages = messagesCaptor.getValue();
    assertEquals(4L, messages.getId());
    assertEquals(new Date(500L), messages.getUpdatedAt());

    Map<Long, SimpleMessage> result = messages.getMessages();
    assertEquals(2, result.size());

    SimpleMessage five = result.get(5L);
    assertEquals(new Date(500L), five.getCreatedAt());
    assertTrue(five.getIsRead());
    assertEquals("This is B", five.getContents());

    SimpleMessage seven = result.get(7L);
    assertEquals(new Date(200L), seven.getCreatedAt());
    assertTrue(seven.getIsRead());
    assertEquals("This is C", seven.getContents());

    assertNull(result.get(4L));
  }

  @Test
  public void verifyPersonCacheRebuildsCacheIfCurrentCacheIsMissing() {
    Person target = new Person();
    target.setId(4L);
    Person bar = new Person();
    bar.setId(7L);

    PrivateMessage c = new PrivateMessage();
    c.setCreatedAt(new Date(200L));
    c.setFromPerson(target);
    c.setToPerson(bar);
    c.setContents("This is C");

    Mockito
      .when(latestMessagesRepository.findById(4L))
      .thenReturn(Optional.empty());

    latestMessagesService.verifyPersonCache(4L, List.of(c));

    verify(latestMessagesRepository, times(1)).save(messagesCaptor.capture());

    PersonLatestMessages messages = messagesCaptor.getValue();
    assertEquals(4L, messages.getId());
    assertEquals(new Date(200L), messages.getUpdatedAt());

    Map<Long, SimpleMessage> result = messages.getMessages();
    assertEquals(1, result.size());

    SimpleMessage simpleMessage = result.get(7L);
    assertEquals(new Date(200L), simpleMessage.getCreatedAt());
    assertTrue(simpleMessage.getIsRead());
    assertEquals("This is C", simpleMessage.getContents());
  }

  @Test
  public void verifyPersonCacheDoesNothingIfCacheIsUpToDate() {
    Person target = new Person();
    target.setId(4L);
    Person bar = new Person();
    bar.setId(7L);

    PrivateMessage c = new PrivateMessage();
    c.setCreatedAt(new Date(200L));
    c.setFromPerson(target);
    c.setToPerson(bar);
    c.setContents("This is C");

    PersonLatestMessages messages = new PersonLatestMessages();
    messages.setUpdatedAt(new Date(200L));

    Mockito
      .when(latestMessagesRepository.findById(4L))
      .thenReturn(Optional.of(messages));

    latestMessagesService.verifyPersonCache(4L, List.of(c));

    verify(latestMessagesRepository, never()).save(any());
  }

  @Test
  public void saveMessageToCacheModifiesBothSenderAndReceiverCache() {
    Person foo = new Person();
    foo.setId(4L);
    Person bar = new Person();
    bar.setId(5L);

    PrivateMessage message = new PrivateMessage();
    message.setContents("This is a test");
    message.setFromPerson(foo);
    message.setToPerson(bar);
    message.setCreatedAt(new Date(50000L));
    SimpleMessage simpleMessage = new SimpleMessage();

    PersonLatestMessages fooMessages = new PersonLatestMessages();
    HashMap<Long, SimpleMessage> fooData = new HashMap<>();
    fooData.put(5L, simpleMessage);
    fooData.put(7L, simpleMessage);
    fooMessages.setMessages(fooData);
    fooMessages.setUpdatedAt(new Date(250L));

    PersonLatestMessages barMessages = new PersonLatestMessages();
    HashMap<Long, SimpleMessage> barData = new HashMap<>();
    barData.put(4L, simpleMessage);
    barData.put(7L, simpleMessage);
    barMessages.setMessages(barData);
    barMessages.setUpdatedAt(new Date(1000L));

    Mockito
      .when(latestMessagesRepository.findById(4L))
      .thenReturn(Optional.of(fooMessages));
    Mockito
      .when(latestMessagesRepository.findById(5L))
      .thenReturn(Optional.of(barMessages));

    latestMessagesService.saveMessageToCache(message);

    Map<Long, SimpleMessage> fooResult = fooMessages.getMessages();
    Map<Long, SimpleMessage> barResult = barMessages.getMessages();

    SimpleMessage resultingMessage = fooResult.get(5L);
    assertEquals("This is a test", resultingMessage.getContents());
    assertEquals(message.getCreatedAt(), resultingMessage.getCreatedAt());
    assertEquals(message.getCreatedAt(), fooMessages.getUpdatedAt());
    assertFalse(resultingMessage.getIsRead());

    assertEquals(message.getCreatedAt(), barMessages.getUpdatedAt());
    assertEquals(resultingMessage, barResult.get(4L));

    verify(latestMessagesRepository, times(1)).save(fooMessages);
    verify(latestMessagesRepository, times(1)).save(barMessages);
  }

  @Test
  public void setMessageFromPersonAsReadReadsFromReceiverAndSetsSimpleFromSenderAsRead() {
    PersonLatestMessages messages = new PersonLatestMessages();
    HashMap<Long, SimpleMessage> data = new HashMap<>();

    SimpleMessage five = new SimpleMessage();
    five.setIsRead(false);
    SimpleMessage seven = new SimpleMessage();
    seven.setIsRead(false);

    data.put(5L, five);
    data.put(7L, seven);
    messages.setMessages(data);

    Mockito
      .when(latestMessagesRepository.findById(4L))
      .thenReturn(Optional.of(messages));

    latestMessagesService.setMessageFromPersonAsRead(4L, 5L);

    Map<Long, SimpleMessage> result = messages.getMessages();
    assertTrue(result.get(5L).getIsRead());
    assertFalse(result.get(7L).getIsRead());

    verify(latestMessagesRepository, times(1)).save(messages);
  }

  @Test
  public void setMessageFromPersonAsReadInitializesNewMessageInsideCache() {
    PersonLatestMessages messages = new PersonLatestMessages();
    HashMap<Long, SimpleMessage> data = new HashMap<>();

    SimpleMessage seven = new SimpleMessage();
    seven.setIsRead(false);

    data.put(7L, seven);
    messages.setMessages(data);

    Mockito
      .when(latestMessagesRepository.findById(4L))
      .thenReturn(Optional.of(messages));

    latestMessagesService.setMessageFromPersonAsRead(4L, 5L);

    Map<Long, SimpleMessage> result = messages.getMessages();
    SimpleMessage simpleMessage = result.get(5L);
    assertNotNull(simpleMessage);
    assertEquals("", simpleMessage.getContents());
    assertTrue(simpleMessage.getIsRead());

    verify(latestMessagesRepository, times(1)).save(messages);
  }

  @Test
  public void setMessageFromPersonAsReadInitializesCacheWhenNotExistingAndContinuesAccordingly() {
    latestMessagesService.setMessageFromPersonAsRead(4L, 5L);
    verify(latestMessagesRepository, times(1)).save(messagesCaptor.capture());

    PersonLatestMessages messages = messagesCaptor.getValue();
    assertEquals(4L, messages.getId());

    Map<Long, SimpleMessage> result = messages.getMessages();
    assertEquals(1, result.size());

    SimpleMessage simpleMessage = result.get(5L);
    assertNotNull(simpleMessage);
    assertEquals("", simpleMessage.getContents());
    assertTrue(simpleMessage.getIsRead());
  }

  @Test
  public void getLatestMessagesFromPersonPlacesCorrectPersonsFromServiceIntoNewMap() {
    Person foo = new Person();
    Person bar = new Person();

    PersonLatestMessages messages = new PersonLatestMessages();
    HashMap<Long, SimpleMessage> data = new HashMap<>();

    SimpleMessage five = new SimpleMessage();
    five.setIsRead(false);
    SimpleMessage seven = new SimpleMessage();
    seven.setIsRead(false);

    data.put(5L, five);
    data.put(7L, seven);
    messages.setMessages(data);

    Mockito
      .when(latestMessagesRepository.findById(4L))
      .thenReturn(Optional.of(messages));
    Mockito.when(personService.getPerson(5L)).thenReturn(foo);
    Mockito.when(personService.getPerson(7L)).thenReturn(bar);

    HashMap<Person, SimpleMessage> expected = new HashMap<>();
    expected.put(foo, five);
    expected.put(bar, seven);

    Map<Person, SimpleMessage> actual = latestMessagesService.getLatestMessagesForPerson(
      4L
    );

    assertEquals(expected, actual);
  }

  @Test
  public void getLatestMessagesFromPersonReturnsEmptyMapWhenNoCacheFound() {
    Map<Person, SimpleMessage> actual = latestMessagesService.getLatestMessagesForPerson(
      4L
    );
    assertTrue(actual.isEmpty());
  }
}
