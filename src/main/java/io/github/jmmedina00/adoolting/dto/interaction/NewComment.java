package io.github.jmmedina00.adoolting.dto.interaction;

import io.github.jmmedina00.adoolting.dto.annotation.OnlyImage;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public class NewComment {
  @NotNull
  @NotEmpty
  private String content;

  @OnlyImage
  private MultipartFile file;

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public MultipartFile getFile() {
    return file;
  }

  public void setFile(MultipartFile file) {
    this.file = file;
  }
}
