package io.github.jmmedina00.adoolting.service;

import io.github.jmmedina00.adoolting.entity.Interaction;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.repository.InteractionRepository;
import io.github.jmmedina00.adoolting.service.person.NotificationService;
import io.github.jmmedina00.adoolting.service.person.NotifiedInteractorService;
import java.util.Date;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class InteractionService {
  @Autowired
  private InteractionRepository interactionRepository;

  @Autowired
  private NotificationService notificationService; // ALL INTERACTIONS GO THROUGH HERE OwO

  @Autowired
  private NotifiedInteractorService notifiedInteractorService;

  public Interaction saveInteraction(Interaction interaction) {
    Interaction saved = interactionRepository.save(interaction);

    Map<Person, Integer> interestedInteractors = notifiedInteractorService.getInteractorsInterestedInInteraction(
      saved
    );
    for (Map.Entry<Person, Integer> entry : interestedInteractors.entrySet()) {
      notificationService.createNotifications(
        interaction,
        entry.getKey(),
        entry.getValue()
      );
    }

    return saved;
  }

  public Interaction getInteraction(Long interactionId) {
    return interactionRepository
      .findActiveInteraction(interactionId)
      .orElseThrow();
  }

  public Page<Interaction> getInteractions(
    Long interactorId,
    Pageable pageable
  ) {
    return interactionRepository.findInteractionsByInteractorId(
      interactorId,
      pageable
    );
  }

  public void deleteInteraction(Long interactionId, Long interactorId)
    throws NotAuthorizedException {
    Interaction interaction = interactionRepository
      .findDeletableInteractionForInteractor(interactionId, interactorId)
      .orElseThrow();

    interaction.setDeletedAt(new Date());
    interactionRepository.save(interaction);
  }
}
