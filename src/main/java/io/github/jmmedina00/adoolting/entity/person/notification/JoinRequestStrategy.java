package io.github.jmmedina00.adoolting.entity.person.notification;

import io.github.jmmedina00.adoolting.entity.Interaction;
import io.github.jmmedina00.adoolting.entity.Interactor;
import io.github.jmmedina00.adoolting.entity.cache.EmailData;
import io.github.jmmedina00.adoolting.entity.group.JoinRequest;
import io.github.jmmedina00.adoolting.entity.group.PeopleGroup;
import io.github.jmmedina00.adoolting.entity.person.Person;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class JoinRequestStrategy extends ConfirmableStrategy {

  @Override
  public EmailData generateData(Interaction interaction, Person forPerson) {
    EmailData data = super.generateData(interaction, forPerson);
    JoinRequest request = (JoinRequest) interaction;
    PeopleGroup group = request.getGroup();

    List<String> arguments = Stream
      .of(data.getSubjectArguments(), List.of(group.getName()))
      .flatMap(list -> list.stream())
      .toList();

    Interactor groupCreator = group.getInteractor();
    String addendum =
      "group" +
      (
        Objects.equals(request.getInteractor().getId(), groupCreator.getId())
          ? ".invite"
          : ".request"
      );

    data.setSubjectArguments(arguments);
    data.setSubjectAddendum(addendum);

    return data;
  }
}
