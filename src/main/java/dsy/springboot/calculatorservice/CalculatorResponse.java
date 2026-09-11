package dsy.springboot.calculatorservice;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The API answers with {"type":"VALUE","value":"3"}. Jackson maps that onto this record
 * automatically, so no manual JSON parsing is needed here (unlike in the intro topic).
 */
public record CalculatorResponse(String type, String value)
{
  /** @JsonIgnore because Jackson would otherwise read "isSuccess" as a field named success. */
  @JsonIgnore
  public boolean isSuccess()
  {
    return "VALUE".equals(type);
  }
}
