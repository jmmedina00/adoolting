package io.github.jmmedina00.adoolting.service;

import io.github.jmmedina00.adoolting.entity.Interaction;
import io.github.jmmedina00.adoolting.entity.Medium;
import io.github.jmmedina00.adoolting.repository.MediumRepository;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import org.apache.commons.io.FilenameUtils;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MediumService {
  @Autowired
  private MediumRepository mediumRepository;

  @Autowired
  private JobScheduler jobScheduler;

  @Value("${user.dir}")
  private String workDirectory;

  private String fullRelativePath = "/data/cdn/media/full/";
  private String squareRelativePath = "/data/cdn/media/square/";

  @PostConstruct
  public void initializeDirectoriesIfNeeded() {
    File full = new File(workDirectory + fullRelativePath);
    File square = new File(workDirectory + squareRelativePath);
    full.mkdirs();
    square.mkdirs();
  }

  public List<String> getMediaForInteraction(Long interactionId) {
    return mediumRepository
      .findByInteractionId(interactionId)
      .stream()
      .map(
        medium ->
          "/cdn/media/full/" +
          medium.getId() +
          medium.getReference().replace("cdn:", "")
      )
      .toList();
  }

  public void saveAllFiles(List<MultipartFile> files, Interaction interaction)
    throws Exception {
    for (MultipartFile file : files) {
      Medium medium = new Medium();
      String extension = FilenameUtils.getExtension(file.getOriginalFilename());

      medium.setInteraction(interaction);
      medium.setReference("cdn:" + "." + extension);

      Medium saved = mediumRepository.save(medium);
      String path =
        workDirectory + fullRelativePath + saved.getId() + "." + extension;

      File writingFile = new File(path);
      file.transferTo(writingFile);

      jobScheduler.enqueue(() -> getImageSquare(medium.getId()));
    }
  }

  @Job(name = "Get image square")
  public void getImageSquare(Long mediumId) throws Exception {
    Medium medium = mediumRepository.findById(mediumId).orElse(null);

    if (medium == null) {
      return;
    }

    String extension = medium.getReference().replace("cdn:", "");
    File fullFile = new File(
      workDirectory + fullRelativePath + mediumId + extension
    );
    File squareFile = new File(
      workDirectory + squareRelativePath + mediumId + extension
    );

    if (!fullFile.exists() || squareFile.exists()) {
      return;
    }

    ImageWriter writer = getProperImageWriter(fullFile);
    BufferedImage sourceImage = ImageIO.read(fullFile);
    ImageOutputStream outputStream = new FileImageOutputStream(squareFile);

    int imageType = sourceImage.getColorModel().hasAlpha()
      ? BufferedImage.TYPE_4BYTE_ABGR
      : sourceImage.getType(); // Account for transparent image cases, fallback for JPGs - writer doesn't like it

    int minDimension = Stream
      .of(sourceImage.getWidth(), sourceImage.getHeight())
      .mapToInt(v -> v)
      .min()
      .getAsInt();

    BufferedImage square = new BufferedImage(
      minDimension,
      minDimension,
      imageType
    );
    Graphics2D graphics = square.createGraphics();
    graphics.setRenderingHint(
      RenderingHints.KEY_INTERPOLATION,
      RenderingHints.VALUE_INTERPOLATION_BICUBIC
    );
    graphics.setBackground(new Color(0, 0, 0, 0));

    if (sourceImage.getWidth() > sourceImage.getHeight()) {
      int offset = (sourceImage.getWidth() - sourceImage.getHeight()) / 2;
      graphics.drawImage(
        sourceImage,
        offset * -1,
        0,
        sourceImage.getWidth(),
        sourceImage.getHeight(),
        null
      );
    } else {
      int offset = (sourceImage.getHeight() - sourceImage.getWidth()) / 2;
      graphics.drawImage(
        sourceImage,
        0,
        offset * -1,
        sourceImage.getWidth(),
        sourceImage.getHeight(),
        null
      );
    }

    graphics.dispose();
    writer.setOutput(outputStream);
    writer.write(square);
    outputStream.close();
  }

  private ImageWriter getProperImageWriter(File file) throws Exception {
    String imageMimeType = Files.probeContentType(file.toPath());
    return ImageIO.getImageWritersByMIMEType(imageMimeType).next();
  }
}
