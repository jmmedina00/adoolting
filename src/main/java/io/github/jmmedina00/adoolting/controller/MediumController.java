package io.github.jmmedina00.adoolting.controller;

import io.github.jmmedina00.adoolting.exception.MediumNotFoundException;
import io.github.jmmedina00.adoolting.service.MediumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/media")
public class MediumController {
  @Autowired
  private MediumService mediumService;

  @RequestMapping(method = RequestMethod.GET, value = "/thumbnail/{size}/{id}")
  public String getLinkToThumbnail(
    @PathVariable("size") int size,
    @PathVariable("id") Long mediumId
  )
    throws MediumNotFoundException {
    return (
      "redirect:" + mediumService.getThumbnailLinkForMedium(mediumId, size)
    );
  }
}
