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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

  private String toCdn = "/data/cdn/";
  private String mediaDir = "media/";
  private String full = "full/";
  private String square = "square/";

  private int[] expectedSizes = { 512, 256, 128, 64 };

  @PostConstruct
  public void initializeDirectoriesIfNeeded() {
    File fullDir = new File(workDirectory + toCdn + mediaDir + full);
    File squareDir = new File(workDirectory + toCdn + mediaDir + square);
    fullDir.mkdirs();
    squareDir.mkdirs();

    for (int size : expectedSizes) {
      File dir = new File(workDirectory + toCdn + mediaDir + size + "/");
      dir.mkdirs();
    }
  }

  public String getProperPublicPath() {
    return "/cdn/" + mediaDir + full;
  }

  public String getProperFullPath() {
    return workDirectory + toCdn + mediaDir + full;
  }

  public String getThumbnailLinkForMedium(Long mediumId, int desiredSize) {
    Medium medium = mediumRepository.findById(mediumId).get();

    List<Integer> cycleSizes = new ArrayList<>(
      Arrays.stream(expectedSizes).boxed().toList()
    );
    Collections.reverse(cycleSizes);

    String extension = medium.getReference().replace("cdn:", "");

    for (Integer size : cycleSizes) {
      if (size.intValue() < desiredSize) {
        continue;
      }

      String testUrl =
        workDirectory +
        toCdn +
        mediaDir +
        size.intValue() +
        "/" +
        medium.getId() +
        extension;

      File fileCheck = new File(testUrl);

      if (fileCheck.exists()) {
        return testUrl.replace(workDirectory + "/data", "");
      }
    }

    String defaultSquare =
      "/cdn/" + mediaDir + square + medium.getId() + extension;
    File file = new File(workDirectory + "/data" + defaultSquare);

    return file.exists()
      ? defaultSquare
      : ("/cdn/" + mediaDir + full + medium.getId() + extension);
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
      medium.setReference("cdn:" + "." + extension);

      Medium saved = mediumRepository.save(medium);
      String path =
        workDirectory +
        toCdn +
        mediaDir +
        full +
        saved.getId() +
        "." +
        extension;

      File writingFile = new File(path);
      file.transferTo(writingFile);

      jobScheduler.enqueue(() -> getImageSquare(medium.getId()));
    }
  }

  // TODO: refactor to make it more likeable by JobRunr
  @Job(name = "Get image square")
  public void getImageSquare(Long mediumId) throws Exception {
    Medium medium = mediumRepository.findById(mediumId).orElse(null);

    if (medium == null) {
      return;
    }

    String extension = medium.getReference().replace("cdn:", "");
    File fullFile = new File(
      workDirectory + toCdn + mediaDir + full + mediumId + extension
    );
    File squareFile = new File(
      workDirectory + toCdn + mediaDir + square + mediumId + extension
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

    for (int size : expectedSizes) {
      if (minDimension >= size) {
        jobScheduler.enqueue(() -> scaleSquareImageToSize(mediumId, size));
      }
    }
  }

  @Job(name = "Scale squared image")
  public void scaleSquareImageToSize(Long mediumId, int size) throws Exception {
    Medium medium = mediumRepository.findById(mediumId).orElse(null);

    if (medium == null) {
      return;
    }

    String extension = medium.getReference().replace("cdn:", "");
    File squareFile = new File(
      workDirectory + toCdn + mediaDir + square + mediumId + extension
    );
    File scaledFile = new File(
      workDirectory + toCdn + mediaDir + size + "/" + mediumId + extension
    );

    if (!squareFile.exists() || scaledFile.exists()) {
      return;
    }

    ImageWriter writer = getProperImageWriter(squareFile);
    BufferedImage sourceImage = ImageIO.read(squareFile);
    ImageOutputStream outputStream = new FileImageOutputStream(scaledFile);

    int imageType = sourceImage.getColorModel().hasAlpha()
      ? BufferedImage.TYPE_4BYTE_ABGR
      : sourceImage.getType();

    BufferedImage target = new BufferedImage(size, size, imageType);
    Graphics2D graphics = target.createGraphics();
    graphics.setRenderingHint(
      RenderingHints.KEY_INTERPOLATION,
      RenderingHints.VALUE_INTERPOLATION_BICUBIC
    );
    graphics.setBackground(new Color(0, 0, 0, 0));
    graphics.drawImage(sourceImage, 0, 0, size, size, null);
    graphics.dispose();

    writer.setOutput(outputStream);
    writer.write(target);
    outputStream.close();
  }

  private ImageWriter getProperImageWriter(File file) throws Exception {
    String imageMimeType = Files.probeContentType(file.toPath());
    return ImageIO.getImageWritersByMIMEType(imageMimeType).next();
  }
}
