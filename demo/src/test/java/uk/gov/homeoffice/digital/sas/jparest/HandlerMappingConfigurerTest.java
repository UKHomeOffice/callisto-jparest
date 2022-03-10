package uk.gov.homeoffice.digital.sas.jparest;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Pageable;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import uk.gov.homeoffice.digital.sas.demo.EntitiesApplication;
import uk.gov.homeoffice.digital.sas.demo.models.Record;
import uk.gov.homeoffice.digital.sas.demo.models.*;
import uk.gov.homeoffice.digital.sas.jparest.controller.ResourceApiController;
import uk.gov.homeoffice.digital.sas.jparest.testutils.HandlerMappingConfigurerTestUtil;

import javax.persistence.EntityManager;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes= EntitiesApplication.class)
@WebAppConfiguration
@AutoConfigureTestDatabase
public class HandlerMappingConfigurerTest {


    @Autowired
    private EntityManager entityManager;

    @MockBean
    private PlatformTransactionManager transactionManager;

    @MockBean
    private ApplicationContext context;

    @MockBean
    private ResourceEndpoint resourceEndpoint;

    @MockBean
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    private static boolean CONTEXT_LOADED = false;

    private static final Map<Class<?>, String> RESOURCE_TO_PATH_NAME_MAP = Map.of(
            Artist.class, "artists",
            Concert.class, "concerts",
            Profile.class, "profiles",
            Record.class, "records",
            Session.class, "sessions"
    );


    @BeforeEach
    public void setup() throws NoSuchMethodException, ClassNotFoundException {
        when(context.getBean(RequestMappingHandlerMapping.class)).thenReturn(requestMappingHandlerMapping);

        //We don't want to directly call the method under test for the first test method that is ran
        // as the method under test will be ran automatically once the app context is started
        if (!CONTEXT_LOADED) CONTEXT_LOADED = true;
        else {
            var handlerMappingConfigurer = new HandlerMappingConfigurer(entityManager, transactionManager, context, resourceEndpoint);
            handlerMappingConfigurer.registerUserController();
        }
    }



    //Resources Path Registry Tests
    @Test
    public void registerUserController_listPathsAreRegisteredForResources() throws SecurityException {

        assertThat(RESOURCE_TO_PATH_NAME_MAP).allSatisfy((resourceClass, resourcePathName) -> {

            var path = HandlerMappingConfigurerTestUtil.createApiResourcePath(resourcePathName);
            var expectedRequestMappingInfo = HandlerMappingConfigurerTestUtil.createRequestMappingInfo(
                    requestMappingHandlerMapping, path, RequestMethod.GET);

            var expectedMethod = HandlerMappingConfigurerTestUtil.getMethodFromControllerOrFail(
                    "list", ResourceApiController.class, SpelExpression.class, Pageable.class);

            verify(requestMappingHandlerMapping).registerMapping(eq(expectedRequestMappingInfo), any(), eq(expectedMethod));
        });
    }


    @Test
    public void registerUserController_getPathsAreRegisteredForResources() throws SecurityException {

        assertThat(RESOURCE_TO_PATH_NAME_MAP).allSatisfy((resourceClass, resourcePathName) -> {

            var path = HandlerMappingConfigurerTestUtil.createApiResourcePathWithIdParam(resourcePathName);
            var expectedRequestMappingInfo = HandlerMappingConfigurerTestUtil.createRequestMappingInfo(
                    requestMappingHandlerMapping, path, RequestMethod.GET);

            var expectedMethod = HandlerMappingConfigurerTestUtil.getMethodFromControllerOrFail(
                    "get", ResourceApiController.class, Object.class);

            verify(requestMappingHandlerMapping).registerMapping(eq(expectedRequestMappingInfo), any(), eq(expectedMethod));
        });
    }


    @Test
    public void registerUserController_createPathsAreRegisteredForResources() throws SecurityException {

        assertThat(RESOURCE_TO_PATH_NAME_MAP).allSatisfy((resourceClass, resourcePathName) -> {

            var path = HandlerMappingConfigurerTestUtil.createApiResourcePath(resourcePathName);
            var expectedRequestMappingInfo = HandlerMappingConfigurerTestUtil.createRequestMappingInfo(
                    requestMappingHandlerMapping, path, RequestMethod.POST);

            var expectedMethod = HandlerMappingConfigurerTestUtil.getMethodFromControllerOrFail(
                    "create", ResourceApiController.class, String.class);

            verify(requestMappingHandlerMapping).registerMapping(eq(expectedRequestMappingInfo), any(), eq(expectedMethod));
        });
    }

    @Test
    public void registerUserController_deletePathsAreRegisteredForResources() throws SecurityException {

        assertThat(RESOURCE_TO_PATH_NAME_MAP).allSatisfy((resourceClass, resourcePathName) -> {

            var path = HandlerMappingConfigurerTestUtil.createApiResourcePathWithIdParam(resourcePathName);
            var expectedRequestMappingInfo = HandlerMappingConfigurerTestUtil.createRequestMappingInfo(
                    requestMappingHandlerMapping, path, RequestMethod.DELETE);

            var expectedMethod = HandlerMappingConfigurerTestUtil.getMethodFromControllerOrFail(
                    "delete", ResourceApiController.class, Object.class);

            verify(requestMappingHandlerMapping).registerMapping(eq(expectedRequestMappingInfo), any(), eq(expectedMethod));
        });
    }

