package io.github.jmmedina00.adoolting.dto.interaction;

import io.github.jmmedina00.adoolting.dto.annotation.OnlyImages;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public class NewPost {
  @NotNull
  @NotEmpty
  private String contents;

  @OnlyImages
  private List<MultipartFile> media;

  public String getContents() {
    return contents;
  }

  public void setContents(String contents) {
    this.contents = contents;
  }

  public List<MultipartFile> getMedia() {
    return media;
  }

  public void setMedia(List<MultipartFile> media) {
    this.media = media;
  }
}
