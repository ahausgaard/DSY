package dsy.springboot.calculatorservice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests the web layer only. The real calculator API is never called - the client is a mock,
 * so these tests also run without internet.
 */
@WebMvcTest(CalculatorController.class)
class CalculatorControllerTest
{
  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private CalculatorClient client;

  @Test
  @DisplayName("A result is returned as JSON")
  void returnsResult() throws Exception
  {
    given(client.evaluate("2 + 3")).willReturn(new CalculatorResponse("VALUE", "5"));

    mockMvc.perform(get("/calculate").param("expression", "2 + 3"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.type").value("VALUE"))
        .andExpect(jsonPath("$.value").value("5"));
  }

  @Test
  @DisplayName("An error from the API, such as division by zero, becomes 400")
  void passesApiErrorOnAsBadRequest() throws Exception
  {
    given(client.evaluate("10 / 0")).willReturn(new CalculatorResponse("ERROR", "Division by zero"));

    mockMvc.perform(get("/calculate").param("expression", "10 / 0"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.type").value("ERROR"))
        .andExpect(jsonPath("$.value").value("Division by zero"));
  }

  @Test
  @DisplayName("An invalid expression gives 400 and an explanation")
  void returnsBadRequestForInvalidExpression() throws Exception
  {
    given(client.evaluate(any())).willThrow(new IllegalArgumentException("'abc' is not a number"));

    mockMvc.perform(get("/calculate").param("expression", "abc + 1"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.type").value("INPUT_ERROR"))
        .andExpect(jsonPath("$.value").value("'abc' is not a number"));
  }
}
