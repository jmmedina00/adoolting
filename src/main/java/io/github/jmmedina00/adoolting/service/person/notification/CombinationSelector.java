package io.github.jmmedina00.adoolting.service.person.notification;

import io.github.jmmedina00.adoolting.entity.Interaction;
import io.github.jmmedina00.adoolting.entity.person.Person;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CombinationSelector implements PersonSelector {
  @Autowired
  private CommentSelector commentSelector;

  @Autowired
  private PageFollowersSelector followerSelector;

  @Autowired
  private IncludeReceiverSelector receiverSelector;

  @Override
  public Map<Person, Integer> getPersonNotificationMap(
    Interaction interaction
  ) {
    return List
      .of(
        commentSelector.getPersonNotificationMap(interaction),
        receiverSelector.getPersonNotificationMap(interaction),
        followerSelector.getPersonNotificationMap(interaction)
      )
      .stream()
      .flatMap(map -> map.entrySet().stream())
      .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (a, b) -> a));
  }
}
