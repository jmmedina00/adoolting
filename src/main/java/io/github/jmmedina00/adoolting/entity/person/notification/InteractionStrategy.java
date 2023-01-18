package io.github.jmmedina00.adoolting.entity.person.notification;

import io.github.jmmedina00.adoolting.entity.Interaction;
import io.github.jmmedina00.adoolting.entity.Interactor;
import io.github.jmmedina00.adoolting.entity.cache.EmailData;
import io.github.jmmedina00.adoolting.entity.person.Person;
import java.util.HashMap;
import java.util.List;

public class InteractionStrategy implements DataStrategy {

  @Override
  public EmailData generateData(Interaction interaction, Person forPerson) {
    EmailData data = new EmailData();
    data.setPerson(forPerson);

    HashMap<String, String> parameters = new HashMap<>();
    parameters.put("interaction", interaction.getId().toString());
    data.setParameters(parameters);

    List<Interactor> interactors = getInterestingInteractors(interaction);
    List<String> arguments = interactors
      .stream()
      .map(i -> i.getFullName())
      .toList();
    String addendum = arguments.indexOf(forPerson.getFullName()) == -1
      ? "page"
      : "profile";

    data.setSubjectArguments(arguments);
    data.setSubjectAddendum(addendum);

    return data;
  }

  public List<Interactor> getInterestingInteractors(Interaction interaction) {
    Interactor interactor = interaction.getInteractor();
    Interactor receiverInteractor = interaction.getReceiverInteractor();

    return receiverInteractor == null
      ? List.of(interactor)
      : List.of(interactor, receiverInteractor);
  }
}
