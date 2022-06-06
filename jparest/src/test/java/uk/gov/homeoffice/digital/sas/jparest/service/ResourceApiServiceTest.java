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
import uk.gov.homeoffice.digital.sas.jparest.JpaRestRepository;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityA;
import uk.gov.homeoffice.digital.sas.jparest.entityutils.testentities.DummyEntityB;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.ResourceNotFoundException;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.homeoffice.digital.sas.jparest.exceptions.ResourceNotFoundExceptionMessageUtil.deletableRelatedResourcesMessage;
import static uk.gov.homeoffice.digital.sas.jparest.exceptions.ResourceNotFoundExceptionMessageUtil.relatedResourcesMessage;


@ExtendWith(MockitoExtension.class)
public class ResourceApiServiceTest <T extends BaseEntity> {

    @Mock
    private EntityUtils<T> entityUtils;

    @Mock
    private JpaRestRepository<T, Serializable> repository;

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private TransactionStatus transactionStatus;

    private ResourceApiService<T> resourceApiService;


    private static final UUID RESOURCE_ID = UUID.randomUUID();
    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final String RELATED_RESOURCE_NAME = "dummyEntityBSet";
    private static final Class<?> RELATED_RESOURCE_CLASS = DummyEntityB.class;


    @BeforeEach
    private void setup() {
        resourceApiService = new ResourceApiService<>(entityUtils, repository, transactionManager);
    }


    // region get

    @Test
    void getAllResources_verifyRepositoryInteraction() {
        resourceApiService.getAllResources(TENANT_ID, null, null);
        verify(repository).findAllByTenantId(TENANT_ID, null, null);
    }

    @Test
    void getResource_withoutFilterAndResourceExists_resourceReturned() {
        T resource = ResourceTestUtil.getResourceWithId(DummyEntityA.class, RESOURCE_ID, TENANT_ID);
        when(repository.findByIdAndTenantId(RESOURCE_ID, TENANT_ID)).thenReturn(Optional.of(resource));
        var actualResource = resourceApiService.getResource(RESOURCE_ID, TENANT_ID);
        assertThat(actualResource).isEqualTo(resource);
    }

