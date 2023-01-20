package io.github.jmmedina00.adoolting.service.person.notification;

import io.github.jmmedina00.adoolting.entity.Interaction;
import io.github.jmmedina00.adoolting.entity.person.Person;
import java.util.Map;

public interface PersonSelector {
  public Map<Person, Integer> getPersonNotificationMap(Interaction interaction);
}
