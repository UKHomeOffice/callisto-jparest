package uk.gov.homeoffice.digital.sas.jparest.utils;

import java.beans.PropertyEditorSupport;
import java.time.Instant;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

public class CustomInstantEditor extends PropertyEditorSupport {

  @Override
  public String getAsText() {
    Instant value = (Instant) getValue();
    return (value != null ? value.toString() : "");
  }

  @Override
  public void setAsText(@Nullable String text) throws IllegalArgumentException {
    if (!StringUtils.hasText(text)) {
      setValue(null);
    } else {
      setValue(Instant.parse(text));
    }
  }
}
