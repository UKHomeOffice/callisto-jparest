package uk.gov.homeoffice.digital.sas.jparest.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import lombok.Getter;
import org.springframework.data.domain.Pageable;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.UnknownResourcePropertyException;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;
import uk.gov.homeoffice.digital.sas.jparest.service.ResourceApiService;
import uk.gov.homeoffice.digital.sas.jparest.utils.WebDataBinderFactory;
import uk.gov.homeoffice.digital.sas.jparest.validators.CrudResourceValidator;
import uk.gov.homeoffice.digital.sas.jparest.validators.EntityConstraintValidator;
import uk.gov.homeoffice.digital.sas.jparest.web.ApiResponse;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Spring MVC controller that exposes JPA entities through CRUD operations.
 */
@ResponseBody
public class ResourceApiController<T extends BaseEntity, U> {

    @Getter
    private final Class<T> entityType;
    private final ResourceApiService<T> resourceApiService;
    private final EntityConstraintValidator entityConstraintValidator;
    private final CrudResourceValidator crudResourceValidator;

    private static final WebDataBinder binder = WebDataBinderFactory.getWebDataBinder();


    public ResourceApiController(Class<T> entityType,
                                 ResourceApiService<T> resourceApiService,
                                 EntityConstraintValidator entityConstraintValidator,
                                 CrudResourceValidator crudResourceValidator) {

        this.entityType = entityType;
        this.resourceApiService = resourceApiService;
        this.entityConstraintValidator = entityConstraintValidator;
        this.crudResourceValidator = crudResourceValidator;
    }



    private static @NonNull Serializable getIdentifier(Object identifier) {
        Serializable result = binder.convertIfNecessary(identifier, UUID.class);
        if (result == null) throw new IllegalArgumentException("identifier must not be null");
        return result;
    }

    private static @NonNull List<Serializable> getIdentifiers(Object[] identifiers) {
        return Arrays.stream(identifiers)
                .map(ResourceApiController::getIdentifier)
                .collect(Collectors.toList());
    }



    public ApiResponse<T> list(SpelExpression filter, Pageable pageable, @RequestParam UUID tenantId) {
        return new ApiResponse<>(resourceApiService.getAllResources(tenantId, filter, pageable));
    }


    public ApiResponse<T> get(@PathVariable U id, @RequestParam UUID tenantId) {
        return new ApiResponse<>(resourceApiService.getResource(getIdentifier(id), tenantId));
    }


    public ApiResponse<T> create(@RequestBody String body, @RequestParam UUID tenantId) throws JsonProcessingException {

        T payload = readPayload(body);
        crudResourceValidator.validateTenantIdPayloadMatch(tenantId, payload.getTenantId());
        entityConstraintValidator.validate(payload);
        return new ApiResponse<>(resourceApiService.createResource(payload, tenantId));
    }

    public void delete(@PathVariable U id, @RequestParam UUID tenantId) {
        resourceApiService.deleteResource(getIdentifier(id), tenantId);
    }

    public ApiResponse<T> update(@PathVariable U id,
                                 @RequestBody String body,
                                 @RequestParam UUID tenantId) throws JsonProcessingException {


        var payload = readPayload(body);
        var serializedId = getIdentifier(id);

        crudResourceValidator.validateTenantIdPayloadMatch(tenantId, payload.getTenantId());
        crudResourceValidator.validateUrlIdPayloadMatch((UUID) serializedId, payload.getId());
        entityConstraintValidator.validate(payload);

        var existingResource = resourceApiService.getResource(serializedId, tenantId);
        resourceApiService.updateResource(existingResource, payload);

        return new ApiResponse<>(existingResource);
    }

    @SuppressWarnings("rawtypes")
    public ApiResponse getRelated(@PathVariable U id,
                                  @PathVariable String relation,
                                  SpelExpression filter, Pageable pageable,
                                  @RequestParam UUID tenantId) {

        return new ApiResponse<>(
                resourceApiService.getRelatedResources(getIdentifier(id), relation, tenantId, filter, pageable));
    }



    public void deleteRelated(@PathVariable U id,
                              @PathVariable String relation,
                              @PathVariable Object[] relatedIds,
                              @RequestParam UUID tenantId) throws IllegalArgumentException {

        var originalEntity = resourceApiService.getResource(getIdentifier(id), relation, tenantId);

        var serializedRelatedIds = getIdentifiers(relatedIds);
        resourceApiService.deleteRelatedResources(originalEntity, relation, serializedRelatedIds, tenantId);
    }


    public void addRelated(@PathVariable U id,
                           @PathVariable String relation,
                           @PathVariable Object[] relatedIds,
                           @RequestParam UUID tenantId) throws IllegalArgumentException {


        var originalEntity = resourceApiService.getResource(getIdentifier(id), relation, tenantId);

        var serializedRelatedIds = getIdentifiers(relatedIds);
        resourceApiService.addRelatedResources(originalEntity, relation, serializedRelatedIds, tenantId);
    }


    private T readPayload(String body) throws JsonProcessingException {
        try {
            var objectMapper = new ObjectMapper();
            return objectMapper.readValue(body, entityType);
        } catch (UnrecognizedPropertyException ex) {
            throw new UnknownResourcePropertyException(ex.getPropertyName(), ex.getReferringClass().getSimpleName());
        }
    }

}
