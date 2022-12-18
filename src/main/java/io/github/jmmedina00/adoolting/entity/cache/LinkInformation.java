package io.github.jmmedina00.adoolting.entity.cache;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.data.redis.core.RedisHash;

@RedisHash(value = "linkinfo", timeToLive = 86400) // TTL = 24 hours
public class LinkInformation implements Serializable {
  private Long id;
  private String actualLink;
  private String title;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getActualLink() {
    return actualLink;
  }

  public void setActualLink(String actualLink) {
    this.actualLink = actualLink;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getPage() {
    String withoutHttp = actualLink.replaceFirst("https?:\\/\\/", "");
    String baseOnly = withoutHttp.replaceAll("\\/.+$", "");
    Matcher matcher = Pattern.compile("(.+\\.)?(.+\\..+)").matcher(baseOnly);
    return matcher.matches() ? matcher.group(2) : "";
  }
}
