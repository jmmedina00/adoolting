package io.github.jmmedina00.adoolting.dto.common;

import java.io.Serializable;
import java.util.Date;
import javax.validation.constraints.NotNull;

public abstract class DatePortion implements Serializable {
  @NotNull
  private Date value;

  public DatePortion(@NotNull Date value) {
    this.value = value;
  }

  public Date getValue() {
    return value;
  }

  public abstract String toString();
}
