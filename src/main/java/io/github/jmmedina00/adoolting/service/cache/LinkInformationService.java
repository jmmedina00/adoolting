package io.github.jmmedina00.adoolting.service.cache;

import io.github.jmmedina00.adoolting.entity.Medium;
import io.github.jmmedina00.adoolting.entity.cache.LinkInformation;
import io.github.jmmedina00.adoolting.repository.cache.LinkInformationRepository;
import io.github.jmmedina00.adoolting.service.MediumService;
import io.github.jmmedina00.adoolting.service.util.FileService;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.scheduling.JobScheduler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LinkInformationService {
  @Autowired
  private LinkInformationRepository infoRepository;

  @Autowired
  private MediumService mediumService;

  @Autowired
  private FileService fileService;

  @Autowired
  private JobScheduler jobScheduler;

  @Value("${DEFAULT_IMAGE}")
  private String defaultImageFile;

  private static final Logger logger = LoggerFactory.getLogger(
    LinkInformationService.class
  );

  public LinkInformation getLinkInfo(Long mediumId) {
    LinkInformation info = infoRepository.findById(mediumId).orElse(null);

    if (info != null) {
      logger.debug("Fetched info for medium {} correctly", mediumId);
      return info;
    }

    String uuidSeed =
      "Link process: " +
      mediumId +
      " @ " +
      Calendar.getInstance().get(Calendar.DAY_OF_MONTH); // Jobs are deleted at least a few days after done

    logger.info(
      "Info not found for medium {}. Scheduling fetch job {}.",
      mediumId,
      uuidSeed
    );

    jobScheduler.enqueue(
      UUID.nameUUIDFromBytes(uuidSeed.getBytes()),
      () -> fetchAndSaveLinkInfo(mediumId)
    );
    return getBlankInfo();
  }

  @Job(name = "Cache data for link")
  public void fetchAndSaveLinkInfo(Long mediumId) throws Exception {
    Medium medium = mediumService.getMedium(mediumId);
    String reference = medium.getReference();

    Document document = Jsoup
      .connect(reference)
      .userAgent("Mozilla")
      .referrer("https://www.google.com/")
      .get();
    Elements metaTags = document.getElementsByTag("meta");

    logger.debug(
      "<meta> tags for medium {} fetched successfully. {} in total",
      mediumId,
      metaTags.size()
    );

    HashMap<String, String> tagInfo = new HashMap<>();
    for (Element element : metaTags) {
      Attributes attributes = element.attributes();
      String key = attributes.get("property");

      if (key.isEmpty()) {
        continue;
      }

      String value = attributes.get("content");
      logger.debug(
        "Medium {} reference has attribute {} with value {}",
        mediumId,
        key,
        value
      );
      tagInfo.put(key, value);
    }

    logger.debug(
      "Finished cycling medium {} reference meta tags. {} interesting properties",
      mediumId,
      tagInfo.size()
    );

    String title = Optional
      .ofNullable(tagInfo.get("og:title"))
      .orElse(document.title());
    String actualLink = Optional
      .ofNullable(tagInfo.get("og:url"))
      .orElse(reference);
    Optional<String> image = Optional.ofNullable(tagInfo.get("og:image"));

    if (image.isPresent()) {
      logger.debug(
        "Image is present for medium {} reference. Sending to cache.",
        mediumId
      );
      fileService.cacheImageForLinkMedium(image.get(), mediumId);
    }

    LinkInformation info = new LinkInformation();
    info.setId(mediumId);
    info.setActualLink(actualLink);
    info.setTitle(title);
    infoRepository.save(info);

    logger.info(
      "Link information for medium {} has been saved successfully",
      mediumId
    );
  }

  private LinkInformation getBlankInfo() {
    LinkInformation info = new LinkInformation();
    info.setActualLink("");
    info.setTitle("");
    return info;
  }
}
