package io.github.jmmedina00.adoolting.service;

import io.github.jmmedina00.adoolting.entity.SuperEntry;
import io.github.jmmedina00.adoolting.repository.SuperEntryRepository;
import java.io.PrintStream;
import java.util.List;
import java.util.Random;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SuperEntryService {
  @Autowired
  private SuperEntryRepository superEntryRepository;

  @Autowired
  private JobScheduler jobScheduler;

  private Random random = new Random();

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
}
