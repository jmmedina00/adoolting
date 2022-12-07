package io.github.jmmedina00.adoolting.service;

import io.github.jmmedina00.adoolting.entity.ConfirmableInteraction;
import io.github.jmmedina00.adoolting.entity.Interaction;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.repository.InteractionRepository;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InteractionService {
  @Autowired
  private InteractionRepository interactionRepository;

  public Interaction getInteraction(Long interactionId)
    throws NotAuthorizedException {
    Interaction interaction = interactionRepository
      .findById(interactionId)
      .orElseThrow();

    if (interaction.getDeletedAt() != null) {
      throw new NotAuthorizedException();
    }

    if (interaction instanceof ConfirmableInteraction) {
      ConfirmableInteraction cInteraction = (ConfirmableInteraction) interaction;

      if (cInteraction.getConfirmedAt() == null) {
        throw new NotAuthorizedException();
      }
    }

    return interaction;
  }

  public Interaction getInteractionReference(Long interactionId) {
    return interactionRepository.getReferenceById(interactionId);
  }

  public List<Interaction> getInteractions(Long interactorId) {
    return interactionRepository.findInteractionsByInteractorId(interactorId);
  }

  public void deleteInteraction(Long interactionId, Long interactorId)
    throws NotAuthorizedException {
    Interaction interaction = interactionRepository
      .findById(interactionId)
      .orElseThrow(() -> new NotAuthorizedException());

    Long creatorId = interaction.getInteractor().getId();
    Long receiverId = Optional
      .of(interaction.getReceiverInteractor().getId())
      .orElse(0L);

    if (
      !(
        Objects.equals(interactorId, creatorId) ||
        Objects.equals(interactorId, receiverId)
      )
    ) {
      throw new NotAuthorizedException();
    }

    if (interaction.getDeletedAt() != null) {
      throw new NotAuthorizedException();
    }

    interaction.setDeletedAt(new Date());
    interactionRepository.save(interaction);
  }
}
