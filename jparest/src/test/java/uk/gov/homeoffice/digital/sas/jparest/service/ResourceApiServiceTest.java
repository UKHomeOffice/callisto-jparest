package uk.gov.homeoffice.digital.sas.jparest.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import uk.gov.homeoffice.digital.sas.jparest.EntityUtils;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityA;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityB;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityTestUtil;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.ResourceConstraintViolationException;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.ResourceNotFoundException;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;
import uk.gov.homeoffice.digital.sas.jparest.repository.TenantRepository;
import uk.gov.homeoffice.digital.sas.jparest.validation.EntityValidator;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.homeoffice.digital.sas.jparest.exceptions.ResourceNotFoundExceptionMessageUtil.deletableRelatedResourcesMessage;
import static uk.gov.homeoffice.digital.sas.jparest.exceptions.ResourceNotFoundExceptionMessageUtil.relatedResourcesMessage;


@ExtendWith(MockitoExtension.class)
class ResourceApiServiceTest<T extends BaseEntity> {

    @Mock
    private EntityUtils<T, ?> entityUtils;

    @Mock
    private EntityValidator entityValidator;

    @Mock
    private TenantRepository<T> repository;

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private TransactionStatus transactionStatus;

    private ResourceApiService<T> resourceApiService;


    private static final UUID RESOURCE_ID = UUID.randomUUID();
    private static final UUID RESOURCE_ID_2 = UUID.randomUUID();
    private static final UUID RESOURCE_ID_3 = UUID.randomUUID();
    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final String RELATED_RESOURCE_NAME = "dummyEntityBSet";
    private static final Class<?> RELATED_RESOURCE_CLASS = DummyEntityB.class;


    @BeforeEach
    private void setup() {
      resourceApiService = new ResourceApiService<T>(entityUtils, transactionManager,
          repository, entityValidator);
    }


    // region get

    @Test
    void getAllResources_resourcesReturned() {
      resourceApiService.getAllResources(TENANT_ID, null, null);
      verify(repository).findAllByTenantId(TENANT_ID, null, null);
    }

    @Test
    void getResource_resourceExists_resourceReturned() {
      T resource = DummyEntityTestUtil.getResource(DummyEntityA.class, RESOURCE_ID, TENANT_ID);
      when(repository.findByIdAndTenantId(TENANT_ID, RESOURCE_ID)).thenReturn(Optional.of(resource));
      var actualResource = resourceApiService.getResource(TENANT_ID, RESOURCE_ID);
      assertThat(actualResource).isEqualTo(resource);
    }

    @Test
    void getResource_resourceDoesNotExist_resourceNotFoundExceptionThrown() {
      assertThatExceptionOfType(ResourceNotFoundException.class)
              .isThrownBy(() -> resourceApiService.getResource(TENANT_ID, RESOURCE_ID));
    }

    // endregion


    // region create

    @Test
    void createResource_entityValidationPassed_resourceCreated() {

      T resourceToSave = DummyEntityTestUtil.getResource(DummyEntityA.class);
      T createdResource = DummyEntityTestUtil.getResource(DummyEntityA.class, RESOURCE_ID, TENANT_ID);

      when(repository.saveAndFlush(resourceToSave)).thenReturn(createdResource);
      when(transactionManager.getTransaction(new DefaultTransactionDefinition())).thenReturn(transactionStatus);

      var actualResource = resourceApiService.createResource(resourceToSave);

      verify(transactionManager).commit(transactionStatus);
      verify(transactionManager, never()).rollback(transactionStatus);
      assertThat(actualResource).isEqualTo(resourceToSave);
    }

    @Test
    void createResource_entityValidationFailed_resourceConstraintViolationExceptionThrown() {

      T resourceToSave = DummyEntityTestUtil.getResource(DummyEntityA.class);

      doThrow(ResourceConstraintViolationException.class).when(entityValidator)
          .validateAndThrowIfErrorsExist(resourceToSave);
      when(transactionManager.getTransaction(new DefaultTransactionDefinition())).thenReturn(transactionStatus);

      assertThatExceptionOfType(ResourceConstraintViolationException.class).isThrownBy(() ->
          resourceApiService.createResource(resourceToSave));
      verify(transactionManager).rollback(transactionStatus);
      verify(transactionManager, never()).commit(transactionStatus);
      verify(repository, never()).saveAndFlush(resourceToSave);

    }

    // endregion


    // region delete

