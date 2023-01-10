package io.github.jmmedina00.adoolting.service.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.jmmedina00.adoolting.util.MethodDoesThatNameGenerator;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayNameGeneration(MethodDoesThatNameGenerator.class)
public class GraphicsServiceTest {
  @Autowired
  private GraphicsService graphicsService;

  @Captor
  private ArgumentCaptor<BufferedImage> imageCaptor;

  private String imaginaryFile =
    System.getProperty("java.io.tmpdir") + File.separator + "imaginary";

  private static Stream<Arguments> imageDimensions() {
    return Stream.of(
      Arguments.of(100, 200, 100),
      Arguments.of(300, 50, 50),
      Arguments.of(555, 600, 555)
    );
  }

  @ParameterizedTest
  @MethodSource("imageDimensions")
  public void getImageMinimumDimensionAlwaysGetsSmallestNumberOfImageDimensions(
    int width,
    int height,
    int expected
  )
    throws Exception {
    BufferedImage image = new BufferedImage(
      width,
      height,
      BufferedImage.TYPE_4BYTE_ABGR
    );

    MockedStatic<ImageIO> imageIoUtilities = Mockito.mockStatic(ImageIO.class);
    imageIoUtilities
      .when(() -> ImageIO.read(new File(imaginaryFile)))
      .thenReturn(image);

    int actual = graphicsService.getImageMinimumDimension(imaginaryFile);
    assertEquals(expected, actual);

    imageIoUtilities.closeOnDemand();
  }

  @Test
  public void saveImageFromNetworkReadsImageAndWritesItToFileViaWriter()
    throws Exception {
    BufferedImage image = new BufferedImage(
      200,
      200,
      BufferedImage.TYPE_4BYTE_ABGR
    );

    ImageWriter writer = Mockito.mock(ImageWriter.class);
    File file = new File(imaginaryFile);

    MockedStatic<ImageIO> imageIoUtilities = Mockito.mockStatic(ImageIO.class);
    imageIoUtilities
      .when(() -> ImageIO.read(new URL("http://test.local/image")))
      .thenReturn(image);
    imageIoUtilities
      .when(() -> ImageIO.getImageWritersByMIMEType("image/png"))
      .thenReturn(List.of(writer).iterator());

    graphicsService.saveImageFromNetwork("http://test.local/image", file);

    verify(writer, times(1)).setOutput(any());
    verify(writer, times(1)).write(image);

    imageIoUtilities.closeOnDemand();
  }

  @ParameterizedTest
  @MethodSource("imageDimensions")
  public void snipImageToSquareCreatesSquareImageThatIsAlwaysAtMinimumDimension(
    int width,
    int height,
    int expectedDimension
  )
    throws Exception {
    String sourcePath =
      System.getProperty("java.io.tmpdir") + File.separator + "source.jpg";
    String destPath =
      System.getProperty("java.io.tmpdir") + File.separator + "dest.jpg";

    File source = new File(sourcePath);
    source.createNewFile();
    File dest = new File(destPath);
    dest.delete();

    BufferedImage image = new BufferedImage(
      width,
      height,
      BufferedImage.TYPE_4BYTE_ABGR
    );
    ImageWriter writer = Mockito.mock(ImageWriter.class);

    MockedStatic<ImageIO> imageIoUtilities = Mockito.mockStatic(ImageIO.class);
    imageIoUtilities.when(() -> ImageIO.read(source)).thenReturn(image);
    imageIoUtilities
      .when(() -> ImageIO.getImageWritersByMIMEType("image/jpeg"))
      .thenReturn(List.of(writer).iterator());

    graphicsService.snipImageToSquare(
      sourcePath,
      destPath,
      GraphicsService.NO_OVERWRITING
    );

    verify(writer, times(1)).setOutput(any());
    verify(writer, times(1)).write(imageCaptor.capture());

    BufferedImage written = imageCaptor.getValue();
    assertEquals(expectedDimension, written.getWidth());
    assertEquals(expectedDimension, written.getHeight());
    assertEquals(BufferedImage.TYPE_4BYTE_ABGR, written.getType());

    imageIoUtilities.closeOnDemand();
  }

