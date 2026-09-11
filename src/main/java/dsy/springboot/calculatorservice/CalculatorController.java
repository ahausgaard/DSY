package dsy.springboot.calculatorservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** GET /calculate?expression=2 + 3 * 4 -> {"type":"VALUE","value":"20"} */
@RestController
public class CalculatorController
{
  private static final Logger log = LoggerFactory.getLogger(CalculatorController.class);

  private final CalculatorClient client;

  public CalculatorController(CalculatorClient client)
  {
    this.client = client;
  }

  @GetMapping("/calculate")
  public ResponseEntity<CalculatorResponse> calculate(@RequestParam String expression)
  {
    CalculatorResponse response = client.evaluate(expression);

    if (response.isSuccess())
    {
      log.info("{} = {}", expression, response.value());
      return ResponseEntity.ok(response);
    }

    // Errors from the API, such as division by zero, are passed on as 400
    log.warn("{} -> {}: {}", expression, response.type(), response.value());
    return ResponseEntity.badRequest().body(response);
  }

  /** Bad expressions are answered with 400 instead of a stack trace. */
  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public CalculatorResponse invalidExpression(IllegalArgumentException e)
  {
    log.warn("Invalid expression: {}", e.getMessage());
    return new CalculatorResponse("INPUT_ERROR", e.getMessage());
  }
}