    @Test
    void deleteResource_entityExistsForId_verifyRepositoryDeleteInteraction() {

      when(transactionManager.getTransaction(new DefaultTransactionDefinition())).thenReturn(transactionStatus);
      assertThatNoException().isThrownBy(() -> resourceApiService.deleteResource(TENANT_ID, RESOURCE_ID));

      verify(repository).deleteByIdAndTenantId(TENANT_ID, RESOURCE_ID);
      verify(transactionManager).commit(transactionStatus);
      verify(transactionManager, never()).rollback(transactionStatus);
    }

    @Test
    void deleteResource_entityDoesNotExistForId_resourceNotFoundExceptionThrown() {

      doThrow(NoSuchElementException.class).when(repository)
          .deleteByIdAndTenantId(TENANT_ID, RESOURCE_ID);
      when(transactionManager.getTransaction(new DefaultTransactionDefinition())).thenReturn(transactionStatus);

      assertThatExceptionOfType(ResourceNotFoundException.class).isThrownBy(() ->
          resourceApiService.deleteResource(TENANT_ID, RESOURCE_ID));
      verify(transactionManager, never()).commit(transactionStatus);
    }

    // endregion


    // region update
    @Test
    void updateResource_entityValidationPassed_existingResourceIsUpdated() {

      T existingResource = DummyEntityTestUtil.getResource(DummyEntityA.class, RESOURCE_ID, TENANT_ID);
      T newResource = DummyEntityTestUtil.getResource(DummyEntityA.class, RESOURCE_ID, TENANT_ID);
      var newDummyAResource = (DummyEntityA) newResource;
      newDummyAResource.setProfileId(1L);

      when(repository.findByIdAndTenantId(newResource.getTenantId(), newResource.getId()))
          .thenReturn(Optional.of(existingResource));
      when(transactionManager.getTransaction(new DefaultTransactionDefinition())).thenReturn(transactionStatus);

      resourceApiService.updateResource(newResource);

      var existingDummyAResource = (DummyEntityA) existingResource;
      assertThat(existingResource.getId()).isEqualTo(RESOURCE_ID);
      assertThat(existingResource.getTenantId()).isEqualTo(TENANT_ID);
      assertThat(existingDummyAResource.getProfileId()).isEqualTo(newDummyAResource.getProfileId());
      verify(transactionManager).commit(transactionStatus);
      verify(transactionManager, never()).rollback(transactionStatus);
    }

