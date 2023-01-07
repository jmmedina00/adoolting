package io.github.jmmedina00.adoolting.service.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.jmmedina00.adoolting.util.MethodDoesThatNameGenerator;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.jobrunr.jobs.lambdas.JobLambda;
import org.jobrunr.scheduling.JobScheduler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayNameGeneration(MethodDoesThatNameGenerator.class)
public class FileServiceTest {
  @MockBean
  private JobScheduler jobScheduler;

  @MockBean
  private GraphicsService graphicsService;

  @Autowired
  private FileService fileService;

  @Captor
  private ArgumentCaptor<JobLambda> lambdaCaptor;

  private static String baseFolderPath =
    System.getProperty("java.io.tmpdir") + File.separator + "fileTest";
  private static File baseFolder = new File(baseFolderPath);
  private static int[] expectedSizes = { 64, 128, 256, 512 };

  private static String mediaDir =
    baseFolderPath +
    File.separator +
    "data" +
    File.separator +
    "cdn" +
    File.separator +
    "media" +
    File.separator;

  @BeforeAll // BeforeEach doesn't apply the changes in time
  public static void resetTestFolder() {
    FileSystemUtils.deleteRecursively(baseFolder);
    baseFolder.mkdir();
    System.setProperty("user.dir", baseFolderPath);
  }

  @Test
  public void initializeDirectoriesIfNeededCreatesAllDirectoriesNeeded() {
    File fullDir = new File(mediaDir + "full");
    File squareDir = new File(mediaDir + "square");
    File dir512 = new File(mediaDir + "512");
    File dir256 = new File(mediaDir + "256");
    File dir128 = new File(mediaDir + "128");
    File dir64 = new File(mediaDir + "64");

    fileService.initializeDirectoriesIfNeeded();

    assertTrue(fullDir.exists());
    assertTrue(fullDir.isDirectory());
    assertTrue(squareDir.exists());
    assertTrue(squareDir.isDirectory());
    assertTrue(dir512.exists());
    assertTrue(dir512.isDirectory());
    assertTrue(dir256.exists());
    assertTrue(dir256.isDirectory());
    assertTrue(dir128.exists());
    assertTrue(dir128.isDirectory());
    assertTrue(dir64.exists());
    assertTrue(dir64.isDirectory());
  }

  @Test
  public void saveImageSavesFileToFullDir() throws Exception {
    String fullDir = mediaDir + "full" + File.separator;
    MultipartFile file = Mockito.mock(MultipartFile.class);

    fileService.saveImage(file, "200.jpg");

    verify(file, times(1)).transferTo(new File(fullDir + "200.jpg"));
  }

  @Test
  public void cacheImageForLinkMediumCallsGraphicsServiceWithCorrectFileSpec()
    throws Exception {
    String fullDir = mediaDir + "full" + File.separator;
    String url = "http://test.local/image";

    fileService.cacheImageForLinkMedium(url, 123L);

    verify(graphicsService, times(1))
      .saveImageFromNetwork(url, new File(fullDir + "123.png"));
    verify(jobScheduler, times(1)).enqueue(any(JobLambda.class));
  }

  private static Stream<Arguments> firstAvailableAndDesiredSizeCombinations() {
    List<Integer> desiredSizes = List.of(64, 100, 128, 192, 256, 450, 512);
    return Arrays
      .stream(expectedSizes)
      .boxed()
      .flatMap(
        firstSize ->
          desiredSizes
            .stream()
            .map(desiredSize -> Arguments.of(firstSize, desiredSize))
      );
  }

