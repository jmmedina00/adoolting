package io.github.jmmedina00.adoolting.controller.interaction;

import io.github.jmmedina00.adoolting.entity.Medium;
import io.github.jmmedina00.adoolting.exception.MediumNotFoundException;
import io.github.jmmedina00.adoolting.service.MediumService;
import java.util.List;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/picture")
public class PictureController {
  @Autowired
  private MediumService mediumService;

  @RequestMapping(method = RequestMethod.GET, value = "/{id}")
  public String getPictureViewer(
    @PathVariable("id") Long pictureId,
    Model model
  )
    throws MediumNotFoundException {
    Medium original = mediumService.getMedium(pictureId);
    List<Medium> media = mediumService.getPicturesForPictureViewer(pictureId);
    int originalIndex = media.indexOf(original);
    Medium next = (originalIndex + 1) < media.size()
      ? media.get(originalIndex + 1)
      : null;
    Medium previous = (originalIndex - 1) >= 0
      ? media.get(originalIndex - 1)
      : null;

    model.addAttribute("original", original);
    model.addAttribute(
      "interaction",
      Hibernate.unproxy(original.getInteraction())
    );
    model.addAttribute("media", media);
    model.addAttribute("previous", previous);
    model.addAttribute("next", next);
    return "interaction/picture";
  }
}
