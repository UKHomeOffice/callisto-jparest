package uk.gov.homeoffice.digital.sas.jparest.testutils;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

import static org.assertj.core.api.Fail.fail;

public class HandlerMappingConfigurerTestUtil {

    private HandlerMappingConfigurerTestUtil() {
        // no instantiation
    }

    private static final String API_ROOT_PATH = "/resources";


    public static RequestMappingInfo createRequestMappingInfo(RequestMappingHandlerMapping requestMappingHandlerMapping,
                                                              String path,
                                                              RequestMethod requestMethod) {
        var builderOptions = new RequestMappingInfo.BuilderConfiguration();
        builderOptions.setPathMatcher(requestMappingHandlerMapping.getPathMatcher());
        builderOptions.setPatternParser(requestMappingHandlerMapping.getPatternParser());

        var builder = RequestMappingInfo.paths(path).options(builderOptions)
                .methods(requestMethod)
                .produces(MediaType.APPLICATION_JSON_VALUE);

        return builder.build();
    }


    public static String createApiResourcePath(String resourcePathName) {
        return API_ROOT_PATH + "/" + resourcePathName;
    }

    public static String createApiResourcePathWithIdParam(String resourcePathName) {
        return createApiResourcePath(resourcePathName) + "/{id}";
    }

    public static String createApiRelatedResourcePath(String parentResourcePathName, String relatedResource) {
        return createApiResourcePathWithIdParam(parentResourcePathName) + "/{relation:" + Pattern.quote(relatedResource) + "}";
    }

    public static String createApiRelatedResourcePathWithRelatedId(String parentResourcePathName, String relatedResource) {
        return createApiRelatedResourcePath(parentResourcePathName, relatedResource) + "/{relatedId}";
    }


    public static Method getMethodFromControllerOrFail(String methodName,
                                                       Class<?> controllerClass,
                                                       Class<?>... parameterTypes) {

        Method method = null;
        try {
            method = controllerClass.getDeclaredMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            fail(String.format("Method: '%s' could not be found within controller: %s", methodName, controllerClass));
        }
        return method;
    }



}
