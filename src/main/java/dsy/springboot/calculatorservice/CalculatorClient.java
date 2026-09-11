package dsy.springboot.calculatorservice;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * HTTP client for the Simple Calculator API, written with Spring's RestClient.
 *
 * Same rules as the intro topic: one operator goes to /calculate, several go to /sequence
 * and are evaluated left to right.
 */
@Component
public class CalculatorClient
{
  private static final Logger log = LoggerFactory.getLogger(CalculatorClient.class);

  private static final Map<String, String> OPS = Map.of("+", "plus", "-", "minus", "*", "times", "/", "divide");

  private final RestClient restClient;

  public CalculatorClient(RestClient.Builder builder, @Value("${calculator.api.base-url}") String baseUrl)
  {
    this.restClient = builder.baseUrl(baseUrl).build();
  }

  /** One operation in a sequence request, e.g. {"operation":"times","right":4} */
  record Operation(String operation, BigDecimal right) {}

  record SequenceRequest(List<Operation> operations) {}

  public CalculatorResponse evaluate(String expression)
  {
    String[] tokens = expression.trim().split("\\s+");
    validate(tokens);

    return tokens.length == 3
        ? calculate(tokens[0], tokens[1], tokens[2])
        : sequence(tokens);
  }

  /** Operands on the even positions, operators on the odd ones. */
  static void validate(String[] tokens)
  {
    if (tokens.length < 3 || tokens.length % 2 == 0)
      throw new IllegalArgumentException("expected: number operator number [operator number ...]");

    for (int i = 0; i < tokens.length; i++)
    {
      if (i % 2 == 0 && !tokens[i].matches("-?\\d+(\\.\\d+)?"))
        throw new IllegalArgumentException("'" + tokens[i] + "' is not a number");
      else if (i % 2 == 1 && !OPS.containsKey(tokens[i]))
        throw new IllegalArgumentException("unknown operator '" + tokens[i] + "', use + - * /");
    }
  }

  private CalculatorResponse calculate(String left, String operator, String right)
  {
    log.debug("GET /calculate {} {} {}", left, operator, right);

    return restClient.get()
        .uri(uri -> uri.path("/calculate")
            .queryParam("operation", OPS.get(operator))
            .queryParam("left", left)
            .queryParam("right", right)
            .build())
        .retrieve()
        .onStatus(status -> status.isError(), (request, response) -> {}) // the API describes its errors in the body
        .body(CalculatorResponse.class);
  }

  private CalculatorResponse sequence(String[] tokens)
  {
    List<Operation> operations = new ArrayList<>();
    operations.add(new Operation("plus", new BigDecimal(tokens[0]))); // 0 + first operand

    for (int i = 1; i < tokens.length; i += 2)
      operations.add(new Operation(OPS.get(tokens[i]), new BigDecimal(tokens[i + 1])));

    log.debug("POST /sequence {}", operations);

    return restClient.post()
        .uri("/sequence")
        .body(new SequenceRequest(operations))
        .retrieve()
        .onStatus(status -> status.isError(), (request, response) -> {})
        .body(CalculatorResponse.class);
  }
}
