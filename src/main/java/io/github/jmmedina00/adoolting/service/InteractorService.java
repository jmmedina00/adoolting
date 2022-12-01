package io.github.jmmedina00.adoolting.service;

import io.github.jmmedina00.adoolting.entity.Interactor;
import io.github.jmmedina00.adoolting.repository.InteractorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InteractorService {
  @Autowired
  private InteractorRepository interactorRepository;

  public Interactor getInteractor(Long interactorId) {
    return interactorRepository.findById(interactorId).orElseThrow();
  }
}
