package uk.gov.homeoffice.digital.sas.jparest;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Pageable;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import uk.gov.homeoffice.digital.sas.jparest.annotation.Resource;
import uk.gov.homeoffice.digital.sas.jparest.controller.ResourceApiController;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityA;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityB;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityC;
import uk.gov.homeoffice.digital.sas.jparest.testutils.HandlerMappingConfigurerTestUtil;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@Transactional
@ContextConfiguration(locations = "/test-context.xml")
class HandlerMappingConfigurerTest {


    @PersistenceContext
    private EntityManager entityManager;

    @MockBean
    private PlatformTransactionManager transactionManager;

    @MockBean
    private ApplicationContext context;

    @MockBean
    private ResourceEndpoint resourceEndpoint;

    @MockBean
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    private HandlerMappingConfigurer handlerMappingConfigurer;

    private static final Map<String, Class<?>> RESOURCE_TO_PATH_NAME_MAP = Map.of(
            DummyEntityA.class.getAnnotation(Resource.class).path(), DummyEntityA.class,
            DummyEntityB.class.getAnnotation(Resource.class).path(), DummyEntityB.class,
            DummyEntityC.class.getAnnotation(Entity.class).name().toLowerCase(), DummyEntityC.class
    );


    @BeforeEach
    public void setup() {
        when(context.getBean(RequestMappingHandlerMapping.class)).thenReturn(requestMappingHandlerMapping);
        handlerMappingConfigurer = new HandlerMappingConfigurer(entityManager, transactionManager, context, resourceEndpoint);
    }



    //Resources Path Registry Tests
    @Test
    void registerUserController_listPathsAreRegisteredForResources() {

        assertDoesNotThrow(() -> handlerMappingConfigurer.registerUserController());
        RESOURCE_TO_PATH_NAME_MAP.forEach((resourcePathName, resourceClass) -> {

            var path = HandlerMappingConfigurerTestUtil.createApiResourcePath(resourcePathName);
            var expectedRequestMappingInfo = HandlerMappingConfigurerTestUtil.createRequestMappingInfo(
                    requestMappingHandlerMapping, path, RequestMethod.GET);

            var expectedMethod = HandlerMappingConfigurerTestUtil.getMethodFromControllerOrFail(
                    "list", ResourceApiController.class, SpelExpression.class, Pageable.class);

            verify(requestMappingHandlerMapping).registerMapping(eq(expectedRequestMappingInfo), any(), eq(expectedMethod));
        });
    }


    @Test
    void registerUserController_getPathsAreRegisteredForResources() {

        assertDoesNotThrow(() -> handlerMappingConfigurer.registerUserController());
        RESOURCE_TO_PATH_NAME_MAP.forEach((resourcePathName, resourceClass) -> {

            var path = HandlerMappingConfigurerTestUtil.createApiResourcePathWithIdParam(resourcePathName);
            var expectedRequestMappingInfo = HandlerMappingConfigurerTestUtil.createRequestMappingInfo(
                    requestMappingHandlerMapping, path, RequestMethod.GET);

            var expectedMethod = HandlerMappingConfigurerTestUtil.getMethodFromControllerOrFail(
                    "get", ResourceApiController.class, Object.class);

            verify(requestMappingHandlerMapping).registerMapping(eq(expectedRequestMappingInfo), any(), eq(expectedMethod));
        });
    }


    @Test
    void registerUserController_createPathsAreRegisteredForResources() {

        assertDoesNotThrow(() -> handlerMappingConfigurer.registerUserController());
        RESOURCE_TO_PATH_NAME_MAP.forEach((resourcePathName, resourceClass) -> {

            var path = HandlerMappingConfigurerTestUtil.createApiResourcePath(resourcePathName);
            var expectedRequestMappingInfo = HandlerMappingConfigurerTestUtil.createRequestMappingInfo(
                    requestMappingHandlerMapping, path, RequestMethod.POST);

            var expectedMethod = HandlerMappingConfigurerTestUtil.getMethodFromControllerOrFail(
                    "create", ResourceApiController.class, String.class);

            verify(requestMappingHandlerMapping).registerMapping(eq(expectedRequestMappingInfo), any(), eq(expectedMethod));
        });
    }

    @Test
    void registerUserController_deletePathsAreRegisteredForResources() {

        assertDoesNotThrow(() -> handlerMappingConfigurer.registerUserController());
        RESOURCE_TO_PATH_NAME_MAP.forEach((resourcePathName, resourceClass) -> {

            var path = HandlerMappingConfigurerTestUtil.createApiResourcePathWithIdParam(resourcePathName);
            var expectedRequestMappingInfo = HandlerMappingConfigurerTestUtil.createRequestMappingInfo(
                    requestMappingHandlerMapping, path, RequestMethod.DELETE);

            var expectedMethod = HandlerMappingConfigurerTestUtil.getMethodFromControllerOrFail(
                    "delete", ResourceApiController.class, Object.class);

            verify(requestMappingHandlerMapping).registerMapping(eq(expectedRequestMappingInfo), any(), eq(expectedMethod));
        });
    }

    @Test
    void registerUserController_updatePathsAreRegisteredForResources() {

        assertDoesNotThrow(() -> handlerMappingConfigurer.registerUserController());
        RESOURCE_TO_PATH_NAME_MAP.forEach((resourcePathName, resourceClass) -> {

            var path = HandlerMappingConfigurerTestUtil.createApiResourcePathWithIdParam(resourcePathName);
            var expectedRequestMappingInfo = HandlerMappingConfigurerTestUtil.createRequestMappingInfo(
                    requestMappingHandlerMapping, path, RequestMethod.PUT);

            var expectedMethod = HandlerMappingConfigurerTestUtil.getMethodFromControllerOrFail(
                    "update", ResourceApiController.class, Object.class, String.class);

            verify(requestMappingHandlerMapping).registerMapping(eq(expectedRequestMappingInfo), any(), eq(expectedMethod));
        });
    }


