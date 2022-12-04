package io.github.jmmedina00.adoolting.service.person;

import io.github.jmmedina00.adoolting.dto.person.NewMessage;
import io.github.jmmedina00.adoolting.entity.cache.PersonLatestMessages;
import io.github.jmmedina00.adoolting.entity.cache.SimpleMessage;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.entity.person.PrivateMessage;
import io.github.jmmedina00.adoolting.repository.cache.PersonLatestMessagesRepository;
import io.github.jmmedina00.adoolting.repository.person.PrivateMessageRepository;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PrivateMessageService {
  @Autowired
  private PrivateMessageRepository messageRepository;

  @Autowired
  private PersonLatestMessagesRepository latestMessagesRepository;

  @Autowired
  private PersonService personService;

  @PostConstruct
  public void ensureCacheIsUpToDate() {
    List<Person> persons = personService.getAllActivePersons();

    for (Person person : persons) {
      Long personId = person.getId();
      List<PrivateMessage> messages = messageRepository.findMessagesExchangedWithPerson(
        personId
      );
      PersonLatestMessages latestCache = latestMessagesRepository
        .findById(personId)
        .orElse(null);
      PrivateMessage latestMessage = messages.stream().findFirst().orElse(null);

      if (latestMessage == null) {
        return;
      }

      if (
        latestCache == null ||
        !latestCache.getUpdatedAt().equals(latestMessage.getCreatedAt())
      ) {
        System.out.println(personId + " not up to date. Caching...");
        initializePersonCache(personId, messages);
      }
    }
  }

  public List<PrivateMessage> getMessagesBetweenPersons(
    Long receiverId,
    Long senderId
  ) {
    PersonLatestMessages cache = latestMessagesRepository
      .findById(receiverId)
      .orElse(null);

    if (cache != null) {
      SimpleMessage latest = cache.getMessages().get(senderId);

      if (latest != null) {
        cache.getMessages().get(senderId).setIsRead(true);
        latestMessagesRepository.save(cache);
      }
    }

    return messageRepository.findMessagesByPersonIds(receiverId, senderId);
  }

  public PrivateMessage sendMessageToPerson(
    Person sender,
    Person receiver,
    NewMessage newMessage
  ) {
    PrivateMessage message = new PrivateMessage();
    message.setFromPerson(sender);
    message.setToPerson(receiver);
    message.setContents(newMessage.getContents());
    PrivateMessage saved = messageRepository.save(message);

    saveMessageToCache(message);
    return saved;
  }

  public Map<Person, SimpleMessage> getLatestMessagesForPerson(Long personId) {
    PersonLatestMessages latest = latestMessagesRepository
      .findById(personId)
      .orElse(null);

    if (latest == null) {
      return new HashMap<>();
    }

    Map<Long, SimpleMessage> messages = latest.getMessages();
    List<Person> persons = personService.getPersons(messages.keySet());
    LinkedHashMap<Person, SimpleMessage> transformed = new LinkedHashMap<>();

    messages
      .entrySet()
      .stream()
      .sorted(Map.Entry.comparingByValue())
      .forEach(
        entry -> {
          Person person = persons
            .stream()
            .filter(
              singlePerson ->
                Objects.equals(singlePerson.getId(), entry.getKey())
            )
            .findFirst()
            .get();
          transformed.put(person, entry.getValue());
        }
      );

    return transformed;
  }

  private PersonLatestMessages initNewCache(Long personId) {
    PersonLatestMessages cache = new PersonLatestMessages();
    cache.setId(personId);
    cache.setMessages(new HashMap<>());
    return cache;
  }

  private void saveMessageToCache(PrivateMessage message) {
    Long senderId = message.getFromPerson().getId();
    Long receiverId = message.getToPerson().getId();

    SimpleMessage simpleMessage = new SimpleMessage(message);
    simpleMessage.setIsRead(false);
    PersonLatestMessages senderCache = latestMessagesRepository
      .findById(senderId)
      .orElse(initNewCache(senderId));
    PersonLatestMessages receiverCache = latestMessagesRepository
      .findById(receiverId)
      .orElse(initNewCache(receiverId));

    HashMap<Long, SimpleMessage> senderMessages = new HashMap<>(
      senderCache.getMessages()
    );
    HashMap<Long, SimpleMessage> receiverMessages = new HashMap<>(
      receiverCache.getMessages()
    );

    senderMessages.put(receiverId, simpleMessage);
    receiverMessages.put(senderId, simpleMessage);
    senderCache.setMessages(senderMessages);
    receiverCache.setMessages(receiverMessages);

    senderCache.setUpdatedAt(message.getCreatedAt());
    receiverCache.setUpdatedAt(message.getCreatedAt());
    latestMessagesRepository.saveAll(List.of(senderCache, receiverCache));
  }

  private void initializePersonCache(
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
    SimpleMessage latestAmongFirsts = firsts
      .values()
      .stream()
      .sorted()
      .findFirst()
      .get();
    cache.setUpdatedAt(latestAmongFirsts.getCreatedAt());
    latestMessagesRepository.save(cache);
  }
}
