package uk.gov.homeoffice.digital.sas.jparest.editor;

import java.beans.PropertyEditorSupport;
import java.time.LocalDate;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

public class LocalDateEditor extends PropertyEditorSupport {

  @Override
  public String getAsText() {
    LocalDate value = (LocalDate) getValue();
    return (value != null ? value.toString() : "");
  }

  @Override
  public void setAsText(@Nullable String text) throws IllegalArgumentException {
    if (!StringUtils.hasText(text)) {
      setValue(null);
    } else {
      setValue(LocalDate.parse(text));
    }
  }
}
