package io.github.jmmedina00.adoolting.service.person;

import io.github.jmmedina00.adoolting.dto.person.NewMessage;
import io.github.jmmedina00.adoolting.entity.cache.PersonLatestMessages;
import io.github.jmmedina00.adoolting.entity.cache.SimpleMessage;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.entity.person.PrivateMessage;
import io.github.jmmedina00.adoolting.repository.cache.PersonLatestMessagesRepository;
import io.github.jmmedina00.adoolting.repository.person.PrivateMessageRepository;
import java.util.HashMap;
import java.util.List;
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
    Long firstPersonId,
    Long secondPersonId
  ) {
    return messageRepository.findMessagesByPersonIds(
      firstPersonId,
      secondPersonId
    );
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

  public List<PrivateMessage> getLatestMessagesForPerson(Long personId) {
    List<PrivateMessage> all = messageRepository.findMessagesExchangedWithPerson(
      personId
    );
    HashMap<Long, PrivateMessage> firsts = new HashMap<>();

    for (PrivateMessage message : all) {
      Long interestingPersonId = Objects.equals(
          message.getFromPerson().getId(),
          personId
        )
        ? message.getToPerson().getId()
        : message.getFromPerson().getId();

      if (!firsts.containsKey(interestingPersonId)) {
        firsts.put(interestingPersonId, message);
      }
    }

    return firsts
      .values()
      .stream()
      .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()) * -1)
      .toList();
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