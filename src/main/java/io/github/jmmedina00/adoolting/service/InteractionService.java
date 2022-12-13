package io.github.jmmedina00.adoolting.service;

import io.github.jmmedina00.adoolting.entity.Interaction;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.repository.InteractionRepository;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class InteractionService {
  @Autowired
  private InteractionRepository interactionRepository;

  public Interaction saveInteraction(Interaction interaction) {
    return interactionRepository.save(interaction);
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
