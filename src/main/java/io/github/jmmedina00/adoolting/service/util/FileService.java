package io.github.jmmedina00.adoolting.service.util;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import javax.annotation.PostConstruct;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.scheduling.JobScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {
  @Autowired
  private JobScheduler jobScheduler;

  @Autowired
  private GraphicsService graphicsService;

  private String dataFolder, cdnDir, mediaDir, mediaFullDir, mediaSquareDir;
  private int[] expectedSizes = { 64, 128, 256, 512 };

  private static final Logger logger = LoggerFactory.getLogger(
    FileService.class
  );

  public FileService(@Value("${user.dir}") String workDirectory) {
    dataFolder = workDirectory + File.separator + "data";
    cdnDir = dataFolder + File.separator + "cdn" + File.separator;
    mediaDir = cdnDir + "media" + File.separator;
    mediaFullDir = mediaDir + "full" + File.separator;
    mediaSquareDir = mediaDir + "square" + File.separator;

    logger.debug("Data folder: {}", dataFolder);
    logger.debug("CDN folder: {}", cdnDir);
    logger.debug("Media folder: {}", mediaDir);
    logger.debug("Full media folder: {}", mediaFullDir);
    logger.debug("Square media folder: {}", mediaSquareDir);
  }

  @PostConstruct
  public void initializeDirectoriesIfNeeded() {
    File fullDir = new File(mediaFullDir);
    File squareDir = new File(mediaSquareDir);
    fullDir.mkdirs();
    squareDir.mkdirs();

    logger.info("Initialized main folders");

    for (int size : expectedSizes) {
      String dirPath = mediaDir + size + File.separator;

      File dir = new File(mediaDir + size + File.separator);
      dir.mkdirs();

      logger.debug("Initialized folder {}", dirPath);
    }
  }

  public void saveImage(MultipartFile file, String filename) throws Exception {
    File writingTo = new File(mediaFullDir + filename);
    file.transferTo(writingTo);

    logger.info(
      "Transferring full file {} to full dir and setting up all scaling",
      filename
    );
    jobScheduler.enqueue(() -> setupImageScaling(filename));
  }

  public void cacheImageForLinkMedium(String url, Long mediumId)
    throws Exception {
    String filename = mediumId + ".png";

    logger.info(
      "Fetching url {} as {} and saving to full dir. Will setup all scaling",
      url,
      filename
    );
    graphicsService.saveImageFromNetwork(
      url,
      new File(mediaFullDir + filename)
    );
    jobScheduler.enqueue(() -> setupImageScaling(filename));
  }

  public String getExistingPathForFile(String filename, int desiredSize) {
    Optional<File> goodFile = Arrays
      .stream(expectedSizes)
      .filter(size -> size >= desiredSize)
      .mapToObj(size -> new File(mediaDir + size + File.separator + filename))
      .filter(file -> file.exists())
      .findFirst();

    if (goodFile.isPresent()) {
      logger.debug(
        "Found thumbnail for file {}: {}",
        filename,
        goodFile.get().getAbsolutePath()
      );
      return getFileUrl(goodFile.get());
    }

    logger.debug(
      "No thumbnail found for file {}. Trying square of full images instead.",
      filename
    );

    File defaultSquareFile = new File(mediaSquareDir + filename);

    return defaultSquareFile.exists()
      ? getFileUrl(defaultSquareFile)
      : getFileUrl(mediaFullDir + filename);
  }

  @Job(name = "Setup image scaling")
  public void setupImageScaling(String filename) throws Exception {
    String fullPath = mediaFullDir + filename;
    String squaredPath = mediaSquareDir + filename;
    int minDimension = graphicsService.getImageMinimumDimension(
      mediaFullDir + filename
    );
    graphicsService.snipImageToSquare(fullPath, squaredPath);

    logger.info("Preparing convenient resizes for file {}", filename);

    for (int size : expectedSizes) {
      if (minDimension < size) {
        logger.debug(
          "Skipping resize setup for file {} to size {}.",
          filename,
          size
        );
        continue;
      }

      String path = mediaDir + size + File.separator + filename;
      jobScheduler.enqueue(
        () -> graphicsService.resizeSquare(squaredPath, path, size)
      );
    }
  }

  private String getFileUrl(String path) {
    return path.replace(dataFolder, "").replace(File.separator, "/");
  }

  private String getFileUrl(File file) {
    return getFileUrl(file.getAbsolutePath());
  }
}
