package io.github.jmmedina00.adoolting.service;

import io.github.jmmedina00.adoolting.entity.Interaction;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.repository.InteractionRepository;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InteractionService {
  @Autowired
  private InteractionRepository interactionRepository;

  public Interaction getInteraction(Long interactionId) {
    return interactionRepository
      .findActiveInteraction(interactionId)
      .orElseThrow();
  }

  public List<Interaction> getInteractions(Long interactorId) {
    return interactionRepository.findInteractionsByInteractorId(interactorId);
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
