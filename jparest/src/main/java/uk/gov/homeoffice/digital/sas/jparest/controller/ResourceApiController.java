package uk.gov.homeoffice.digital.sas.jparest.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import org.springframework.data.domain.Pageable;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.TenantIdMismatchException;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.UnknownResourcePropertyException;
import uk.gov.homeoffice.digital.sas.jparest.models.BaseEntity;
import uk.gov.homeoffice.digital.sas.jparest.service.ResourceApiService;
import uk.gov.homeoffice.digital.sas.jparest.web.ApiResponse;

/**
 * Spring MVC controller that exposes JPA entities
 * and has common functionality for paging, sorting
 * and filtering resources.
 *
 * <p>Resources for ManyToMany relationships can also be queried.
 */
@ResponseBody
@Controller
public class ResourceApiController<T extends BaseEntity> {

  @Getter
  private final Class<T> entityType;
  private final ResourceApiService<T> service;
  private final ObjectMapper objectMapper;


  @SuppressWarnings("unchecked")
  public ResourceApiController(Class<T> entityType, 
                               ResourceApiService<T> service,
                               ObjectMapper objectMapper) {
    this.entityType = entityType;
    this.service = service;
    this.objectMapper = objectMapper;
  }

  public ApiResponse<T> list(
      @RequestParam UUID tenantId, Pageable pageable, SpelExpression filter) {
    return new ApiResponse<>(service.getAllResources(tenantId, pageable, filter));
  }

  public ApiResponse<T> get(@RequestParam UUID tenantId, @PathVariable UUID id) {
    return new ApiResponse<>(service.getResource(tenantId, id));
  }

  public ApiResponse<T> create(@RequestParam UUID tenantId, @RequestBody String body)
      throws JsonProcessingException {

    T entity = readEntityFromPayload(body);
    validateAndSetTenantIdPayloadMatch(tenantId, entity);

    if (Objects.nonNull(entity.getId())) {
      throw new IllegalArgumentException(
        "A resource id should not be provided when creating a new resource.");
    }
    return new ApiResponse<>(service.createResource(entity));
  }

  public void delete(@RequestParam UUID tenantId, @PathVariable UUID id) {
    service.deleteResource(tenantId, id);
  }

  public ApiResponse<T> update(@RequestParam UUID tenantId,
                               @PathVariable UUID id,
                               @RequestBody String body) throws JsonProcessingException {

    T entity = readEntityFromPayload(body);
    validateAndSetTenantIdPayloadMatch(tenantId, entity);

    var payloadEntityId = service.getEntityId(entity);
    if (payloadEntityId != null && !id.equals(payloadEntityId)) {
      throw new IllegalArgumentException(
        "The supplied payload resource id value must match the url id path parameter value");
    }
    entity.setId(id);
    return new ApiResponse<>(service.updateResource(entity));
  }

  public ApiResponse<T> batchUpdate(@RequestParam UUID tenantId,
                               @PathVariable UUID id,
                               @RequestBody String body) throws JsonProcessingException {

    List<T> entities = readEntitiesFromPayload(body);
    validateAndSetTenantIdPayloadMatch(tenantId, entities.get(0));

    var payloadEntityId = service.getEntityId(entities.get(0));
    if (payloadEntityId != null && !id.equals(payloadEntityId)) {
      throw new IllegalArgumentException(
          "The supplied payload resource id value must match the url id path parameter value");
    }
    entities.get(0).setId(id);

    return new ApiResponse<>(service.updateResources(entities));
  }

  @SuppressWarnings("squid:S1452") // Generic wildcard types should not be used in return parameters
  public ApiResponse<?> getRelated(
      @RequestParam UUID tenantId,
      @PathVariable UUID id,
      @PathVariable String relation, Pageable pageable, SpelExpression filter) {

    return new ApiResponse<>(service.getRelatedResources(tenantId, id, relation, pageable, filter));

  }

  public void deleteRelated(
      @RequestParam UUID tenantId,
      @PathVariable UUID id,
      @PathVariable String relation,
      @PathVariable List<UUID> relatedIds)
      throws IllegalArgumentException {
    service.deleteRelatedResources(tenantId, id, relation, relatedIds);
  }

  public void addRelated(
        @RequestParam UUID tenantId,
        @PathVariable UUID id,
        @PathVariable String relation,
        @PathVariable List<UUID> relatedIds)
      throws IllegalArgumentException {
    service.addRelatedResources(tenantId, id, relation, relatedIds);
  }


  private T readEntityFromPayload(String body) throws JsonProcessingException {
    try {
      return objectMapper.readValue(body, entityType);
    } catch (UnrecognizedPropertyException ex) {
      throw new UnknownResourcePropertyException(
        ex.getPropertyName(), ex.getReferringClass().getSimpleName());
    }
  }

  private List<T> readEntitiesFromPayload(String body) throws JsonProcessingException {
    try {
      return objectMapper.readerForListOf(entityType).readValue(body);
    } catch (UnrecognizedPropertyException ex) {
      throw new UnknownResourcePropertyException(
          ex.getPropertyName(), ex.getReferringClass().getSimpleName());
    }
  }

  private void validateAndSetTenantIdPayloadMatch(UUID requestTenantId, T entity) {

    var entityTenantId = entity.getTenantId();
    if (entityTenantId != null && !requestTenantId.equals(entityTenantId)) {
      throw new TenantIdMismatchException();

    } else if (entityTenantId == null) {
      entity.setTenantId(requestTenantId);
    }
  }


}
