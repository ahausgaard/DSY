package dsy.springboot.calculatorservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Plain unit tests of the expression rules - no Spring context, no network. */
class CalculatorClientTest
{
  @Test
  @DisplayName("A valid expression passes validation")
  void acceptsValidExpression()
  {
    CalculatorClient.validate(new String[]{"2", "+", "3"});
    CalculatorClient.validate(new String[]{"-2.5", "*", "4", "/", "2"});
  }

  @Test
  @DisplayName("An operand that is not a number is rejected")
  void rejectsNonNumber()
  {
    IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
        () -> CalculatorClient.validate(new String[]{"abc", "+", "1"}));

    assertEquals("'abc' is not a number", e.getMessage());
  }

  @Test
  @DisplayName("An unknown operator is rejected")
  void rejectsUnknownOperator()
  {
    assertThrows(IllegalArgumentException.class,
        () -> CalculatorClient.validate(new String[]{"1", "%", "2"}));
  }
}
