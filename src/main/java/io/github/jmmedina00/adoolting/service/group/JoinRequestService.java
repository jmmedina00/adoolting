package io.github.jmmedina00.adoolting.service.group;

import io.github.jmmedina00.adoolting.entity.Interactor;
import io.github.jmmedina00.adoolting.entity.group.JoinRequest;
import io.github.jmmedina00.adoolting.entity.group.PeopleGroup;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.repository.group.JoinRequestRepository;
import io.github.jmmedina00.adoolting.service.InteractionService;
import io.github.jmmedina00.adoolting.service.InteractorService;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JoinRequestService {
  @Autowired
  private JoinRequestRepository joinRequestRepository;

  @Autowired
  private PeopleGroupService groupService;

  @Autowired
  private InteractorService interactorService;

  @Autowired
  private InteractionService interactionService;

  private static final Logger logger = LoggerFactory.getLogger(
    JoinRequestService.class
  );

  public JoinRequest getJoinRequestForPersonAndGroup(
    Long personId,
    Long groupId
  ) {
    return joinRequestRepository.findExistingForInteractorsAndGroup(
      personId,
      groupId
    );
  }

  public List<JoinRequest> getExistingForGroup(Long groupId) {
    return joinRequestRepository.findExistingForGroup(groupId);
  }

  public JoinRequest joinGroup(Long personId, Long groupId)
    throws NotAuthorizedException {
    JoinRequest existing = getJoinRequestForPersonAndGroup(personId, groupId);
    if (existing != null) {
      logger.debug(
        "A join request for person {} and group {} already exists.",
        personId,
        groupId
      );
      return existing;
    }

    Interactor person = (Person) interactorService.getInteractor(personId);
    PeopleGroup group = groupService.getGroup(groupId);

    if (Objects.equals(person.getId(), group.getInteractor().getId())) {
      throw new NotAuthorizedException();
    }

    logger.info("Person {} wants to join group {}", personId, groupId);
    return createJoinRequest(person, group.getInteractor(), group);
  }

  public JoinRequest inviteToGroup(
    Long hostPersonId,
    Long invitedPersonId,
    Long groupId
  )
    throws NotAuthorizedException {
    PeopleGroup group = groupService.getGroupManagedByPerson(
      groupId,
      hostPersonId
    );

    Interactor host = interactorService.getInteractor(hostPersonId);
    Interactor invited = interactorService.getInteractor(invitedPersonId);

    JoinRequest existing = getJoinRequestForPersonAndGroup(
      invitedPersonId,
      groupId
    );
    if (existing != null) {
      logger.debug(
        "A join request for person {} and group {} already exists.",
        invitedPersonId,
        groupId
      );
      return existing;
    }

    if (!(invited instanceof Person)) {
      throw new NotAuthorizedException();
    }

    logger.info(
      "Person {} is inviting person {} to group {}",
      hostPersonId,
      invitedPersonId,
      groupId
    );
    return createJoinRequest(host, invited, group);
  }

  private JoinRequest createJoinRequest(
    Interactor sender,
    Interactor receiver,
    PeopleGroup group
  ) {
    JoinRequest joinRequest = new JoinRequest();
    joinRequest.setGroup(group);
    joinRequest.setInteractor(sender);
    joinRequest.setReceiverInteractor(receiver);

    JoinRequest saved = (JoinRequest) interactionService.saveInteraction(
      joinRequest
    );

    logger.info(
      "Join request created from interactor {} to interactor {} with group {} involved",
      sender.getId(),
      receiver.getId(),
      group.getId()
    );

    return saved;
  }
}
