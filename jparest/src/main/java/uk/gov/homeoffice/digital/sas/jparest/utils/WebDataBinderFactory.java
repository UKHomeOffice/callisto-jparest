package uk.gov.homeoffice.digital.sas.jparest.utils;

import com.fasterxml.jackson.databind.util.StdDateFormat;
import lombok.Getter;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.WebDataBinder;

import java.util.Date;
import java.util.Objects;

public class WebDataBinderFactory {

    @Getter
    private static WebDataBinder webDataBinder;

    static {
        webDataBinder = new WebDataBinder(null);
        var dateFormat2 = new StdDateFormat();
        webDataBinder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat2, true));
    }

    private WebDataBinderFactory() {
    }

}