    @Test
    void registerUserController_resourcesAreAddedToResourceEndpoint() {

        assertDoesNotThrow(() -> handlerMappingConfigurer.registerUserController());
        RESOURCE_TO_PATH_NAME_MAP.forEach((resourcePathName, resourceClass) -> {

            var path = HandlerMappingConfigurerTestUtil.createApiResourcePath(resourcePathName);
            var entityUtils = new EntityUtils<>(resourceClass, entityManager);
            verify(resourceEndpoint).add(resourceClass, path, entityUtils.getIdFieldType());
        });
    }



    //Related Resources Path Registry Tests
    @Test
    void registerUserController_getPathsAreRegisteredForRelatedResources() {

        assertDoesNotThrow(() -> handlerMappingConfigurer.registerUserController());
        var resourceClass = DummyEntityA.class;
        var resourcePathName = DummyEntityA.class.getAnnotation(Resource.class).path();

        var entityUtils = new EntityUtils<>(resourceClass, entityManager);
        var relatedResources = entityUtils.getRelatedResources();

        assertThat(relatedResources).isNotNull().hasSize(1);
        assertThat(relatedResources).allSatisfy(relatedResource -> {

            var relatedPath = HandlerMappingConfigurerTestUtil.createApiRelatedResourcePath(resourcePathName, relatedResource);
            var expectedRelatedRequestMappingInfo = HandlerMappingConfigurerTestUtil.createRequestMappingInfo(
                    requestMappingHandlerMapping, relatedPath, RequestMethod.GET);

            var expectedRelatedMethod = HandlerMappingConfigurerTestUtil.getMethodFromControllerOrFail(
                    "getRelated", ResourceApiController.class, Object.class, String.class, SpelExpression.class, Pageable.class);

            verify(requestMappingHandlerMapping).registerMapping(eq(expectedRelatedRequestMappingInfo), any(), eq(expectedRelatedMethod));
        });
    }

    @Test
    void registerUserController_deletePathsAreRegisteredForRelatedResources() {

        assertDoesNotThrow(() -> handlerMappingConfigurer.registerUserController());
        var resourceClass = DummyEntityA.class;
        var resourcePathName = DummyEntityA.class.getAnnotation(Resource.class).path();

        var entityUtils = new EntityUtils<>(resourceClass, entityManager);
        var relatedResources = entityUtils.getRelatedResources();

        assertThat(relatedResources).isNotNull().hasSize(1);
        assertThat(relatedResources).allSatisfy(relatedResource -> {

            var relatedPath = HandlerMappingConfigurerTestUtil.createApiRelatedResourcePathWithRelatedId(resourcePathName, relatedResource);
            var expectedRelatedRequestMappingInfo = HandlerMappingConfigurerTestUtil.createRequestMappingInfo(
                    requestMappingHandlerMapping, relatedPath, RequestMethod.DELETE);

            var expectedRelatedMethod = HandlerMappingConfigurerTestUtil.getMethodFromControllerOrFail(
                    "deleteRelated", ResourceApiController.class, Object.class, String.class, Object[].class);

            verify(requestMappingHandlerMapping).registerMapping(eq(expectedRelatedRequestMappingInfo), any(), eq(expectedRelatedMethod));
        });
    }

    @Test
    void registerUserController_updatePathsAreRegisteredForRelatedResources() {

        assertDoesNotThrow(() -> handlerMappingConfigurer.registerUserController());
        var resourceClass = DummyEntityA.class;
        var resourcePathName = DummyEntityA.class.getAnnotation(Resource.class).path();

        var entityUtils = new EntityUtils<>(resourceClass, entityManager);
        var relatedResources = entityUtils.getRelatedResources();

        assertThat(relatedResources).isNotNull().hasSize(1);
        assertThat(relatedResources).allSatisfy(relatedResource -> {

            var relatedPath = HandlerMappingConfigurerTestUtil.createApiRelatedResourcePathWithRelatedId(resourcePathName, relatedResource);
            var expectedRelatedRequestMappingInfo = HandlerMappingConfigurerTestUtil.createRequestMappingInfo(
                    requestMappingHandlerMapping, relatedPath, RequestMethod.PUT);

            var expectedRelatedMethod = HandlerMappingConfigurerTestUtil.getMethodFromControllerOrFail(
                    "addRelated", ResourceApiController.class, Object.class, String.class, Object[].class);

            verify(requestMappingHandlerMapping).registerMapping(eq(expectedRelatedRequestMappingInfo), any(), eq(expectedRelatedMethod));
        });

    }


    @Test
    void registerUserController_relatedResourcesAreAddedToResourceEndpoint() {

        assertDoesNotThrow(() -> handlerMappingConfigurer.registerUserController());
        var resourceClass = DummyEntityA.class;
        var resourcePathName = DummyEntityA.class.getAnnotation(Resource.class).path();

        var entityUtils = new EntityUtils<>(resourceClass, entityManager);
        var relatedResources = entityUtils.getRelatedResources();

        assertThat(relatedResources).isNotNull().hasSize(1);
        assertThat(relatedResources).allSatisfy(relatedResource -> {

            var path = HandlerMappingConfigurerTestUtil.createApiResourcePathWithIdParam(resourcePathName) + "/" + relatedResource;
            verify(resourceEndpoint).addRelated(
                    resourceClass, entityUtils.getRelatedType(relatedResource), path, entityUtils.getRelatedIdType(relatedResource));
        });
    }

}