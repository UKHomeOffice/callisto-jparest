package uk.gov.homeoffice.digital.sas.jparest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import uk.gov.homeoffice.digital.sas.jparest.controller.ResourceApiController;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityA;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityB;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityC;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityD;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityH;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

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

    private static Stream<Arguments> resources() {
        return Stream.of(
                Arguments.of(DummyEntityA.class, "dummyEntityAs"),
                Arguments.of(DummyEntityB.class, "dummyEntityBs"),
                Arguments.of(DummyEntityC.class, "dummyentityc"),
                Arguments.of(DummyEntityD.class, "dummyentityd"));
    }

    @BeforeEach
    public void setup() {
        when(context.getBean(RequestMappingHandlerMapping.class)).thenReturn(requestMappingHandlerMapping);
        handlerMappingConfigurer = new HandlerMappingConfigurer(entityManager, transactionManager, context,
                resourceEndpoint);
    }

    @Test
    void registerUserController_classAnnotatedAsResourceButDoesNotExtendBaseEntity_restfulEndpointsNotRegistered() {

        var resourceName = "dummyEntityHs";
        var expectedCalls = List.of(
                List.of("{GET [/resources/" + resourceName + "], produces [application/json]}", "list"),
                List.of("{GET [/resources/" + resourceName + "/{id}], produces [application/json]}", "get"),
                List.of("{POST [/resources/" + resourceName + "], produces [application/json]}", "create"),
                List.of("{DELETE [/resources/" + resourceName + "/{id}], produces [application/json]}", "delete"),
                List.of("{PUT [/resources/" + resourceName + "/{id}], produces [application/json]}", "update"));

        assertThatNoException().isThrownBy(() -> handlerMappingConfigurer.registerUserController());

        for (var expected : expectedCalls) {
            Mockito.verify(requestMappingHandlerMapping, never()).registerMapping(
                    argThat((requestMappingInfo) -> requestMappingInfo.toString().equals(expected.get(0))),
                    argThat((controller) -> ((ResourceApiController<?,?>) controller).getEntityType().equals(DummyEntityH.class)),
                    argThat((method) -> method.getName().equals(expected.get(1))));
        }

    }

    @ParameterizedTest
    @MethodSource("resources")
    void registerUserController_classAnnotatedAsResource_registersRestfulEndpoints(
            Class<?> clazz, String resourceName) {
        var expectedCalls = List.of(
                List.of("{GET [/resources/" + resourceName + "], produces [application/json]}", "list"),
                List.of("{GET [/resources/" + resourceName + "/{id}], produces [application/json]}", "get"),
                List.of("{POST [/resources/" + resourceName + "], produces [application/json]}", "create"),
                List.of("{DELETE [/resources/" + resourceName + "/{id}], produces [application/json]}", "delete"),
                List.of("{PUT [/resources/" + resourceName + "/{id}], produces [application/json]}", "update"));
        assertThatNoException().isThrownBy(() -> handlerMappingConfigurer.registerUserController());
        verifyExpectedHandlerMappingCalls(requestMappingHandlerMapping, clazz, expectedCalls);
    }

    @Test
    void registerUserController_classAnnotatedAsResourceWithManyToManyAnnotation_registersRelatedEndpoints() {
        var expectedCalls = List.of(
                List.of("{GET [/resources/dummyEntityAs/{id}/{relation:\\QdummyEntityBSet\\E}], produces [application/json]}",
                        "getRelated"),
                List.of("{DELETE [/resources/dummyEntityAs/{id}/{relation:\\QdummyEntityBSet\\E}/{relatedIds}], produces [application/json]}",
                        "deleteRelated"),
                List.of("{PUT [/resources/dummyEntityAs/{id}/{relation:\\QdummyEntityBSet\\E}/{relatedIds}], produces [application/json]}",
                        "addRelated"));
        assertThatNoException().isThrownBy(() -> handlerMappingConfigurer.registerUserController());
        verifyExpectedHandlerMappingCalls(requestMappingHandlerMapping, DummyEntityA.class, expectedCalls);
    }

    private void verifyExpectedHandlerMappingCalls(
            RequestMappingHandlerMapping requestMappingHandlerMapping2,
            Class<?> clazz, List<List<String>> expectedCalls) {
        for (var expected : expectedCalls) {
            Mockito.verify(requestMappingHandlerMapping).registerMapping(
                    argThat((a) -> a.toString().equals(expected.get(0))),
                    argThat((b) -> ((ResourceApiController<?,?>) b).getEntityType().equals(clazz)),
                    argThat((c) -> c.getName().equals(expected.get(1))));
        }
    }


}