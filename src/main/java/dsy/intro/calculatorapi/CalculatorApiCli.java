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

/**
 * Command line client for the Simple Calculator API:
 * https://testpages.eviltester.com/apps/calculator-api/
 *
 * One operator  -> GET  /calculate
 * More than one -> POST /sequence   (evaluated left to right, no operator precedence)
 */
public class CalculatorApiCli
{
  static final String API = "https://testpages.eviltester.com/apps/api/calculator/";
  static final HttpClient CLIENT = HttpClient.newHttpClient();
  static final Map<String, String> OPS = Map.of("+", "plus", "-", "minus", "*", "times", "/", "divide");
  // Pulls "type" and "value" out of e.g. {"type":"VALUE","value":"3"}
  static final Pattern RESULT = Pattern.compile("\"type\"\\s*:\\s*\"([^\"]*)\".*?\"value\"\\s*:\\s*\"?([^\",}]*)");

  public static void main(String[] args)
  {
    Scanner input = new Scanner(System.in);
    System.out.println("Calculator API client - example: 2 + 3 * 4 (spaces required, type 'exit' to quit)");

    while (true)
    {
      System.out.print("> ");
      if (!input.hasNextLine()) break;

      String line = input.nextLine().trim();
      if (line.isEmpty()) continue;
      if (line.equalsIgnoreCase("exit")) break;

      try
      {
        System.out.println(evaluate(line.split("\\s+")));
      }
      catch (Exception e)
      {
        System.out.println("Error: " + e.getMessage());
      }
    }
  }

  /** Validates the expression tokens and sends them to the right endpoint. */
  static String evaluate(String[] tokens) throws Exception
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

    return tokens.length == 3 ? calculate(tokens[0], tokens[1], tokens[2]) : sequence(tokens);
  }

  /** GET /calculate?operation=plus&left=1&right=2 */
  static String calculate(String left, String operator, String right) throws Exception
  {
    String url = API + "calculate?operation=" + OPS.get(operator)
        + "&left=" + encode(left)
        + "&right=" + encode(right);

    return send(HttpRequest.newBuilder(URI.create(url)).GET());
  }

  /** POST /sequence with {"operations":[{"operation":"plus","right":2}, ...]} */
  static String sequence(String[] tokens) throws Exception
  {
    StringBuilder json = new StringBuilder("{\"operations\":[");
    json.append("{\"operation\":\"plus\",\"right\":").append(tokens[0]).append("}"); // 0 + first operand

    for (int i = 1; i < tokens.length; i += 2)
      json.append(",{\"operation\":\"").append(OPS.get(tokens[i]))
          .append("\",\"right\":").append(tokens[i + 1]).append("}");

    json.append("]}");

    return send(HttpRequest.newBuilder(URI.create(API + "sequence"))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(json.toString())));
  }

  /** Sends the request and turns the JSON response into a line of text. */
  static String send(HttpRequest.Builder request) throws Exception
  {
    HttpResponse<String> response = CLIENT.send(request.build(), HttpResponse.BodyHandlers.ofString());
    Matcher result = RESULT.matcher(response.body());

    if (!result.find())
      return "Unexpected response (" + response.statusCode() + "): " + response.body();

    return result.group(1).equals("VALUE")
        ? "= " + result.group(2)
        : result.group(1) + ": " + result.group(2);
  }

  static String encode(String value)
  {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }
}
