package io.github.jmmedina00.adoolting.dto.interaction;

import io.github.jmmedina00.adoolting.dto.annotation.OnlyImageRequired;
import org.springframework.web.multipart.MultipartFile;

public class ProfilePictureFile {
  @OnlyImageRequired
  private MultipartFile file;

  public MultipartFile getFile() {
    return file;
  }

  public void setFile(MultipartFile file) {
    this.file = file;
  }
}