  @ParameterizedTest
  @MethodSource("firstAvailableAndDesiredSizeCombinations")
  public void getExistingPathForFileGetsSmallestFileAvailableStartingFromDesiredSize(
    int firstAvailable,
    int desiredSize
  )
    throws Exception {
    int expected = Arrays
      .stream(expectedSizes)
      .filter(
        size ->
          size >= IntStream.of(firstAvailable, desiredSize).max().getAsInt()
      )
      .findFirst()
      .getAsInt();
    String filename = "test.png";

    for (int size : expectedSizes) {
      String path = mediaDir + size + File.separator + filename;
      File file = new File(path);

      if (size >= firstAvailable) {
        file.createNewFile();
      } else {
        file.delete();
      }
    }

    String resultingPath = fileService.getExistingPathForFile(
      filename,
      desiredSize
    );
    assertEquals("/cdn/media/" + expected + "/" + filename, resultingPath);
  }

  @Test
  public void getExistingPathForFileDefaultsToSquareSnipIfNoThumbnailsAvailable()
    throws IOException {
    String filename = "non-exist.jpg";

    for (int size : expectedSizes) {
      String path = mediaDir + size + File.separator + filename;
      File file = new File(path);
      file.delete();
    }

    String squarePath = mediaDir + "square" + File.separator + filename;
    File square = new File(squarePath);
    square.createNewFile();

    String resultingPath = fileService.getExistingPathForFile(filename, 100);
    assertEquals("/cdn/media/square/" + filename, resultingPath);
  }

  @Test
  public void getExistingPathForFileDefaultsToSquareSnipIfNotBigEnoughThumbnailsAvailable()
    throws IOException {
    String filename = "non-exist.jpg";

    for (int size : expectedSizes) {
      String path = mediaDir + size + File.separator + filename;
      File file = new File(path);
      file.delete();
    }

    String goodPath = mediaDir + 64 + File.separator + filename; // Shouldn't be reached by call
    File goodFile = new File(goodPath);
    goodFile.createNewFile();

    String squarePath = mediaDir + "square" + File.separator + filename;
    File square = new File(squarePath);
    square.createNewFile();

    String resultingPath = fileService.getExistingPathForFile(filename, 128);
    assertEquals("/cdn/media/square/" + filename, resultingPath);
  }

  @Test
  public void getExistingPathForFileDefaultsToFullImageIfNoThumbnailsNorSquareSnipAvailable()
    throws IOException {
    String filename = "non-exist.jpg";

    for (int size : expectedSizes) {
      String path = mediaDir + size + File.separator + filename;
      File file = new File(path);
      file.delete();
    }

    String squarePath = mediaDir + "square" + File.separator + filename;
    File square = new File(squarePath);
    square.delete();

    String resultingPath = fileService.getExistingPathForFile(filename, 100);
    assertEquals("/cdn/media/full/" + filename, resultingPath);
  }

  private static Stream<Arguments> possibleSizes() {
    return List
      .of(32, 64, 90, 128, 200, 256, 330, 512, 900)
      .stream()
      .map(size -> Arguments.of(size));
  }

  @ParameterizedTest
  @MethodSource("possibleSizes")
  public void setupImageScalingPreparesAllCorrectCallsDependingOnMinimumSize(
    int minDimension
  )
    throws Exception {
    String filename = "saved.png";
    String expectedFull = mediaDir + "full" + File.separator + filename;
    String expectedSquare = mediaDir + "square" + File.separator + filename;

    Mockito
      .when(graphicsService.getImageMinimumDimension(expectedFull))
      .thenReturn(minDimension);

    fileService.setupImageScaling(filename);

    verify(graphicsService, times(1))
      .snipImageToSquare(expectedFull, expectedSquare);
    verify(jobScheduler, atLeast(0)).enqueue(lambdaCaptor.capture());

    List<JobLambda> lambdas = lambdaCaptor.getAllValues();

    for (JobLambda lambda : lambdas) {
      lambda.run();
    }

    for (int size : expectedSizes) {
      String expectedSized = mediaDir + size + File.separator + filename;
      verify(graphicsService, minDimension < size ? never() : times(1))
        .resizeSquare(expectedSquare, expectedSized, size);
    }
  }
}
