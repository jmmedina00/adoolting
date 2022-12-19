package io.github.jmmedina00.adoolting.service.util;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import javax.annotation.PostConstruct;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.scheduling.JobScheduler;
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

  public FileService(@Value("${user.dir}") String workDirectory) {
    dataFolder = workDirectory + File.separator + "data";
    cdnDir = dataFolder + File.separator + "cdn" + File.separator;
    mediaDir = cdnDir + "media" + File.separator;
    mediaFullDir = mediaDir + "full" + File.separator;
    mediaSquareDir = mediaDir + "square" + File.separator;
  }

  @PostConstruct
  public void initializeDirectoriesIfNeeded() {
    File fullDir = new File(mediaFullDir);
    File squareDir = new File(mediaSquareDir);
    fullDir.mkdirs();
    squareDir.mkdirs();

    for (int size : expectedSizes) {
      File dir = new File(mediaDir + size + "/");
      dir.mkdirs();
    }
  }

  public void saveImage(MultipartFile file, String filename) throws Exception {
    File writingTo = new File(mediaFullDir + filename);
    file.transferTo(writingTo);
    jobScheduler.enqueue(() -> setupImageScaling(filename));
  }

  public void cacheImageForLinkMedium(String url, Long mediumId)
    throws Exception {
    String filename = mediumId + ".png";
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
      .mapToObj(size -> new File(mediaDir + size + "/" + filename))
      .filter(file -> file.exists())
      .findFirst();

    if (goodFile.isPresent()) {
      return getFileUrl(goodFile.get());
    }

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

    for (int size : expectedSizes) {
      if (minDimension < size) {
        continue;
      }

      String path = mediaDir + size + "/" + filename;
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
