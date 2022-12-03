package io.github.jmmedina00.adoolting.controller;

import io.github.jmmedina00.adoolting.service.MediumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/media")
public class MediumController {
  @Autowired
  private MediumService mediumService;

  @Value("${DEFAULT_IMAGE}")
  private String defaultImageFile;

  @RequestMapping(method = RequestMethod.GET, value = "/thumbnail/{size}/{id}")
  public String getLinkToThumbnail(
    @PathVariable("size") String sizeStr,
    @PathVariable("id") String idStr
  ) {
    try {
      int size = Integer.parseInt(sizeStr);
      Long mediumId = Long.parseLong(idStr);
      return (
        "redirect:" + mediumService.getThumbnailLinkForMedium(mediumId, size)
      );
    } catch (Exception e) {
      return "redirect:/cdn/" + defaultImageFile;
    }
  }
}
