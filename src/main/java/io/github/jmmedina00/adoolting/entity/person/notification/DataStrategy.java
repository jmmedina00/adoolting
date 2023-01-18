package io.github.jmmedina00.adoolting.entity.person.notification;

import io.github.jmmedina00.adoolting.entity.Interaction;
import io.github.jmmedina00.adoolting.entity.cache.EmailData;
import io.github.jmmedina00.adoolting.entity.person.Person;

public interface DataStrategy {
  EmailData generateData(Interaction interaction, Person forPerson);
}
