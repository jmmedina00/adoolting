package io.github.jmmedina00.adoolting.dto.group;

import io.github.jmmedina00.adoolting.dto.annotation.HasFutureThreshold;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

@HasFutureThreshold
public class NewEvent extends NewGroup {
  @NotNull(message = "{error.not_empty}")
  @NotEmpty(message = "{error.not_empty}")
  private String location; // temporary

  @NotNull
  @DateTimeFormat(iso = ISO.DATE)
  private Date date;

  @NotNull
  @DateTimeFormat(pattern = "HH:mm")
  private Date time;

  @NotNull
  private int offsetFromUTC;

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public Date getTime() {
    return time;
  }

  public void setTime(Date time) {
    this.time = time;
  }

  public int getOffsetFromUTC() {
    return offsetFromUTC;
  }

  public void setOffsetFromUTC(int offsetFromUTC) {
    this.offsetFromUTC = offsetFromUTC;
  }

  public Date getFinalizedDate() {
    if (date == null || time == null) {
      return new Date(Long.MAX_VALUE);
    }

    Calendar calendarDate = Calendar.getInstance();
    calendarDate.setTime(date);
    Calendar calendarTime = Calendar.getInstance();
    calendarTime.setTime(time);

    calendarDate.setTimeZone(TimeZone.getTimeZone("UTC"));
    calendarDate.set(
      Calendar.HOUR_OF_DAY,
      calendarTime.get(Calendar.HOUR_OF_DAY)
    );
    calendarDate.set(Calendar.MINUTE, calendarTime.get(Calendar.MINUTE));
    calendarDate.add(Calendar.MINUTE, offsetFromUTC);

    return new Date(calendarDate.getTimeInMillis());
  }
}
