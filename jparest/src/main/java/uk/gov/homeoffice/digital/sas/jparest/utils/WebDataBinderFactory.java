package uk.gov.homeoffice.digital.sas.jparest.utils;

import com.fasterxml.jackson.databind.util.StdDateFormat;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.WebDataBinder;

import java.util.Date;
import java.util.Objects;

public class WebDataBinderFactory {

    private static WebDataBinder binder;

    private WebDataBinderFactory() {
    }

    public static WebDataBinder getInstance(){
        if(Objects.isNull(binder)){
            binder = new WebDataBinder(null);
            var dateFormat2 = new StdDateFormat();
            binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat2, true));
        }
        return binder;
    }

}