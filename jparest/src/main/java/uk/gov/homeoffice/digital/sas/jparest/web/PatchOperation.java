package uk.gov.homeoffice.digital.sas.jparest.web;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class PatchOperation<T> {
  public String op;
  public String path;
  public T value;
}
