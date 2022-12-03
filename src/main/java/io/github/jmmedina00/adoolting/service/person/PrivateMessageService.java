package io.github.jmmedina00.adoolting.service.person;

import io.github.jmmedina00.adoolting.dto.person.NewMessage;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.entity.person.PrivateMessage;
import io.github.jmmedina00.adoolting.repository.person.PrivateMessageRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PrivateMessageService {
  @Autowired
  private PrivateMessageRepository messageRepository;

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
    return messageRepository.save(message);
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
}
