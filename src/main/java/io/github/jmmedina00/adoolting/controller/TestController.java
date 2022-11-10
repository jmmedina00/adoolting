package io.github.jmmedina00.adoolting.controller;

import io.github.jmmedina00.adoolting.service.SuperEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/test")
public class TestController {
  @Autowired
  private SuperEntryService superEntryService;

  @RequestMapping(method = RequestMethod.GET)
  public String test(
    @RequestParam(name = "name", defaultValue = "world") String name,
    Model model
  ) {
    model.addAttribute("name", name);
    model.addAttribute("entries", superEntryService.getEntries());
    return "testing";
  }

  @RequestMapping(method = RequestMethod.POST, value = "/make")
  public String register(@RequestParam("name") String name) {
    superEntryService.createEntry(name);
    return "redirect:/test";
  }

  @RequestMapping(method = RequestMethod.POST, value = "/delete/{entryId}")
  public String delete(@PathVariable("entryId") String id) {
    Long convertedId = Long.parseLong(id);
    superEntryService.deleteEntry(convertedId);
    return "redirect:/test";
  }

  @RequestMapping(method = RequestMethod.POST, value = "/upload")
  public String uploadImage(@RequestParam("image") MultipartFile file)
    throws Exception {
    superEntryService.saveImage(file);

    return "redirect:/test";
  }
}
