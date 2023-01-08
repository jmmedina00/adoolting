package io.github.jmmedina00.adoolting.service.person;

import io.github.jmmedina00.adoolting.dto.person.NewMessage;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.entity.person.PrivateMessage;
import io.github.jmmedina00.adoolting.repository.person.PrivateMessageRepository;
import io.github.jmmedina00.adoolting.service.cache.PersonLatestMessagesService;
import java.util.List;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PrivateMessageService {
  @Autowired
  private PrivateMessageRepository messageRepository;

  @Autowired
  private PersonLatestMessagesService latestMessagesService;

  @Autowired
  private PersonService personService;

  private static final Logger logger = LoggerFactory.getLogger(
    PrivateMessageService.class
  );

  @PostConstruct
  public void ensureCacheIsUpToDate() {
    List<Person> persons = personService.getAllActivePersons();

    logger.info(
      "{} person(s) are active, checking message caches",
      persons.size()
    );

    for (Person person : persons) {
      Long personId = person.getId();
      List<PrivateMessage> messages = messageRepository.findMessagesExchangedWithPerson(
        personId
      );

      if (messages.size() == 0) {
        logger.debug(
          "Skipping person {}. No messages exchanged with this person.",
          personId
        );
        continue;
      }

      logger.info("Verifying person {} message cache.", personId);
      latestMessagesService.verifyPersonCache(personId, messages);
    }
  }

  public Page<PrivateMessage> getMessagesBetweenPersons(
    Long receiverId,
    Long senderId,
    Pageable pageable
  ) {
    latestMessagesService.setMessageFromPersonAsRead(receiverId, senderId);
    return messageRepository.findMessagesByPersonIds(
      receiverId,
      senderId,
      pageable
    );
  }

  public PrivateMessage sendMessageToPerson(
    Long senderId,
    Long receiverId,
    NewMessage newMessage
  ) {
    Person sender = personService.getPerson(senderId);
    Person receiver = personService.getPerson(receiverId);

    PrivateMessage message = new PrivateMessage();
    message.setFromPerson(sender);
    message.setToPerson(receiver);
    message.setContents(newMessage.getContents());
    PrivateMessage saved = messageRepository.save(message);

    logger.info(
      "Person {} has sent a new message to person {}, id {}",
      senderId,
      receiverId,
      saved.getId()
    );

    latestMessagesService.saveMessageToCache(message);
    return saved;
  }
}
