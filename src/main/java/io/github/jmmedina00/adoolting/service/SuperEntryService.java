package io.github.jmmedina00.adoolting.service;

import io.github.jmmedina00.adoolting.entity.SuperEntry;
import io.github.jmmedina00.adoolting.repository.SuperEntryRepository;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import org.apache.commons.io.FilenameUtils;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SuperEntryService {
  @Autowired
  private SuperEntryRepository superEntryRepository;

  @Autowired
  private JobScheduler jobScheduler;

  private Random random = new Random();

  @Value("${user.dir}")
  private String workDirectory;

  public List<SuperEntry> getEntries() {
    return superEntryRepository.findByDeletedAtIsNull();
  }

  public void createEntry(String name) {
    SuperEntry entry = new SuperEntry(name);
    superEntryRepository.save(entry);
    jobScheduler.enqueue(() -> writeNameToFile(name));
  }

  public void deleteEntry(Long id) {
    SuperEntry entry = superEntryRepository.getReferenceById(id);
    entry.delete();
    superEntryRepository.save(entry);
  }

  public void saveImage(MultipartFile file) throws Exception {
    String extension = FilenameUtils.getExtension(file.getOriginalFilename());
    String path = workDirectory + "/data/cdn/test/" + random.nextInt() + "." + extension;

    File fileToWrite = new File(path);
    file.transferTo(fileToWrite);
    jobScheduler.enqueue(() -> scaleImage(path));
  }

  @Job(name = "Write to file")
  public void writeNameToFile(String name) throws Exception {
    PrintStream ps = new PrintStream(
      "./data/cdn/test/" + random.nextInt() + ".txt"
    );
    ps.println(name);
    ps.println(name.toUpperCase());
    ps.println(name.toLowerCase());
    ps.println(name.intern());
    ps.close();
  }

  @Job(name = "Scale image")
  public void scaleImage(String path) throws Exception {
    String filename = FilenameUtils.getName(path);

    InputStream stream = new FileInputStream(path);
    BufferedImage source = ImageIO.read(stream);

    // https://stackoverflow.com/questions/4756268/how-to-resize-the-buffered-image-n-graphics-2d-in-java
    BufferedImage target128 = new BufferedImage(128, 128, source.getType());
    Graphics2D graphics128 = target128.createGraphics();
    graphics128.drawImage(source, 0, 0, 128, 128, null);
    graphics128.dispose();
    ImageIO.write(
      target128,
      "jpeg",
      new File("./data/cdn/test/big/" + filename)
    );

    BufferedImage target64 = new BufferedImage(64, 64, source.getType());
    Graphics2D graphics64 = target64.createGraphics();
    graphics64.drawImage(source, 0, 0, 64, 64, null);
    graphics64.dispose();
    ImageIO.write(
      target64,
      "jpeg",
      new File("./data/cdn/test/smol/" + filename)
    );
  }
}