    @Test
    public void registerUserController_updatePathsAreRegisteredForResources() throws SecurityException {

        assertThat(RESOURCE_TO_PATH_NAME_MAP).allSatisfy((resourceClass, resourcePathName) -> {

            var path = HandlerMappingConfigurerTestUtil.createApiResourcePathWithIdParam(resourcePathName);
            var expectedRequestMappingInfo = HandlerMappingConfigurerTestUtil.createRequestMappingInfo(
                    requestMappingHandlerMapping, path, RequestMethod.PUT);

            var expectedMethod = HandlerMappingConfigurerTestUtil.getMethodFromControllerOrFail(
                    "update", ResourceApiController.class, Object.class, String.class);

            verify(requestMappingHandlerMapping).registerMapping(eq(expectedRequestMappingInfo), any(), eq(expectedMethod));
        });
    }


    @Test
    public void registerUserController_resourcesAreAddedToResourceEndpoint() throws SecurityException {

        assertThat(RESOURCE_TO_PATH_NAME_MAP).allSatisfy((resourceClass, resourcePathName) -> {

            var path = HandlerMappingConfigurerTestUtil.createApiResourcePath(resourcePathName);
            var entityUtils = new EntityUtils<>(resourceClass, entityManager);
            verify(resourceEndpoint).Add(resourceClass, path, entityUtils.getIdFieldType());
        });
    }



    //Related Resources Path Registry Tests
    @Test
    public void registerUserController_getPathsAreRegisteredForRelatedResources() throws SecurityException {

        RESOURCE_TO_PATH_NAME_MAP.forEach((resourceClass, resourcePathName) -> {

            var entityUtils = new EntityUtils<>(resourceClass, entityManager);
            var relatedResources = entityUtils.getRelatedResources();

            assertThat(relatedResources).allSatisfy(relatedResource -> {

                var relatedPath = HandlerMappingConfigurerTestUtil.createApiRelatedResourcePath(resourcePathName, relatedResource);
                var expectedRelatedRequestMappingInfo = HandlerMappingConfigurerTestUtil.createRequestMappingInfo(
                        requestMappingHandlerMapping, relatedPath, RequestMethod.GET);

                var expectedRelatedMethod = HandlerMappingConfigurerTestUtil.getMethodFromControllerOrFail(
                        "getRelated", ResourceApiController.class, Object.class, String.class, SpelExpression.class, Pageable.class);

                verify(requestMappingHandlerMapping).registerMapping(eq(expectedRelatedRequestMappingInfo), any(), eq(expectedRelatedMethod));
            });
        });
    }

    @Test
    public void registerUserController_deletePathsAreRegisteredForRelatedResources() throws SecurityException {

        RESOURCE_TO_PATH_NAME_MAP.forEach((resourceClass, resourcePathName) -> {

            var entityUtils = new EntityUtils<>(resourceClass, entityManager);
            var relatedResources = entityUtils.getRelatedResources();

            assertThat(relatedResources).allSatisfy(relatedResource -> {

                var relatedPath = HandlerMappingConfigurerTestUtil.createApiRelatedResourcePathWithRelatedId(resourcePathName, relatedResource);
                var expectedRelatedRequestMappingInfo = HandlerMappingConfigurerTestUtil.createRequestMappingInfo(
                        requestMappingHandlerMapping, relatedPath, RequestMethod.DELETE);

                var expectedRelatedMethod = HandlerMappingConfigurerTestUtil.getMethodFromControllerOrFail(
                        "deleteRelated", ResourceApiController.class, Object.class, String.class, Object[].class);

                verify(requestMappingHandlerMapping).registerMapping(eq(expectedRelatedRequestMappingInfo), any(), eq(expectedRelatedMethod));
            });
        });
    }

    @Test
    public void registerUserController_updatePathsAreRegisteredForRelatedResources() throws SecurityException {

        RESOURCE_TO_PATH_NAME_MAP.forEach((resourceClass, resourcePathName) -> {

            var entityUtils = new EntityUtils<>(resourceClass, entityManager);
            var relatedResources = entityUtils.getRelatedResources();

            assertThat(relatedResources).allSatisfy(relatedResource -> {

                var relatedPath = HandlerMappingConfigurerTestUtil.createApiRelatedResourcePathWithRelatedId(resourcePathName, relatedResource);
                var expectedRelatedRequestMappingInfo = HandlerMappingConfigurerTestUtil.createRequestMappingInfo(
                        requestMappingHandlerMapping, relatedPath, RequestMethod.PUT);

                var expectedRelatedMethod = HandlerMappingConfigurerTestUtil.getMethodFromControllerOrFail(
                        "addRelated", ResourceApiController.class, Object.class, String.class, Object[].class);

                verify(requestMappingHandlerMapping).registerMapping(eq(expectedRelatedRequestMappingInfo), any(), eq(expectedRelatedMethod));
            });
        });
    }


    @Test
    public void registerUserController_relatedResourcesAreAddedToResourceEndpoint() throws SecurityException {

        RESOURCE_TO_PATH_NAME_MAP.forEach((resourceClass, resourcePathName) -> {

            var entityUtils = new EntityUtils<>(resourceClass, entityManager);
            var relatedResources = entityUtils.getRelatedResources();

            assertThat(relatedResources).allSatisfy(relatedResource -> {

                var path = HandlerMappingConfigurerTestUtil.createApiResourcePathWithIdParam(resourcePathName) + "/" + relatedResource;
                verify(resourceEndpoint).AddRelated(
                        resourceClass, entityUtils.getRelatedType(relatedResource), path, entityUtils.getRelatedIdType(relatedResource));
            });
        });
    }






}
