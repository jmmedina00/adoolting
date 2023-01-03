package io.github.jmmedina00.adoolting.service.cache;

import io.github.jmmedina00.adoolting.entity.cache.PersonLatestMessages;
import io.github.jmmedina00.adoolting.entity.cache.simple.SimpleMessage;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.entity.person.PrivateMessage;
import io.github.jmmedina00.adoolting.repository.cache.PersonLatestMessagesRepository;
import io.github.jmmedina00.adoolting.service.person.PersonService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PersonLatestMessagesService {
  @Autowired
  private PersonLatestMessagesRepository latestMessagesRepository;

  @Autowired
  private PersonService personService;

  public void verifyPersonCache(Long personId, List<PrivateMessage> messages) {
    PersonLatestMessages latestCache = latestMessagesRepository
      .findById(personId)
      .orElseGet(() -> initializePersonCache(personId, messages));
    PrivateMessage latestMessage = messages.get(0);

    if (latestCache.getUpdatedAt().equals(latestMessage.getCreatedAt())) {
      return;
    }

    initializePersonCache(personId, messages);
  }

  public void saveMessageToCache(PrivateMessage message) {
    Long senderId = message.getFromPerson().getId();
    Long receiverId = message.getToPerson().getId();

    SimpleMessage simpleMessage = new SimpleMessage(message);
    simpleMessage.setIsRead(false);

    addMessageToCache(simpleMessage, senderId, receiverId);
    addMessageToCache(simpleMessage, receiverId, senderId);
  }

  public void setMessageFromPersonAsRead(Long receiverId, Long senderId) {
    PersonLatestMessages cache = latestMessagesRepository
      .findById(receiverId)
      .orElse(initNewCache(receiverId));

    HashMap<Long, SimpleMessage> messages = new HashMap<>(cache.getMessages());

    SimpleMessage latest = Optional
      .ofNullable(messages.get(senderId))
      .orElse(initializeNewMessage(senderId));
    latest.setIsRead(true);

    messages.put(senderId, latest);
    cache.setMessages(messages);
    latestMessagesRepository.save(cache);
  }

  public Map<Person, SimpleMessage> getLatestMessagesForPerson(Long personId) {
    PersonLatestMessages latest = latestMessagesRepository
      .findById(personId)
      .orElse(initNewCache(personId));
    Map<Long, SimpleMessage> messages = latest.getMessages();
    LinkedHashMap<Person, SimpleMessage> transformed = new LinkedHashMap<>();

    for (Entry<Long, SimpleMessage> entry : messages.entrySet()) {
      Person person = personService.getPerson(entry.getKey());
      transformed.put(person, entry.getValue());
    }

    return transformed;
  }

  private SimpleMessage initializeNewMessage(Long personId) {
    SimpleMessage message = new SimpleMessage();
    message.setContents("");
    message.setCreatedAt(new Date());
    return message;
  }

  private PersonLatestMessages initNewCache(Long personId) {
    PersonLatestMessages cache = new PersonLatestMessages();
    cache.setId(personId);
    cache.setMessages(new HashMap<>());
    return cache;
  }

  private PersonLatestMessages addMessageToCache(
    SimpleMessage message,
    Long fooId,
    Long barId
  ) {
    PersonLatestMessages cache = latestMessagesRepository
      .findById(fooId)
      .orElse(initNewCache(fooId));

    HashMap<Long, SimpleMessage> messages = new HashMap<>(cache.getMessages());

    messages.put(barId, message);
    cache.setMessages(messages);
    cache.setUpdatedAt(message.getCreatedAt());
    return latestMessagesRepository.save(cache);
  }

  private PersonLatestMessages initializePersonCache(
    Long personId,
    List<PrivateMessage> messages
  ) {
    PersonLatestMessages cache = new PersonLatestMessages();
    cache.setId(personId);

    HashMap<Long, SimpleMessage> firsts = new HashMap<>();

    for (PrivateMessage message : messages) {
      Long interestingPersonId = Objects.equals(
          message.getFromPerson().getId(),
          personId
        )
        ? message.getToPerson().getId()
        : message.getFromPerson().getId();

      if (!firsts.containsKey(interestingPersonId)) {
        SimpleMessage simple = new SimpleMessage(message);
        firsts.put(interestingPersonId, simple);
      }
    }

    cache.setMessages(firsts);
    ArrayList<SimpleMessage> values = new ArrayList<>(firsts.values());
    Collections.sort(values);
    cache.setUpdatedAt(values.get(0).getCreatedAt());
    return latestMessagesRepository.save(cache);
  }
}
