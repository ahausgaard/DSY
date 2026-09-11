package dsy.intro.calculatorapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dsy.intro.calculatorapi.CalculatorApiCli.Result;

/** Tests that need no network: input validation, request building and response parsing. */
class CalculatorApiCliTest
{
  @Test
  @DisplayName("A valid expression passes validation")
  void acceptsValidExpression()
  {
    CalculatorApiCli.validate(new String[]{"2", "+", "3"});
    CalculatorApiCli.validate(new String[]{"-2.5", "*", "4", "/", "2"});
  }

  @Test
  @DisplayName("An operand that is not a number is rejected")
  void rejectsNonNumber()
  {
    IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
        () -> CalculatorApiCli.validate(new String[]{"abc", "+", "1"}));

    assertEquals("'abc' is not a number", e.getMessage());
  }

  @Test
  @DisplayName("An unknown operator is rejected")
  void rejectsUnknownOperator()
  {
    assertThrows(IllegalArgumentException.class,
        () -> CalculatorApiCli.validate(new String[]{"1", "%", "2"}));
  }

  @Test
  @DisplayName("An incomplete expression is rejected")
  void rejectsIncompleteExpression()
  {
    assertThrows(IllegalArgumentException.class,
        () -> CalculatorApiCli.validate(new String[]{"1", "+"}));
  }

  @Test
  @DisplayName("One operator becomes a calculate URL")
  void buildsCalculateUrl()
  {
    assertEquals(CalculatorApiCli.API + "calculate?operation=minus&left=7&right=2",
        CalculatorApiCli.calculateUrl("7", "-", "2"));
  }

  @Test
  @DisplayName("Several operators become a sequence body, starting from 0 plus the first operand")
  void buildsSequenceJson()
  {
    assertEquals("{\"operations\":["
            + "{\"operation\":\"plus\",\"right\":2},"
            + "{\"operation\":\"plus\",\"right\":3},"
            + "{\"operation\":\"times\",\"right\":4}]}",
        CalculatorApiCli.sequenceJson(new String[]{"2", "+", "3", "*", "4"}));
  }

  @Test
  @DisplayName("A success response is parsed into type and value")
  void parsesValue()
  {
    assertEquals(new Result("VALUE", "20"),
        CalculatorApiCli.parse("{\"type\":\"VALUE\",\"value\":\"20\"}", 200));
  }

  @Test
  @DisplayName("An error response is parsed into type and value")
  void parsesError()
  {
    assertEquals(new Result("ERROR", "Division by zero"),
        CalculatorApiCli.parse("{\"type\":\"ERROR\",\"value\":\"Division by zero\"}", 400));
  }

  @Test
  @DisplayName("Anything the regex does not recognise is reported instead of crashing")
  void parsesUnexpectedResponse()
  {
    assertEquals(new Result("UNEXPECTED_RESPONSE", "HTTP 500 <html>oops</html>"),
        CalculatorApiCli.parse("<html>oops</html>", 500));
  }
}
