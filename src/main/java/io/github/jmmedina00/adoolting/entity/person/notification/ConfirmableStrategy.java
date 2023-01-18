package io.github.jmmedina00.adoolting.entity.person.notification;

import io.github.jmmedina00.adoolting.entity.ConfirmableInteraction;
import io.github.jmmedina00.adoolting.entity.Interaction;
import io.github.jmmedina00.adoolting.entity.cache.EmailData;
import io.github.jmmedina00.adoolting.entity.person.Person;
import java.util.ArrayList;
import java.util.Collections;

public class ConfirmableStrategy extends InteractionStrategy {

  @Override
  public EmailData generateData(Interaction interaction, Person forPerson) {
    ConfirmableInteraction cInteraction = (ConfirmableInteraction) interaction;
    EmailData data = super.generateData(interaction, forPerson);

    if (cInteraction.getConfirmedAt() != null) {
      ArrayList<String> arguments = new ArrayList<>(data.getSubjectArguments());
      Collections.reverse(arguments);
      data.setSubjectArguments(arguments);
    }

    data.setSubjectAddendum("friend");
    return data;
  }
}
