package uk.gov.homeoffice.digital.sas.jparest.web;

import java.net.URL;
import java.util.List;
import lombok.Getter;
import uk.gov.homeoffice.digital.sas.jparest.controller.ResourceApiController;

/**
 * <p>Used to return responses from the.</p> {@link ResourceApiController}
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

  @Getter
  private Metadata meta = new Metadata();

  @Getter
  private List<T> items;

  public ApiResponse(List<T> items) {
    this.items = items;
  }


}