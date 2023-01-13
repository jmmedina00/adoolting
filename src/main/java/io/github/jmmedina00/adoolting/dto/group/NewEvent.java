package io.github.jmmedina00.adoolting.dto.group;

import io.github.jmmedina00.adoolting.dto.annotation.HasFutureThreshold;
import io.github.jmmedina00.adoolting.dto.common.DateExtractOfDate;
import io.github.jmmedina00.adoolting.dto.common.TimeExtractOfDate;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@HasFutureThreshold
public class NewEvent extends NewGroup {
  @NotNull(message = "{error.not_empty}")
  @NotEmpty(message = "{error.not_empty}")
  private String location;

  @NotNull
  private Long createAs;

  @NotEmpty
  private DateExtractOfDate date;

  @NotEmpty
  private TimeExtractOfDate time;

  @NotNull
  private int offsetFromUTC;

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public Long getCreateAs() {
    return createAs;
  }

  public void setCreateAs(Long createAs) {
    this.createAs = createAs;
  }

  public DateExtractOfDate getDate() {
    return date;
  }

  public void setDate(DateExtractOfDate date) {
    this.date = date;
  }

  public TimeExtractOfDate getTime() {
    return time;
  }

  public void setTime(TimeExtractOfDate time) {
    this.time = time;
  }

  public int getOffsetFromUTC() {
    return offsetFromUTC;
  }

  public void setOffsetFromUTC(int offsetFromUTC) {
    this.offsetFromUTC = offsetFromUTC;
  }

  public Date getFinalizedDate() {
    if (date.getValue() == null || time.getValue() == null) {
      return new Date(Long.MAX_VALUE);
    }

    Calendar calendarDate = Calendar.getInstance();
    calendarDate.setTime(date.getValue());
    Calendar calendarTime = Calendar.getInstance();
    calendarTime.setTime(time.getValue());

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
