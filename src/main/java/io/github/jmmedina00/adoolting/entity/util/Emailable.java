package io.github.jmmedina00.adoolting.entity.util;

import io.github.jmmedina00.adoolting.entity.cache.EmailData;
import java.io.Serializable;

public interface Emailable extends Serializable {
  EmailData getEmailData();
}
