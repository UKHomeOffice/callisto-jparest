package uk.gov.homeoffice.digital.sas.jparest.utils;

import com.fasterxml.jackson.databind.util.StdDateFormat;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.WebDataBinder;

import java.util.Date;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WebDataBinderFactory {

    @Getter
    private static WebDataBinder webDataBinder = initWebDataBinder();

    private static WebDataBinder initWebDataBinder() {
        var webDataBinder = new WebDataBinder(null);
        var dateFormat2 = new StdDateFormat();
        webDataBinder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat2, true));
        return webDataBinder;
    }

}