  @Test
  public void snipImageToSquareExitsEarlyIfSourceDoesNotExist()
    throws Exception {
    String sourcePath =
      System.getProperty("java.io.tmpdir") + File.separator + "source.jpg";
    String destPath =
      System.getProperty("java.io.tmpdir") + File.separator + "dest.jpg";

    File source = new File(sourcePath);
    source.delete();
    File dest = new File(destPath);
    dest.delete();

    MockedStatic<ImageIO> imageIoUtilities = Mockito.mockStatic(ImageIO.class);

    graphicsService.snipImageToSquare(
      sourcePath,
      destPath,
      GraphicsService.NO_OVERWRITING
    );

    imageIoUtilities.verify(() -> ImageIO.read(any(File.class)), never());
    imageIoUtilities.verify(
      () -> ImageIO.getImageWritersByMIMEType("image/jpeg"),
      never()
    );

    imageIoUtilities.closeOnDemand();
  }

  @Test
  public void snipImageToSquareExitsEarlyIfDestinationAlreadyExistsAndSpecifiedByFlag()
    throws Exception {
    String sourcePath =
      System.getProperty("java.io.tmpdir") + File.separator + "source.jpg";
    String destPath =
      System.getProperty("java.io.tmpdir") + File.separator + "dest.jpg";

    File source = new File(sourcePath);
    source.createNewFile();
    File dest = new File(destPath);
    dest.createNewFile();

    MockedStatic<ImageIO> imageIoUtilities = Mockito.mockStatic(ImageIO.class);

    graphicsService.snipImageToSquare(
      sourcePath,
      destPath,
      GraphicsService.NO_OVERWRITING
    );

    imageIoUtilities.verify(() -> ImageIO.read(any(File.class)), never());
    imageIoUtilities.verify(
      () -> ImageIO.getImageWritersByMIMEType("image/jpeg"),
      never()
    );

    imageIoUtilities.closeOnDemand();
  }

  public void snipImageToSquareProceedsEvenIfDestinationAlreadyExistsWhenSpecifiedByFlag()
    throws Exception {
    String sourcePath =
      System.getProperty("java.io.tmpdir") + File.separator + "source.jpg";
    String destPath =
      System.getProperty("java.io.tmpdir") + File.separator + "dest.jpg";

    File source = new File(sourcePath);
    source.createNewFile();
    File dest = new File(destPath);
    dest.createNewFile();

    BufferedImage image = new BufferedImage(
      200,
      200,
      BufferedImage.TYPE_4BYTE_ABGR
    );
    ImageWriter writer = Mockito.mock(ImageWriter.class);

    MockedStatic<ImageIO> imageIoUtilities = Mockito.mockStatic(ImageIO.class);
    imageIoUtilities.when(() -> ImageIO.read(source)).thenReturn(image);
    imageIoUtilities
      .when(() -> ImageIO.getImageWritersByMIMEType("image/jpeg"))
      .thenReturn(List.of(writer).iterator());

    graphicsService.snipImageToSquare(
      sourcePath,
      destPath,
      GraphicsService.OVERWRITE_FILE
    );

    verify(writer, times(1)).setOutput(any());
    verify(writer, times(1)).write(any(BufferedImage.class));

    imageIoUtilities.closeOnDemand();
  }

  private static Stream<Arguments> wantedSizes() {
    return Stream.of(
      Arguments.of(512),
      Arguments.of(256),
      Arguments.of(128),
      Arguments.of(64)
    );
  }

