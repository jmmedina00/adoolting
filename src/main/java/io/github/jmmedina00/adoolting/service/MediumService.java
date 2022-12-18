package io.github.jmmedina00.adoolting.service;

import io.github.jmmedina00.adoolting.entity.Interaction;
import io.github.jmmedina00.adoolting.entity.Medium;
import io.github.jmmedina00.adoolting.exception.MediumNotFoundException;
import io.github.jmmedina00.adoolting.repository.MediumRepository;
import io.github.jmmedina00.adoolting.service.util.FileService;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MediumService {
  @Autowired
  private MediumRepository mediumRepository;

  @Autowired
  private FileService fileService;

  public Medium getMedium(Long mediumId) throws MediumNotFoundException {
    return mediumRepository
      .findById(mediumId)
      .orElseThrow(MediumNotFoundException::new);
  }

  public String getThumbnailLinkForMedium(Long mediumId, int desiredSize)
    throws MediumNotFoundException {
    Medium medium = mediumRepository
      .findById(mediumId)
      .orElseThrow(MediumNotFoundException::new);

    if (!medium.isInCDN()) {
      throw new MediumNotFoundException();
    }

    String fileName = getFilename(medium);
    return fileService.getExistingPathForFile(fileName, desiredSize);
  }

  public void saveAllFiles(List<MultipartFile> files, Interaction interaction)
    throws Exception {
    for (MultipartFile file : files) {
      String extension = FilenameUtils.getExtension(file.getOriginalFilename());
      if (extension.isEmpty()) {
        continue;
      }

      Medium medium = new Medium();
      medium.setInteraction(interaction);
      saveImageMedium(medium, file);
    }
  }

  public Medium saveLinkMedium(String link, Interaction interaction) {
    Medium medium = new Medium();
    medium.setReference(link);
    medium.setInteraction(interaction);
    return mediumRepository.save(medium);
  }

  public Medium saveImageMedium(Medium medium, MultipartFile uploaded)
    throws Exception {
    String extension = FilenameUtils.getExtension(
      uploaded.getOriginalFilename()
    );
    medium.setReference("cdn:" + "." + extension);
    mediumRepository.save(medium);

    String filename = getFilename(medium);
    fileService.saveImage(uploaded, filename);
    return medium;
  }

  private String getFilename(Medium medium) {
    return medium.getId() + medium.getReference().replace("cdn:", "");
  }
}