    @Test
    void getResource_withoutFilterAndResourceDoesNotExist_resourceNotFoundExceptionThrown() {
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> resourceApiService.getResource(RESOURCE_ID, TENANT_ID));
    }

    @Test
    void getResource_withFilterAndResourceExists_resourceReturned() {
        T resource = ResourceTestUtil.getResourceWithId(DummyEntityA.class, RESOURCE_ID, TENANT_ID);
        when(repository.findByIdAndTenantId(RESOURCE_ID, TENANT_ID, "filter")).thenReturn(Optional.of(resource));
        var actualResource = resourceApiService.getResource(RESOURCE_ID, "filter", TENANT_ID);
        assertThat(actualResource).isEqualTo(resource);
    }

    @Test
    void getResource_withFilterAndResourceDoesNotExist_resourceNotFoundExceptionThrown() {
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> resourceApiService.getResource(RESOURCE_ID, "filter", TENANT_ID));
    }

    // endregion


    // region create

    @Test
    void createResource_resourceCreated() {

        when(transactionManager.getTransaction(new DefaultTransactionDefinition())).thenReturn(transactionStatus);

        T payload = ResourceTestUtil.getResource(DummyEntityA.class);
        T createdResource = ResourceTestUtil.getResourceWithId(DummyEntityA.class, RESOURCE_ID, TENANT_ID);
        when(repository.saveAndFlush(payload)).thenReturn(createdResource);

        var actualResource = resourceApiService.createResource(payload, TENANT_ID);

        verify(transactionManager).commit(transactionStatus);
        verify(transactionManager, never()).rollback(transactionStatus);
        assertThat(actualResource).isEqualTo(createdResource);
    }

    // endregion



    // region delete

    @Test
    void deleteResource_verifyServiceInteractions() {

        when(transactionManager.getTransaction(new DefaultTransactionDefinition())).thenReturn(transactionStatus);
        assertThatNoException().isThrownBy(() -> resourceApiService.deleteResource(RESOURCE_ID, TENANT_ID));

        verify(repository).deleteByIdAndTenantId(RESOURCE_ID, TENANT_ID);
        verify(transactionManager).commit(transactionStatus);
        verify(transactionManager, never()).rollback(transactionStatus);
    }

    // endregion


    // region update
    @Test
    void updateResource_existingResourceIsUpdatedWithPayloadDataAndIdAndTenantIdRemainUnchanged() {

        when(transactionManager.getTransaction(new DefaultTransactionDefinition())).thenReturn(transactionStatus);
        when(entityUtils.getIdFieldName()).thenReturn(ResourceTestUtil.ID_FIELD_NAME);

        T existingResource = ResourceTestUtil.getResourceWithId(DummyEntityA.class, RESOURCE_ID, TENANT_ID);
        T payload = ResourceTestUtil.getResource(DummyEntityA.class);
        var dummyAPayload = (DummyEntityA) payload;
        dummyAPayload.setProfileId(1L);

        resourceApiService.updateResource(existingResource, payload);

        var existingDummyAResource = (DummyEntityA) existingResource;
        assertThat(existingResource.getId()).isEqualTo(RESOURCE_ID);
        assertThat(existingResource.getTenantId()).isEqualTo(TENANT_ID);
        assertThat(existingDummyAResource.getProfileId()).isEqualTo(dummyAPayload.getProfileId());
        verify(transactionManager).commit(transactionStatus);
        verify(transactionManager, never()).rollback(transactionStatus);
    }

    // endregion



    // region getRelated
    @Test
    void getRelatedResources_verifyRepositoryInteraction() {

        doReturn(RELATED_RESOURCE_CLASS).when(entityUtils).getRelatedType(RELATED_RESOURCE_NAME);
        resourceApiService.getRelatedResources(RESOURCE_ID, RELATED_RESOURCE_NAME, TENANT_ID, null, null);

        verify(repository).findAllByIdAndRelationAndTenantId(
                RESOURCE_ID, RELATED_RESOURCE_NAME, RELATED_RESOURCE_CLASS, TENANT_ID, null, null);
    }

    // endregion



    // region deleteRelated

    @Test
    void deleteRelatedResources_relatedResourcesRemoved() {

        List<Serializable> relatedIds = List.of(UUID.randomUUID(), UUID.randomUUID());

        //mock succeeding related Ids tenant validation
        doReturn(RELATED_RESOURCE_CLASS).when(entityUtils).getRelatedType(RELATED_RESOURCE_NAME);
        when(repository.countAllByRelationAndTenantId(RELATED_RESOURCE_CLASS, relatedIds, TENANT_ID))
                .thenReturn(Long.valueOf(relatedIds.size()));

        //mock getting the related entities and references to delete
        T originalEntity = ResourceTestUtil.getResourceWithId(DummyEntityA.class, RESOURCE_ID, TENANT_ID);
        var relatedEntities = relatedIds.stream()
                .map(id -> ResourceTestUtil.getResourceWithId(DummyEntityB.class, (UUID) id, TENANT_ID))
                .collect(Collectors.toList());
        when(entityUtils.getRelatedEntities(originalEntity, RELATED_RESOURCE_NAME)).thenReturn(relatedEntities);
        when(entityUtils.getEntityReference(RELATED_RESOURCE_NAME, relatedIds.get(0))).thenReturn(relatedEntities.get(0));
        when(entityUtils.getEntityReference(RELATED_RESOURCE_NAME, relatedIds.get(1))).thenReturn(relatedEntities.get(1));

        when(transactionManager.getTransaction(new DefaultTransactionDefinition())).thenReturn(transactionStatus);

        resourceApiService.deleteRelatedResources(originalEntity, RELATED_RESOURCE_NAME, relatedIds, TENANT_ID);

        verify(repository).saveAndFlush(originalEntity);
        verify(transactionManager).commit(transactionStatus);
        verify(transactionManager, never()).rollback(transactionStatus);
    }

    @Test
    void deleteRelatedResources_relatedIdsAreNotValidForTenant_resourceNotFoundExceptionThrown() {

        List<Serializable> relatedIds = List.of(UUID.randomUUID(), UUID.randomUUID());

        //mock failing related Ids tenant validation
        doReturn(RELATED_RESOURCE_CLASS).when(entityUtils).getRelatedType(RELATED_RESOURCE_NAME);
        when(repository.countAllByRelationAndTenantId(RELATED_RESOURCE_CLASS, relatedIds, TENANT_ID))
                .thenReturn(0L);

        T originalEntity = ResourceTestUtil.getResourceWithId(DummyEntityA.class, RESOURCE_ID, TENANT_ID);
        assertThatExceptionOfType(ResourceNotFoundException.class).isThrownBy(() ->
                resourceApiService.deleteRelatedResources(originalEntity, RELATED_RESOURCE_NAME, relatedIds, TENANT_ID))
                .withMessage(relatedResourcesMessage(relatedIds));
    }

    @Test
    void deleteRelatedResources_notAllRelatedIdsToDeleteAreRelated_resourceNotFoundExceptionThrown() {

        var relatedEntityId = UUID.randomUUID();
        var unrelatedEntityId = UUID.randomUUID();
        List<Serializable> idsToDelete = List.of(relatedEntityId, unrelatedEntityId);

        //mock succeeding related Ids tenant validation
        doReturn(RELATED_RESOURCE_CLASS).when(entityUtils).getRelatedType(RELATED_RESOURCE_NAME);
        when(repository.countAllByRelationAndTenantId(RELATED_RESOURCE_CLASS, idsToDelete, TENANT_ID))
                .thenReturn(Long.valueOf(idsToDelete.size()));

        //mock getting the related entities to delete except those that are not related for the IDs passed in
        T originalEntity = ResourceTestUtil.getResourceWithId(DummyEntityA.class, RESOURCE_ID, TENANT_ID);
        var relatedEntities = new ArrayList<>();
        relatedEntities.add(ResourceTestUtil.getResourceWithId(DummyEntityB.class, relatedEntityId, TENANT_ID));
        when(entityUtils.getRelatedEntities(originalEntity, RELATED_RESOURCE_NAME)).thenReturn(relatedEntities);

        //mock getting references for ids passed in
        when(entityUtils.getEntityReference(RELATED_RESOURCE_NAME, relatedEntityId)).thenReturn(relatedEntities.get(0));
        when(entityUtils.getEntityReference(RELATED_RESOURCE_NAME, unrelatedEntityId)).thenReturn(
                ResourceTestUtil.getResourceWithId(DummyEntityB.class, unrelatedEntityId, TENANT_ID));

        when(transactionManager.getTransaction(new DefaultTransactionDefinition())).thenReturn(transactionStatus);

        assertThatExceptionOfType(ResourceNotFoundException.class).isThrownBy(() ->
                        resourceApiService.deleteRelatedResources(originalEntity, RELATED_RESOURCE_NAME, idsToDelete, TENANT_ID))
                .withMessage(deletableRelatedResourcesMessage(RELATED_RESOURCE_CLASS, List.of(unrelatedEntityId)));

        verify(repository, never()).saveAndFlush(originalEntity);
        verify(transactionManager, never()).commit(transactionStatus);
    }

    // endregion



    // region addRelated

    @Test
    void addRelatedResources_relatedResourcesAdded() {

        List<Serializable> relatedIds = List.of(UUID.randomUUID(), UUID.randomUUID());

        //mock succeeding related Ids tenant validation
        doReturn(RELATED_RESOURCE_CLASS).when(entityUtils).getRelatedType(RELATED_RESOURCE_NAME);
        when(repository.countAllByRelationAndTenantId(RELATED_RESOURCE_CLASS, relatedIds, TENANT_ID))
                .thenReturn(Long.valueOf(relatedIds.size()));

        //mock getting the related entities and references to add
        T originalEntity = ResourceTestUtil.getResourceWithId(DummyEntityA.class, RESOURCE_ID, TENANT_ID);
        var relatedEntities = relatedIds.stream()
                .map(id -> ResourceTestUtil.getResourceWithId(DummyEntityB.class, (UUID) id, TENANT_ID))
                .collect(Collectors.toList());
        when(entityUtils.getRelatedEntities(originalEntity, RELATED_RESOURCE_NAME)).thenReturn(relatedEntities);
        when(entityUtils.getEntityReference(RELATED_RESOURCE_NAME, relatedIds.get(0))).thenReturn(relatedEntities.get(0));
        when(entityUtils.getEntityReference(RELATED_RESOURCE_NAME, relatedIds.get(1))).thenReturn(relatedEntities.get(1));

        when(transactionManager.getTransaction(new DefaultTransactionDefinition())).thenReturn(transactionStatus);

        resourceApiService.addRelatedResources(originalEntity, RELATED_RESOURCE_NAME, relatedIds, TENANT_ID);

        verify(repository).saveAndFlush(originalEntity);
        verify(transactionManager).commit(transactionStatus);
        verify(transactionManager, never()).rollback(transactionStatus);
    }

    @Test
    void addRelatedResources_relatedIdsAreNotValidForTenant_resourceNotFoundExceptionThrown() {

        List<Serializable> relatedIds = List.of(UUID.randomUUID(), UUID.randomUUID());

        //mock failing related Ids tenant validation
        doReturn(RELATED_RESOURCE_CLASS).when(entityUtils).getRelatedType(RELATED_RESOURCE_NAME);
        when(repository.countAllByRelationAndTenantId(RELATED_RESOURCE_CLASS, relatedIds, TENANT_ID))
                .thenReturn(0L);

        T originalEntity = ResourceTestUtil.getResourceWithId(DummyEntityA.class, RESOURCE_ID, TENANT_ID);
        assertThatExceptionOfType(ResourceNotFoundException.class).isThrownBy(() ->
                        resourceApiService.addRelatedResources(originalEntity, RELATED_RESOURCE_NAME, relatedIds, TENANT_ID))
                .withMessage(relatedResourcesMessage(relatedIds));
    }

    // endregion

}