    @Test
    void updateResource_entityValidationFailed_resourceConstraintViolationExceptionThrown() {

      T newResource = DummyEntityTestUtil.getResource(DummyEntityA.class, RESOURCE_ID, TENANT_ID);
      doThrow(ResourceConstraintViolationException.class).when(entityValidator)
          .validateAndThrowIfErrorsExist(newResource);
      when(transactionManager.getTransaction(new DefaultTransactionDefinition())).thenReturn(transactionStatus);

      assertThatExceptionOfType(ResourceConstraintViolationException.class).isThrownBy(() ->
          resourceApiService.updateResource(newResource));
      verify(transactionManager).rollback(transactionStatus);
      verify(transactionManager, never()).commit(transactionStatus);
      verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void updateResource_originalEntityNotFound_resourceNotFoundExceptionThrown() {

      T newResource = DummyEntityTestUtil.getResource(DummyEntityA.class, RESOURCE_ID, TENANT_ID);
      when(transactionManager.getTransaction(new DefaultTransactionDefinition())).thenReturn(transactionStatus);

      assertThatExceptionOfType(ResourceNotFoundException.class).isThrownBy(() ->
          resourceApiService.updateResource(newResource));
      verify(transactionManager).rollback(transactionStatus);
      verify(transactionManager, never()).commit(transactionStatus);
      verify(repository, never()).saveAndFlush(any());
    }

    // endregion


    // region deleteRelated

    @Test
    void deleteRelatedResources_parentAndRelatedResourcesFound_verifyRepositoryInteraction() {

      //mock getting parent entity
      T parentResource = DummyEntityTestUtil.getResource(
          DummyEntityA.class, RESOURCE_ID, TENANT_ID);
      when(repository.findByIdAndTenantId(TENANT_ID, RESOURCE_ID, RELATED_RESOURCE_NAME)).thenReturn(Optional.of(parentResource));

      //mock getting the related entities and references to delete
      List<UUID> relatedIds = List.of(RESOURCE_ID_2, RESOURCE_ID_3);
      var relatedEntities = relatedIds.stream()
          .map(id -> DummyEntityTestUtil.getResource(RELATED_RESOURCE_CLASS, id, TENANT_ID))
          .collect(Collectors.toList());
      when(entityUtils.getRelatedEntities(parentResource, RELATED_RESOURCE_NAME)).thenReturn(relatedEntities);
      when(entityUtils.getEntityReference(RELATED_RESOURCE_NAME, relatedIds.get(0))).thenReturn(relatedEntities.get(0));
      when(entityUtils.getEntityReference(RELATED_RESOURCE_NAME, relatedIds.get(1))).thenReturn(relatedEntities.get(1));

      when(transactionManager.getTransaction(new DefaultTransactionDefinition())).thenReturn(transactionStatus);
      assertThatNoException().isThrownBy(() -> resourceApiService.deleteRelatedResources(
          TENANT_ID, parentResource.getId(), RELATED_RESOURCE_NAME, relatedIds));

      verify(repository).saveAndFlush(parentResource);
      verify(transactionManager).commit(transactionStatus);
      verify(transactionManager, never()).rollback(transactionStatus);
    }

    @Test
    void deleteRelatedResources_parentResourceNotFound_resourceNotFoundExceptionThrown() {

      when(transactionManager.getTransaction(new DefaultTransactionDefinition())).thenReturn(transactionStatus);
      List<UUID> relatedIds = List.of();
      assertThatExceptionOfType(ResourceNotFoundException.class)
          .isThrownBy(() -> resourceApiService.deleteRelatedResources(
              TENANT_ID, RESOURCE_ID, RELATED_RESOURCE_NAME, relatedIds));

      verify(repository, never()).saveAndFlush(any());
      verify(transactionManager, never()).commit(transactionStatus);
      verify(transactionManager).rollback(transactionStatus);
    }

    @Test
    void deleteRelatedResources_notAllRelatedIdsToDeleteAreRelated_resourceNotFoundExceptionThrown() {

      var relatedEntityId = RESOURCE_ID_2;
      var unrelatedEntityId = RESOURCE_ID_3;
      List<UUID> idsToDelete = List.of(relatedEntityId, unrelatedEntityId);

      //mock getting parent entity
      T parentResource = DummyEntityTestUtil.getResource(
          DummyEntityA.class, RESOURCE_ID, TENANT_ID);
      when(repository.findByIdAndTenantId(TENANT_ID, RESOURCE_ID, RELATED_RESOURCE_NAME)).thenReturn(Optional.of(parentResource));

      //mock getting the related entities to delete except those that are not related for the IDs passed in
      var relatedEntities = new ArrayList<>();
      relatedEntities.add(DummyEntityTestUtil.getResource(RELATED_RESOURCE_CLASS, relatedEntityId, TENANT_ID));
      when(entityUtils.getRelatedEntities(parentResource, RELATED_RESOURCE_NAME)).thenReturn(relatedEntities);

      //mock getting references for ids passed in
      doReturn(RELATED_RESOURCE_CLASS).when(entityUtils).getRelatedType(RELATED_RESOURCE_NAME);
      when(entityUtils.getEntityReference(RELATED_RESOURCE_NAME, relatedEntityId)).thenReturn(relatedEntities.get(0));
      when(entityUtils.getEntityReference(RELATED_RESOURCE_NAME, unrelatedEntityId)).thenReturn(
          DummyEntityTestUtil.getResource(DummyEntityB.class, unrelatedEntityId, TENANT_ID));

      when(transactionManager.getTransaction(new DefaultTransactionDefinition())).thenReturn(transactionStatus);
      assertThatExceptionOfType(ResourceNotFoundException.class).isThrownBy(() ->
              resourceApiService.deleteRelatedResources(
                  TENANT_ID, RESOURCE_ID, RELATED_RESOURCE_NAME, idsToDelete))
          .withMessage(deletableRelatedResourcesMessage(RELATED_RESOURCE_CLASS, List.of(unrelatedEntityId)));

      verify(repository, never()).saveAndFlush(parentResource);
      verify(transactionManager, never()).commit(transactionStatus);
      verify(transactionManager).rollback(transactionStatus);
    }

    // endregion


    // region addRelated

    @Test
    void addRelatedResources_parentAndRelatedResourcesFound_relatedResourcesAdded() {

      //mock getting parent entity
      T parentResource = DummyEntityTestUtil.getResource(
          DummyEntityA.class, RESOURCE_ID, TENANT_ID);
      when(repository.findByIdAndTenantId(TENANT_ID, RESOURCE_ID, RELATED_RESOURCE_NAME)).thenReturn(Optional.of(parentResource));

      //mock getting the related entities and references to add
      List<UUID> relatedIds = List.of(RESOURCE_ID_2, RESOURCE_ID_3);
      var relatedEntities = relatedIds.stream()
          .map(id -> DummyEntityTestUtil.getResource(RELATED_RESOURCE_CLASS, id, TENANT_ID))
          .collect(Collectors.toList());
      doReturn(RELATED_RESOURCE_CLASS).when(entityUtils).getRelatedType(RELATED_RESOURCE_NAME);
      when(repository.countAllByRelationAndTenantId(
          TENANT_ID, RELATED_RESOURCE_CLASS, relatedIds)).thenReturn(Long.valueOf(relatedIds.size()));
      when(entityUtils.getRelatedEntities(parentResource, RELATED_RESOURCE_NAME)).thenReturn(relatedEntities);
      when(entityUtils.getEntityReference(RELATED_RESOURCE_NAME, relatedIds.get(0))).thenReturn(relatedEntities.get(0));
      when(entityUtils.getEntityReference(RELATED_RESOURCE_NAME, relatedIds.get(1))).thenReturn(relatedEntities.get(1));

      when(transactionManager.getTransaction(new DefaultTransactionDefinition())).thenReturn(transactionStatus);
      resourceApiService.addRelatedResources(
          TENANT_ID, parentResource.getId(), RELATED_RESOURCE_NAME, relatedIds);

      verify(repository).saveAndFlush(parentResource);
      verify(transactionManager).commit(transactionStatus);
      verify(transactionManager, never()).rollback(transactionStatus);
    }

    @Test
    void addRelatedResources_parentResourceNotFound_resourceNotFoundExceptionThrown() {

      when(transactionManager.getTransaction(new DefaultTransactionDefinition())).thenReturn(transactionStatus);
      List<UUID> relatedIds = List.of();
      assertThatExceptionOfType(ResourceNotFoundException.class)
          .isThrownBy(() -> resourceApiService.addRelatedResources(
              TENANT_ID, RESOURCE_ID, RELATED_RESOURCE_NAME, relatedIds));

      verify(repository, never()).saveAndFlush(any());
      verify(transactionManager, never()).commit(transactionStatus);
      verify(transactionManager).rollback(transactionStatus);
    }

    @Test
    void addRelatedResources_notAllRelatedIdsAreValidForParent_resourceNotFoundExceptionThrown() {

      //mock getting parent entity
      T parentResource = DummyEntityTestUtil.getResource(
          DummyEntityA.class, RESOURCE_ID, TENANT_ID);
      when(repository.findByIdAndTenantId(TENANT_ID, RESOURCE_ID, RELATED_RESOURCE_NAME)).thenReturn(Optional.of(parentResource));

      //mock failing related Ids resource validation
      List<UUID> relatedIds = List.of(RESOURCE_ID_2, RESOURCE_ID_3);
      doReturn(RELATED_RESOURCE_CLASS).when(entityUtils).getRelatedType(RELATED_RESOURCE_NAME);
      when(repository.countAllByRelationAndTenantId(TENANT_ID, RELATED_RESOURCE_CLASS, relatedIds))
          .thenReturn(0L);

      when(transactionManager.getTransaction(new DefaultTransactionDefinition())).thenReturn(transactionStatus);

      assertThatExceptionOfType(ResourceNotFoundException.class).isThrownBy(() ->
              resourceApiService.addRelatedResources(
                  TENANT_ID, RESOURCE_ID, RELATED_RESOURCE_NAME, relatedIds))
          .withMessage(relatedResourcesMessage(relatedIds));
      verify(repository, never()).saveAndFlush(any());
      verify(transactionManager, never()).commit(transactionStatus);
      verify(transactionManager).rollback(transactionStatus);
    }

    // endregion


    // region getRelated
    @Test
    void getRelatedResources_relatedResourcesReturned() {

      List<?> relatedResources = List.of(
          DummyEntityTestUtil.getResource(RELATED_RESOURCE_CLASS, RESOURCE_ID_2, TENANT_ID),
          DummyEntityTestUtil.getResource(RELATED_RESOURCE_CLASS, RESOURCE_ID_3, TENANT_ID));

      doReturn(relatedResources).when(repository).findAllByIdAndRelationAndTenantId(
          TENANT_ID, RESOURCE_ID, RELATED_RESOURCE_NAME, RELATED_RESOURCE_CLASS, null, null);
      doReturn(RELATED_RESOURCE_CLASS).when(entityUtils).getRelatedType(RELATED_RESOURCE_NAME);

      var actualRelatedResources = resourceApiService.getRelatedResources(TENANT_ID, RESOURCE_ID,
          RELATED_RESOURCE_NAME, null, null);
      assertThat(actualRelatedResources).isEqualTo(relatedResources);
    }

    // endregion

    @Test
    void getEntityId_verifyRepositoryInteraction() {

      T entity = DummyEntityTestUtil.getResource(DummyEntityA.class, RESOURCE_ID,
          TENANT_ID);
      resourceApiService.getEntityId(entity);
      verify(repository).findId(entity);
    }

}
