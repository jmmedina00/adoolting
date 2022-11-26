package io.github.jmmedina00.adoolting.service;

import io.github.jmmedina00.adoolting.entity.Interaction;
import io.github.jmmedina00.adoolting.entity.Medium;
import io.github.jmmedina00.adoolting.repository.MediumRepository;
import java.io.File;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MediumService {
  @Autowired
  private MediumRepository mediumRepository;

  @Value("${user.dir}")
  private String workDirectory;

  public void saveAllFiles(List<MultipartFile> files, Interaction interaction)
    throws Exception {
    for (MultipartFile file : files) {
      Medium medium = new Medium();
      String extension = FilenameUtils.getExtension(file.getOriginalFilename());

      medium.setInteraction(interaction);
      medium.setReference("cdn:" + "." + extension);

      Medium saved = mediumRepository.save(medium);
      String path =
        workDirectory +
        "/data/cdn/media/full/" +
        saved.getId() +
        "." +
        extension;

      File writingFile = new File(path);
      writingFile.mkdirs();
      file.transferTo(writingFile);
    }
  }
}
