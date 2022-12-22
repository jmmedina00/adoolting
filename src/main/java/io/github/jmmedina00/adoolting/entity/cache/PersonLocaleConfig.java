package io.github.jmmedina00.adoolting.entity.cache;

import java.io.Serializable;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("localeconfig")
public class PersonLocaleConfig implements Serializable {
  private Long id;
  private String locale;
  private int offsetFromUTC = 0;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getLocale() {
    return locale;
  }

  public void setLocale(String locale) {
    this.locale = locale;
  }

  public int getOffsetFromUTC() {
    return offsetFromUTC;
  }

  public void setOffsetFromUTC(int offsetFromUTC) {
    this.offsetFromUTC = offsetFromUTC;
  }
}
