package io.github.jmmedina00.adoolting.dto;

import java.util.Date;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

public class NewEvent extends NewGroup {
  @NotNull(message = "{error.not_empty}")
  @NotEmpty(message = "{error.not_empty}")
  private String location; // temporary

  @DateTimeFormat(iso = ISO.DATE_TIME)
  private Date happeningAt;

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public Date getHappeningAt() {
    return happeningAt;
  }

  public void setHappeningAt(Date happeningAt) {
    this.happeningAt = happeningAt;
  }
}
