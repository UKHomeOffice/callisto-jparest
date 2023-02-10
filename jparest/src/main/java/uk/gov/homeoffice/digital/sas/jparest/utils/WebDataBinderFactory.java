package uk.gov.homeoffice.digital.sas.jparest.utils;

import com.fasterxml.jackson.databind.util.StdDateFormat;
import java.time.Instant;
import java.util.Date;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.WebDataBinder;
import uk.gov.homeoffice.digital.sas.jparest.editor.CustomInstantEditor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WebDataBinderFactory {

  @Getter
  private static final WebDataBinder webDataBinder = initWebDataBinder();

  private static WebDataBinder initWebDataBinder() {
    var webDataBinder = new WebDataBinder(null);
    var dateFormat2 = new StdDateFormat();
    webDataBinder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat2, true));
    webDataBinder.registerCustomEditor(Instant.class, new CustomInstantEditor());
    return webDataBinder;
  }

}