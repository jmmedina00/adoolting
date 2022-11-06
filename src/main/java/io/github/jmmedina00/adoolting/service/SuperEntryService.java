package io.github.jmmedina00.adoolting.service;

import io.github.jmmedina00.adoolting.entity.SuperEntry;
import io.github.jmmedina00.adoolting.repository.SuperEntryRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SuperEntryService {
  @Autowired
  private SuperEntryRepository superEntryRepository;

  public List<SuperEntry> getEntries() {
    return superEntryRepository.findByDeletedAtIsNull();
  }

  public void createEntry(String name) {
    SuperEntry entry = new SuperEntry(name);
    superEntryRepository.save(entry);
  }

  public void deleteEntry(Long id) {
    SuperEntry entry = superEntryRepository.getReferenceById(id);
    entry.delete();
    superEntryRepository.save(entry);
  }
}
