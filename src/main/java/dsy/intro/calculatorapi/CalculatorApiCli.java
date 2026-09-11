package dsy.intro.calculatorapi;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Command line client for the Simple Calculator API:
 * https://testpages.eviltester.com/apps/calculator-api/
 *
 * One operator  -> GET  /calculate
 * More than one -> POST /sequence   (evaluated left to right, no operator precedence)
 *
 * All output goes through Log4j2 instead of System.out, see src/main/resources/log4j2.xml.
 */
public class CalculatorApiCli
{
  private static final Logger LOG = LogManager.getLogger(CalculatorApiCli.class);

  static final String API = "https://testpages.eviltester.com/apps/api/calculator/";
  static final HttpClient CLIENT = HttpClient.newHttpClient();
  static final Map<String, String> OPS = Map.of("+", "plus", "-", "minus", "*", "times", "/", "divide");
  // Pulls "type" and "value" out of e.g. {"type":"VALUE","value":"3"}
  static final Pattern RESULT = Pattern.compile("\"type\"\\s*:\\s*\"([^\"]*)\".*?\"value\"\\s*:\\s*\"?([^\",}]*)");

  /** One response from the API, e.g. VALUE/3 or ERROR/Division by zero. */
  record Result(String type, String value) {}

  public static void main(String[] args)
  {
    Scanner input = new Scanner(System.in);
    LOG.info("Calculator API client - example: 2 + 3 * 4 (spaces required, type 'exit' to quit)");

    while (true)
    {
      // The only direct console write: a logger always ends its line, so it cannot draw a prompt
      System.out.print("> ");
      if (!input.hasNextLine()) break;

      String line = input.nextLine().trim();
      if (line.isEmpty()) continue;
      if (line.equalsIgnoreCase("exit")) break;

      try
      {
        Result result = evaluate(line.split("\\s+"));

        if (result.type().equals("VALUE"))
          LOG.info("{} = {}", line, result.value());
        else
          LOG.warn("{} -> {}: {}", line, result.type(), result.value());
      }
      catch (IllegalArgumentException e)
      {
        LOG.warn("Invalid input '{}': {}", line, e.getMessage());
      }
      catch (Exception e)
      {
        LOG.error("Could not reach the calculator API", e);
      }
    }

    LOG.info("Goodbye");
  }

  /** Validates the expression tokens and sends them to the right endpoint. */
  static Result evaluate(String[] tokens) throws Exception
  {
    validate(tokens);
    return tokens.length == 3 ? calculate(tokens[0], tokens[1], tokens[2]) : sequence(tokens);
  }

  /** Operands on the even positions, operators on the odd ones. Throws if the expression is malformed. */
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

  /** GET /calculate?operation=plus&left=1&right=2 */
  static Result calculate(String left, String operator, String right) throws Exception
  {
    String url = calculateUrl(left, operator, right);

    LOG.debug("GET {}", url);
    return send(HttpRequest.newBuilder(URI.create(url)).GET());
  }

  static String calculateUrl(String left, String operator, String right)
  {
    return API + "calculate?operation=" + OPS.get(operator)
        + "&left=" + encode(left)
        + "&right=" + encode(right);
  }

  /** POST /sequence with {"operations":[{"operation":"plus","right":2}, ...]} */
  static Result sequence(String[] tokens) throws Exception
  {
    String json = sequenceJson(tokens);

    LOG.debug("POST {}sequence {}", API, json);
    return send(HttpRequest.newBuilder(URI.create(API + "sequence"))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(json)));
  }

  static String sequenceJson(String[] tokens)
  {
    StringBuilder json = new StringBuilder("{\"operations\":[");
    json.append("{\"operation\":\"plus\",\"right\":").append(tokens[0]).append("}"); // 0 + first operand

    for (int i = 1; i < tokens.length; i += 2)
      json.append(",{\"operation\":\"").append(OPS.get(tokens[i]))
          .append("\",\"right\":").append(tokens[i + 1]).append("}");

    return json.append("]}").toString();
  }

  /** Sends the request and reads type and value out of the JSON response. */
  static Result send(HttpRequest.Builder request) throws Exception
  {
    HttpResponse<String> response = CLIENT.send(request.build(), HttpResponse.BodyHandlers.ofString());
    LOG.debug("HTTP {} {}", response.statusCode(), response.body());

    return parse(response.body(), response.statusCode());
  }

  /** {"type":"VALUE","value":"3"} -> Result[VALUE, 3] */
  static Result parse(String body, int statusCode)
  {
    Matcher matcher = RESULT.matcher(body);

    if (!matcher.find())
      return new Result("UNEXPECTED_RESPONSE", "HTTP " + statusCode + " " + body);

    return new Result(matcher.group(1), matcher.group(2));
  }

  static String encode(String value)
  {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }
}
