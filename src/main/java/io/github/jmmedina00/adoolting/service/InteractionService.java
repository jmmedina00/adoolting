package io.github.jmmedina00.adoolting.service;

import io.github.jmmedina00.adoolting.entity.Interaction;
import io.github.jmmedina00.adoolting.entity.Interactor;
import io.github.jmmedina00.adoolting.entity.Medium;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.repository.InteractionRepository;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InteractionService {
  @Autowired
  private InteractionRepository interactionRepository;

  @Autowired
  private MediumService mediumService;

  public Interaction getInteraction(Long interactionId) {
    Interaction interaction = interactionRepository
      .findById(interactionId)
      .orElseThrow();
    interaction.setMedia(
      mediumService.getMediaForInteraction(interactionId, 512)
    );
    return interaction;
  }

  public Interaction getInteractionReference(Long interactionId) {
    return interactionRepository.getReferenceById(interactionId);
  }

  public List<Interaction> getInteractions(Long interactorId) {
    return interactionRepository
      .findInteractionsByInteractorId(interactorId)
      .stream()
      .map(
        interaction -> {
          if (interaction.getMedia().size() == 0) {
            return interaction;
          }

          List<Medium> properMedia = mediumService.getMediaForInteraction(
            interaction.getId(),
            256
          );
          interaction.setMedia(properMedia);
          return interaction;
        }
      )
      .toList();
  }

  public void deleteInteraction(
    Long interactionId,
    Interactor creatorInteractor
  )
    throws NotAuthorizedException {
    Interaction interaction = interactionRepository
      .findById(interactionId)
      .orElseThrow(() -> new NotAuthorizedException());

    if (
      !Objects.equals(
        creatorInteractor.getId(),
        interaction.getInteractor().getId()
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
