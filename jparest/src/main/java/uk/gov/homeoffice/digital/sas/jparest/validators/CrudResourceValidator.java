package uk.gov.homeoffice.digital.sas.jparest.validators;

import org.springframework.stereotype.Service;
import uk.gov.homeoffice.digital.sas.jparest.exceptions.TenantIdMismatchException;

import java.util.UUID;

@Service
public class CrudResourceValidator {



    public void validateTenantIdPayloadMatch(UUID requestTenantId, UUID payloadTenantId) {
        if (payloadTenantId != null && !requestTenantId.equals(payloadTenantId))
            throw new TenantIdMismatchException("The supplied payload tenant id value must match the url tenant id query parameter value");
    }


    public void validateUrlIdPayloadMatch(UUID pathEntityId, UUID payloadEntityId) {
        if (payloadEntityId != null && !pathEntityId.equals(payloadEntityId))
            throw new IllegalArgumentException("The supplied payload resource id value must match the url id path parameter value");
    }


}
