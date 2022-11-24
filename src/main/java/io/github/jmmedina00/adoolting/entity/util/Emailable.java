package io.github.jmmedina00.adoolting.entity.util;

import java.io.Serializable;

public interface Emailable extends Serializable {
  EmailData getEmailData();
}
