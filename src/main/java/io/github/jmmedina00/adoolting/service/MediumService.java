package io.github.jmmedina00.adoolting.service;

import io.github.jmmedina00.adoolting.entity.Interaction;
import io.github.jmmedina00.adoolting.entity.Medium;
import io.github.jmmedina00.adoolting.exception.MediumNotFoundException;
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

  private static int FULL_FOLDER = 0;
  private static int SQUARE_FOLDER = 1;

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

  public String getThumbnailLinkForMedium(Long mediumId, int desiredSize)
    throws MediumNotFoundException {
    Medium medium = mediumRepository
      .findById(mediumId)
      .orElseThrow(MediumNotFoundException::new);

    if (!medium.isInCDN()) {
      throw new MediumNotFoundException();
    }

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
      saveImageMedium(medium, file);
    }
  }

  public Medium saveLinkMedium(String link, Interaction interaction) {
    Medium medium = new Medium();
    medium.setReference(link);
    medium.setInteraction(interaction);
    return mediumRepository.save(medium);
  }

  public Medium saveImageMedium(Medium medium, MultipartFile uploaded) {
    String extension = FilenameUtils.getExtension(
      uploaded.getOriginalFilename()
    );
    medium.setReference("cdn:" + "." + extension);
    mediumRepository.save(medium);

    String path =
      workDirectory +
      toCdn +
      mediaDir +
      full +
      medium.getId() +
      "." +
      extension;

    File writingFile = new File(path);

    try {
      uploaded.transferTo(writingFile);
      jobScheduler.enqueue(() -> getImageSquare(medium.getId()));
    } catch (Exception e) {
      e.printStackTrace();
    }

    return medium;
  }

  @Job(name = "Get image square")
  public void getImageSquare(Long mediumId) throws Exception {
    Medium medium = mediumRepository.findById(mediumId).get();
    File fullFile = getMediumFile(medium, FULL_FOLDER);
    File squareFile = getMediumFile(medium, SQUARE_FOLDER);

    if (!fullFile.exists() || squareFile.exists()) {
      return;
    }

    BufferedImage sourceImage = ImageIO.read(fullFile);
    int imageType = sourceImage.getColorModel().hasAlpha()
      ? BufferedImage.TYPE_4BYTE_ABGR
      : sourceImage.getType(); // Account for transparent image cases, fallback for JPGs - writer doesn't like it

    int width = sourceImage.getWidth();
    int height = sourceImage.getHeight();
    int minDimension = width > height ? height : width;
    BufferedImage square = new BufferedImage(
      minDimension,
      minDimension,
      imageType
    );
    Graphics2D graphics = prepareGraphics(square);
    drawOnToGraphics(graphics, sourceImage);
    graphics.dispose();
    writeImageToTargetFile(fullFile, squareFile, square);

    for (int size : expectedSizes) {
      if (minDimension >= size) {
        jobScheduler.enqueue(() -> scaleSquareImageToSize(mediumId, size));
      }
    }
  }

  @Job(name = "Scale squared image")
  public void scaleSquareImageToSize(Long mediumId, int size) throws Exception {
    Medium medium = mediumRepository.findById(mediumId).get();
    File squareFile = getMediumFile(medium, SQUARE_FOLDER);
    File scaledFile = getMediumFile(medium, size);

    if (!squareFile.exists() || scaledFile.exists()) {
      return;
    }

    BufferedImage sourceImage = ImageIO.read(squareFile);
    int imageType = sourceImage.getColorModel().hasAlpha()
      ? BufferedImage.TYPE_4BYTE_ABGR
      : sourceImage.getType();

    BufferedImage target = new BufferedImage(size, size, imageType);
    Graphics2D graphics = prepareGraphics(target);
    graphics.drawImage(sourceImage, 0, 0, size, size, null);
    graphics.dispose();
    writeImageToTargetFile(squareFile, scaledFile, target);
  }

  private Graphics2D prepareGraphics(BufferedImage target) {
    Graphics2D graphics = target.createGraphics();
    graphics.setRenderingHint(
      RenderingHints.KEY_INTERPOLATION,
      RenderingHints.VALUE_INTERPOLATION_BICUBIC
    );
    graphics.setBackground(new Color(0, 0, 0, 0));
    return graphics;
  }

  private File getMediumFile(Medium medium, int size) {
    String baseDir = workDirectory + toCdn + mediaDir;
    String fileName =
      medium.getId() + medium.getReference().replace("cdn:", "");
    String specificFolder = Integer.toString(size) + "/";

    if (size == FULL_FOLDER) {
      specificFolder = full;
    }

    if (size == SQUARE_FOLDER) {
      specificFolder = square;
    }

    return new File(baseDir + specificFolder + fileName);
  }

  private void drawOnToGraphics(Graphics2D graphics, BufferedImage source) {
    int width = source.getWidth();
    int height = source.getHeight();
    int offset;

    if (width > height) {
      offset = (width - height) / 2;
      graphics.drawImage(source, offset * -1, 0, width, height, null);
    } else {
      offset = (height - width) / 2;
      graphics.drawImage(source, 0, offset * -1, width, height, null);
    }
  }

  private void writeImageToTargetFile(
    File source,
    File target,
    BufferedImage image
  )
    throws Exception {
    String imageMimeType = Files.probeContentType(source.toPath());
    ImageWriter writer = ImageIO
      .getImageWritersByMIMEType(imageMimeType)
      .next();
    ImageOutputStream outputStream = new FileImageOutputStream(target);

    writer.setOutput(outputStream);
    writer.write(image);
    outputStream.close();
  }
}
