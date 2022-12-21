package io.github.jmmedina00.adoolting.service;

import io.github.jmmedina00.adoolting.entity.Interaction;
import io.github.jmmedina00.adoolting.entity.Medium;
import io.github.jmmedina00.adoolting.exception.MediumNotFoundException;
import io.github.jmmedina00.adoolting.repository.MediumRepository;
import io.github.jmmedina00.adoolting.service.util.FileService;
import java.util.List;
import java.util.Objects;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MediumService {
  @Autowired
  private MediumRepository mediumRepository;

  @Autowired
  private FileService fileService;

  private static int WANTED_ELEMENTS = 7;
  private static int LIMIT_COUNT = WANTED_ELEMENTS / 2;

  public Page<Medium> getPicturePage(Long interactorId, Pageable pageable) {
    return mediumRepository.findPicturePageByInteractorId(
      interactorId,
      pageable
    );
  }

  public Medium getMedium(Long mediumId) throws MediumNotFoundException {
    return mediumRepository
      .findById(mediumId)
      .orElseThrow(MediumNotFoundException::new);
  }

  public String getThumbnailLinkForMedium(Long mediumId, int desiredSize)
    throws MediumNotFoundException {
    Medium medium = getMedium(mediumId);

    String fileName = medium.isInCDN()
      ? getFilename(medium)
      : mediumId + ".png";
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

  public List<Medium> getPicturesForPictureViewer(Long mediumId) {
    List<Medium> interestingMedia = mediumRepository.findAllPicturesFromTheSameInteractor(
      mediumId
    );
    Medium interestingMedium = interestingMedia
      .stream()
      .filter(medium -> Objects.equals(mediumId, medium.getId()))
      .findFirst()
      .get();

    if (interestingMedia.size() <= WANTED_ELEMENTS) {
      return interestingMedia;
    }

    int maximumCenter = interestingMedia.size() - LIMIT_COUNT - 1;
    int mediumIndex = interestingMedia.indexOf(interestingMedium);
    boolean hittingTheStart = mediumIndex <= LIMIT_COUNT;
    boolean hittingTheEnd = mediumIndex >= maximumCenter;

    int firstSliceIndex = hittingTheStart
      ? 0
      : hittingTheEnd ? maximumCenter - LIMIT_COUNT : mediumIndex - LIMIT_COUNT;

    return interestingMedia.subList(
      firstSliceIndex,
      firstSliceIndex + WANTED_ELEMENTS
    );
  }

  private String getFilename(Medium medium) {
    return medium.getId() + medium.getReference().replace("cdn:", "");
  }
}
