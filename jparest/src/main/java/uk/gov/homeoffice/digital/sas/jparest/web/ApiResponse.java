package uk.gov.homeoffice.digital.sas.jparest.web;

import java.net.URL;
import java.util.List;

/**
 * Used to return responses from the {@link ResourceApiController}
 */
public class ApiResponse<T> {

    public class Metadata {
        private URL next;

        public URL getNext() {
            return this.next;
        }

        public void setNext(URL next) {
            this.next = next;
        }

    }

    private Metadata meta = new Metadata();
    private List<T> items;

    public ApiResponse(List<T> items) {
        this.items = items;
    }

    public Metadata getMeta() {
        return this.meta;
    }

    public void setMeta(Metadata meta) {
        this.meta = meta;
    }

    public List<T> getItems() {
        return this.items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }


}