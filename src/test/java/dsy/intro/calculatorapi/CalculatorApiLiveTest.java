package dsy.intro.calculatorapi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import dsy.intro.calculatorapi.CalculatorApiCli.Result;

/**
 * Tests that call the real API, so they need an internet connection.
 * Skip them with: mvn test -DexcludedGroups=api
 */
@Tag("api")
class CalculatorApiLiveTest
{
  @Test
  @DisplayName("2 + 3 is calculated by the calculate endpoint")
  void calculatesSingleOperation() throws Exception
  {
    assertEquals(new Result("VALUE", "5"), CalculatorApiCli.evaluate(new String[]{"2", "+", "3"}));
  }

  @Test
  @DisplayName("2 + 3 * 4 is calculated left to right by the sequence endpoint")
  void calculatesSequenceLeftToRight() throws Exception
  {
    assertEquals(new Result("VALUE", "20"), CalculatorApiCli.evaluate(new String[]{"2", "+", "3", "*", "4"}));
  }

  @Test
  @DisplayName("The API reports division by zero itself")
  void reportsDivisionByZero() throws Exception
  {
    assertEquals(new Result("ERROR", "Division by zero"), CalculatorApiCli.evaluate(new String[]{"10", "/", "0"}));
  }
}