  @ParameterizedTest
  @MethodSource("wantedSizes")
  public void resizeSquareWritesSquareImageOfSpecifiedDimension(int size)
    throws Exception {
    String sourcePath =
      System.getProperty("java.io.tmpdir") + File.separator + "source.jpg";
    String destPath =
      System.getProperty("java.io.tmpdir") + File.separator + "dest.jpg";

    File source = new File(sourcePath);
    source.createNewFile();
    File dest = new File(destPath);
    dest.delete();

    BufferedImage image = new BufferedImage(
      1000,
      1000,
      BufferedImage.TYPE_4BYTE_ABGR
    );
    ImageWriter writer = Mockito.mock(ImageWriter.class);

    MockedStatic<ImageIO> imageIoUtilities = Mockito.mockStatic(ImageIO.class);
    imageIoUtilities.when(() -> ImageIO.read(source)).thenReturn(image);
    imageIoUtilities
      .when(() -> ImageIO.getImageWritersByMIMEType("image/jpeg"))
      .thenReturn(List.of(writer).iterator());

    graphicsService.resizeSquare(
      sourcePath,
      destPath,
      size,
      GraphicsService.NO_OVERWRITING
    );

    verify(writer, times(1)).setOutput(any());
    verify(writer, times(1)).write(imageCaptor.capture());

    BufferedImage written = imageCaptor.getValue();
    assertEquals(size, written.getWidth());
    assertEquals(size, written.getHeight());
    assertEquals(BufferedImage.TYPE_4BYTE_ABGR, written.getType());

    imageIoUtilities.closeOnDemand();
  }

  @Test
  public void resizeSquareExitsEarlyIfSourceDoesNotExist() throws Exception {
    String sourcePath =
      System.getProperty("java.io.tmpdir") + File.separator + "source.jpg";
    String destPath =
      System.getProperty("java.io.tmpdir") + File.separator + "dest.jpg";

    File source = new File(sourcePath);
    source.delete();
    File dest = new File(destPath);
    dest.delete();

    MockedStatic<ImageIO> imageIoUtilities = Mockito.mockStatic(ImageIO.class);

    graphicsService.resizeSquare(
      sourcePath,
      destPath,
      100,
      GraphicsService.NO_OVERWRITING
    );

    imageIoUtilities.verify(() -> ImageIO.read(any(File.class)), never());
    imageIoUtilities.verify(
      () -> ImageIO.getImageWritersByMIMEType("image/jpeg"),
      never()
    );

    imageIoUtilities.closeOnDemand();
  }

  @Test
  public void resizeSquareExitsEarlyIfDestinationAlreadyExistsAndSpecifiedByFlag()
    throws Exception {
    String sourcePath =
      System.getProperty("java.io.tmpdir") + File.separator + "source.jpg";
    String destPath =
      System.getProperty("java.io.tmpdir") + File.separator + "dest.jpg";

    File source = new File(sourcePath);
    source.createNewFile();
    File dest = new File(destPath);
    dest.createNewFile();

    MockedStatic<ImageIO> imageIoUtilities = Mockito.mockStatic(ImageIO.class);

    graphicsService.resizeSquare(
      sourcePath,
      destPath,
      100,
      GraphicsService.NO_OVERWRITING
    );

    imageIoUtilities.verify(() -> ImageIO.read(any(File.class)), never());
    imageIoUtilities.verify(
      () -> ImageIO.getImageWritersByMIMEType("image/jpeg"),
      never()
    );

    imageIoUtilities.closeOnDemand();
  }

  @ParameterizedTest
  @MethodSource("wantedSizes")
  public void resizeSquareWritesSquareImageOfSpecifiedDimension()
    throws Exception {
    String sourcePath =
      System.getProperty("java.io.tmpdir") + File.separator + "source.jpg";
    String destPath =
      System.getProperty("java.io.tmpdir") + File.separator + "dest.jpg";

    File source = new File(sourcePath);
    source.createNewFile();
    File dest = new File(destPath);
    dest.createNewFile();

    BufferedImage image = new BufferedImage(
      1000,
      1000,
      BufferedImage.TYPE_4BYTE_ABGR
    );
    ImageWriter writer = Mockito.mock(ImageWriter.class);

    MockedStatic<ImageIO> imageIoUtilities = Mockito.mockStatic(ImageIO.class);
    imageIoUtilities.when(() -> ImageIO.read(source)).thenReturn(image);
    imageIoUtilities
      .when(() -> ImageIO.getImageWritersByMIMEType("image/jpeg"))
      .thenReturn(List.of(writer).iterator());

    graphicsService.resizeSquare(
      sourcePath,
      destPath,
      200,
      GraphicsService.OVERWRITE_FILE
    );

    verify(writer, times(1)).setOutput(any());
    verify(writer, times(1)).write(any(BufferedImage.class));

    imageIoUtilities.closeOnDemand();
  }
}
