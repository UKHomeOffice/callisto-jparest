package uk.gov.homeoffice.digital.sas.jparest.web;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PatchOperation<T> {
  private String op;
  private String path;
  private T value;
}
