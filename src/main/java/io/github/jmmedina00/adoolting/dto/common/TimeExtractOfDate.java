package io.github.jmmedina00.adoolting.dto.common;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.validation.constraints.NotNull;

public class TimeExtractOfDate extends DatePortion {
  static final SimpleDateFormat format = new SimpleDateFormat("HH:mm");

  public TimeExtractOfDate(@NotNull Date value) {
    super(value);
  }

  public TimeExtractOfDate(String date) {
    super(format.parse(date, new ParsePosition(0)));
  }

  @Override
  public String toString() {
    if (getValue() == null) return "";
    return format.format(getValue());
  }
}
