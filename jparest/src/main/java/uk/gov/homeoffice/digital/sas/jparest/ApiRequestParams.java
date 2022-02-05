package uk.gov.homeoffice.digital.sas.jparest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class representing the arguments used by the {@link ResourceApiController}
 * for filtering, paging and sorting. 
 */
public class ApiRequestParams {
 
    private int page = 1;
    private int pageSize = 100;
    private String sort; 
    Map<String, String> requestParams = new HashMap<>();
    Set<Criteria> criteria = new HashSet<>();

    public Map<String,String> getRequestParams() {
        return this.requestParams;
    }

    public Set<Criteria> getCriteria() {
        return this.criteria;
    }
    
    public String getSort() {
        return this.sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public int getPage() {
        return this.page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return this.pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
