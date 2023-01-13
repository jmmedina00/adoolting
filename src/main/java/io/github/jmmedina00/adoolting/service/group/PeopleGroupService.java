package io.github.jmmedina00.adoolting.service.group;

import io.github.jmmedina00.adoolting.controller.common.AuthenticatedPerson;
import io.github.jmmedina00.adoolting.dto.common.DateExtractOfDate;
import io.github.jmmedina00.adoolting.dto.common.TimeExtractOfDate;
import io.github.jmmedina00.adoolting.dto.group.NewEvent;
import io.github.jmmedina00.adoolting.dto.group.NewGroup;
import io.github.jmmedina00.adoolting.dto.util.SecureDeletion;
import io.github.jmmedina00.adoolting.entity.Interactor;
import io.github.jmmedina00.adoolting.entity.cache.PersonLocaleConfig;
import io.github.jmmedina00.adoolting.entity.group.Event;
import io.github.jmmedina00.adoolting.entity.group.PeopleGroup;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.repository.group.PeopleGroupRepository;
import io.github.jmmedina00.adoolting.service.InteractionService;
import io.github.jmmedina00.adoolting.service.InteractorService;
import io.github.jmmedina00.adoolting.service.cache.PersonLocaleConfigService;
import io.github.jmmedina00.adoolting.service.page.PageService;
import io.github.jmmedina00.adoolting.service.person.PersonService;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PeopleGroupService {
  @Autowired
  private PeopleGroupRepository groupRepository;

  @Autowired
  private PageService pageService;

  @Autowired
  private PersonService personService;

  @Autowired
  private InteractionService interactionService;

  @Autowired
  private InteractorService interactorService;

  @Autowired
  private PersonLocaleConfigService localeConfigService;

  private static final Logger logger = LoggerFactory.getLogger(
    PeopleGroupService.class
  );

  public PeopleGroup getGroup(Long groupId) {
    return groupRepository.findActiveGroup(groupId).orElseThrow();
  }

  public PeopleGroup getGroupManagedByPerson(Long groupId, Long personId)
    throws NotAuthorizedException {
    PeopleGroup group = getGroup(groupId);
    interactorService.getRepresentableInteractorByPerson(
      group.getInteractor().getId(),
      personId
    );

    return group;
  }

  public PeopleGroup createGroup(NewGroup newGroup, Long personId) {
    Person person = personService.getPerson(personId);
    PeopleGroup group = new PeopleGroup();
    group.setName(newGroup.getName());
    group.setDescription(newGroup.getDescription());
    group.setInteractor(person);
    group.setAccessLevel(newGroup.getAccessLevel());

    PeopleGroup saved = (PeopleGroup) interactionService.saveInteraction(group);
    logger.info(
      "New group (id={}) created by person {}.",
      saved.getId(),
      personId
    );
    return saved;
  }

  public List<PeopleGroup> getGroupsManagedByPerson(Long personId) {
    ArrayList<Interactor> interactors = new ArrayList<>(
      pageService.getAllPersonPages(personId)
    );
    interactors.add(personService.getPerson(personId));
    List<Long> ids = interactors
      .stream()
      .map(interactor -> interactor.getId())
      .sorted()
      .toList();
    return groupRepository.findActiveGroupsByInteractorList(ids);
  }

  public PeopleGroup updateGroup(
    Long groupId,
    Long personId,
    NewGroup newGroup
  )
    throws NotAuthorizedException {
    PeopleGroup group = getGroupManagedByPerson(groupId, personId);
    group.setName(newGroup.getName());
    group.setDescription(newGroup.getDescription());
    group.setAccessLevel(newGroup.getAccessLevel());

    logger.info("Group {} has been updated by person {}", groupId, personId);
    return groupRepository.save(group); // No need to go through default notif flow
  }

  public NewGroup getGroupForm(Long groupId) {
    PeopleGroup group = getGroup(groupId);
    NewGroup form = new NewGroup();

    if (group instanceof Event) {
      Event event = (Event) group;
      PersonLocaleConfig locale = localeConfigService.getConfig(
        AuthenticatedPerson.getPersonId()
      );
      NewEvent eventForm = new NewEvent();
      eventForm.setLocation(event.getLocation());
      eventForm.setCreateAs(group.getInteractor().getId());

      Calendar happeningAtCalendar = Calendar.getInstance();
      TimeZone tz = happeningAtCalendar.getTimeZone();
      logger.debug("Date time is: {}", happeningAtCalendar.getTimeZone());
      happeningAtCalendar.setTime(event.getHappeningAt());
      happeningAtCalendar.add(Calendar.MILLISECOND, tz.getRawOffset() * -1);
      happeningAtCalendar.add(Calendar.MINUTE, locale.getOffsetFromUTC() * -1);

      Date convertedHappeningAt = happeningAtCalendar.getTime();

      eventForm.setDate(new DateExtractOfDate(convertedHappeningAt));
      eventForm.setTime(new TimeExtractOfDate(convertedHappeningAt));

      form = eventForm;
    }

    form.setAccessLevel(group.getAccessLevel());
    form.setDescription(group.getDescription());
    form.setName(group.getName());

    return form;
  }

  public PeopleGroup deleteGroup(
    Long groupId,
    Long attemptingPersonId,
    SecureDeletion confirmation
  )
    throws Exception {
    Person person = personService.getPersonWithMatchingPassword(
      attemptingPersonId,
      confirmation
    );

    PeopleGroup group = getGroupManagedByPerson(groupId, person.getId());
    group.setDeletedAt(new Date());
    logger.info(
      "Group {} has been deleted by person {}",
      groupId,
      attemptingPersonId
    );
    return (PeopleGroup) interactionService.saveInteraction(group);
  }
}
