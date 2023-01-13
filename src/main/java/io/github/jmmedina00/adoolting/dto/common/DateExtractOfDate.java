package io.github.jmmedina00.adoolting.dto.common;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.validation.constraints.NotNull;

public class DateExtractOfDate extends DatePortion {
  static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

  public DateExtractOfDate(@NotNull Date value) {
    super(value);
  }

  public DateExtractOfDate(String date) {
    super(format.parse(date, new ParsePosition(0)));
  }

  @Override
  public String toString() {
    if (getValue() == null) return "";
    return format.format(getValue());
  }
}
