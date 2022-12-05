package io.github.jmmedina00.adoolting.service.group;

import io.github.jmmedina00.adoolting.entity.Interactor;
import io.github.jmmedina00.adoolting.entity.group.JoinRequest;
import io.github.jmmedina00.adoolting.entity.group.PeopleGroup;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.repository.group.JoinRequestRepository;
import io.github.jmmedina00.adoolting.service.InteractorService;
import java.util.Date;
import java.util.Objects;
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

  public JoinRequest getJoinRequestForPersonAndGroup(
    Long personId,
    Long groupId
  ) {
    return joinRequestRepository.findExistingForInteractorsAndGroup(
      personId,
      groupId
    );
  }

  public JoinRequest joinGroup(Long personId, Long groupId)
    throws NotAuthorizedException {
    if (groupService.isGroupManagedByPerson(groupId, personId)) {
      throw new NotAuthorizedException();
    }

    JoinRequest existing = getJoinRequestForPersonAndGroup(personId, groupId);
    if (existing != null) {
      return existing;
    }

    Interactor person = interactorService.getInteractor(personId);
    PeopleGroup group = groupService.getGroup(groupId);
    return createJoinRequest(person, group.getInteractor(), group);
  }

  public JoinRequest inviteToGroup(
    Long hostPersonId,
    Long invitedPersonId,
    Long groupId
  )
    throws NotAuthorizedException {
    if (!groupService.isGroupManagedByPerson(groupId, hostPersonId)) {
      throw new NotAuthorizedException();
    }

    Interactor host = interactorService.getInteractor(hostPersonId);
    Interactor invited = interactorService.getInteractor(invitedPersonId);

    JoinRequest existing = getJoinRequestForPersonAndGroup(
      invitedPersonId,
      groupId
    );
    if (existing != null) {
      return existing;
    }

    if (!(invited instanceof Person)) {
      throw new NotAuthorizedException();
    }

    PeopleGroup group = groupService.getGroup(groupId);
    return createJoinRequest(host, invited, group);
  }

  public JoinRequest deleteJoinRequest(Long joinRequestId, Long personId)
    throws NotAuthorizedException {
    JoinRequest joinRequest = joinRequestRepository
      .findById(joinRequestId)
      .orElseThrow();

    boolean isAllowedToProceed =
      (
        Objects.equals(joinRequest.getInteractor().getId(), personId) ||
        Objects.equals(joinRequest.getReceiverInteractor().getId(), personId) ||
        groupService.isGroupManagedByPerson(
          joinRequest.getGroup().getId(),
          personId
        )
      );

    if (!isAllowedToProceed) {
      throw new NotAuthorizedException();
    }

    joinRequest.setDeletedAt(new Date());
    return joinRequestRepository.save(joinRequest);
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

    return joinRequestRepository.save(joinRequest);
  }
}
