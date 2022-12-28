package io.github.jmmedina00.adoolting.dto.interaction;

import io.github.jmmedina00.adoolting.dto.annotation.OnlyImages;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public class NewPost extends NewInteractionWithMedia {
  @OnlyImages
  private List<MultipartFile> media;

  public NewPost() {}

  public NewPost(Long personId) {
    super(personId);
  }

  public List<MultipartFile> getMedia() {
    return media;
  }

  public void setMedia(List<MultipartFile> media) {
    this.media = media;
  }
}
