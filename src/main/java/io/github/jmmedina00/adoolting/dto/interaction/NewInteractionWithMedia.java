package io.github.jmmedina00.adoolting.dto.interaction;

import io.github.jmmedina00.adoolting.dto.annotation.OnlyOneKindOfMedia;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;
import org.springframework.web.multipart.MultipartFile;

@OnlyOneKindOfMedia
public abstract class NewInteractionWithMedia {
  @NotNull
  @NotEmpty
  private String content;

  @URL
  private String url;

  @NotNull
  private Long postAs;

  public NewInteractionWithMedia() {}

  public NewInteractionWithMedia(Long personId) {
    setPostAs(personId);
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public Long getPostAs() {
    return postAs;
  }

  public void setPostAs(Long postAs) {
    this.postAs = postAs;
  }

  public abstract List<MultipartFile> getMedia();
}
