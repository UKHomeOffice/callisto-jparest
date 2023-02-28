package uk.gov.homeoffice.digital.sas.jparest.web;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PatchOperation<T> {
  public String op;
  public String path;
  public T value;
}
