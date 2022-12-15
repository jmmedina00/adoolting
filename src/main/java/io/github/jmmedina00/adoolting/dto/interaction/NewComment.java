package io.github.jmmedina00.adoolting.dto.interaction;

import io.github.jmmedina00.adoolting.dto.annotation.OnlyImage;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public class NewComment extends NewInteractionWithMedia {
  @OnlyImage
  private MultipartFile file;

  public MultipartFile getFile() {
    return file;
  }

  public void setFile(MultipartFile file) {
    this.file = file;
  }

  @Override
  public List<MultipartFile> getMedia() {
    return List.of(file);
  }
}
