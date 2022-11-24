package io.github.jmmedina00.adoolting.controller;

import io.github.jmmedina00.adoolting.dto.InteractionConfirmation;
import io.github.jmmedina00.adoolting.service.ConfirmableInteractionService;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/network")
public class ConfirmableInteractionController {
  @Autowired
  private ConfirmableInteractionService cInteractionService;

  @RequestMapping(method = RequestMethod.POST, value = "/{interactionId}")
  public String decideInteractionResult(@Valid @RequestBody InteractionConfirmation confirmation) {
    return "";
  }
}
