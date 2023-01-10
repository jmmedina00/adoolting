package io.github.jmmedina00.adoolting.service.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.util.stream.IntStream;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import org.jobrunr.jobs.annotations.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GraphicsService {
  public static final int NO_OVERWRITING = 0;
  public static final int OVERWRITE_FILE = 1;

  private static final Logger logger = LoggerFactory.getLogger(
    GraphicsService.class
  );

  public int getImageMinimumDimension(String path) throws Exception {
    BufferedImage sourceImage = ImageIO.read(new File(path));
    return IntStream
      .of(sourceImage.getWidth(), sourceImage.getHeight())
      .min()
      .getAsInt();
  }

  public void saveImageFromNetwork(String url, File file) throws Exception {
    BufferedImage image = ImageIO.read(new URL(url));
    FileImageOutputStream stream = new FileImageOutputStream(file);
    ImageWriter writer = ImageIO.getImageWritersByMIMEType("image/png").next();

    logger.info(
      "Saving image fetched from {} as file {}",
      url,
      file.getAbsolutePath()
    );

    writer.setOutput(stream);
    writer.write(image);
    stream.close();
  }

  public void snipImageToSquare(
    String sourcePath,
    String destPath,
    int overwriteFlag
  )
    throws Exception {
    File source = new File(sourcePath);
    File destination = new File(destPath);

    if (
      !source.exists() ||
      (overwriteFlag == NO_OVERWRITING && destination.exists())
    ) {
      logger.debug("Unable to continue with snip of {}", sourcePath);
      return;
    }

    BufferedImage sourceImage = ImageIO.read(source);
    int type = getProperImageType(sourceImage);
    int minDimension = getImageMinimumDimension(sourcePath);
    BufferedImage square = new BufferedImage(minDimension, minDimension, type);
    Graphics2D graphics = prepareGraphics(square);
    drawOnToGraphics(graphics, sourceImage);
    graphics.dispose();
    writeImageToTargetFile(source, destination, square);

    logger.info(
      "Image {} snipped to {}, size is {} pixels",
      sourcePath,
      destPath,
      minDimension
    );
  }

  @Job(name = "Scale squared image")
  public void resizeSquare(
    String sourcePath,
    String destPath,
    int size,
    int overwriteFlag
  )
    throws Exception {
    File square = new File(sourcePath);
    File destination = new File(destPath);

    if (
      !square.exists() ||
      (overwriteFlag == NO_OVERWRITING && destination.exists())
    ) {
      logger.debug("Unable to continue scaling {} to {}", sourcePath, destPath);
      return;
    }

    BufferedImage sourceImage = ImageIO.read(square);
    int imageType = sourceImage.getColorModel().hasAlpha()
      ? BufferedImage.TYPE_4BYTE_ABGR
      : sourceImage.getType();

    BufferedImage target = new BufferedImage(size, size, imageType);
    Graphics2D graphics = prepareGraphics(target);
    graphics.drawImage(sourceImage, 0, 0, size, size, null);
    graphics.dispose();
    writeImageToTargetFile(square, destination, target);

    logger.info(
      "Image {} has been scaled to {} pixels and saved to {}",
      sourcePath,
      size,
      destPath
    );
  }

  private int getProperImageType(BufferedImage img) {
    return img.getColorModel().hasAlpha()
      ? BufferedImage.TYPE_4BYTE_ABGR
      : img.getType(); // Account for transparent image cases, fallback for JPGs - writer doesn't like it
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
    FileImageOutputStream outputStream = new FileImageOutputStream(target);

    writer.setOutput(outputStream);
    writer.write(image);
    outputStream.close();
  }
}
