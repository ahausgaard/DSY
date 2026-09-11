# DSY

School work for DSY, collected in one Maven project. Every topic is a package, every
assignment is a subpackage with its own `main` method that prints to the console.

## Layout

```
src/main/java/dsy/
  intro/                       <- topic
    calculatorapi/             <- assignment
      CalculatorApiCli.java    <- has main(), run this one
  springboot/
    calculatorservice/
      CalculatorServiceApplication.java
src/test/java/dsy/
  intro/calculatorapi/         <- tests for that assignment
  springboot/calculatorservice/
```

## Running

Open the class with the `main` method and press the green arrow in IntelliJ, or from a terminal:

```
mvn compile
mvn dependency:build-classpath -Dmdep.outputFile=target/cp.txt
java -cp "target/classes;target/cp.txt content" dsy.intro.calculatorapi.CalculatorApiCli
```

(From the IDE it is just the green arrow - IntelliJ puts the dependencies on the classpath itself.)

## Spring Boot

The `springboot` topic is a web service, not a console program: it starts, keeps running and
waits for HTTP requests. Start `CalculatorServiceApplication` (green arrow) or `mvn spring-boot:run`,
then open <http://localhost:8080> and type the expression in the page there. Stop it again with
the red square in IntelliJ.

The same thing without the page:

```
curl "http://localhost:8080/calculate?expression=2%20%2B%203%20*%204"   -> {"type":"VALUE","value":"20"}
```

Bad input and API errors such as division by zero come back as HTTP 400 with the same JSON shape.

Note the `%2B`: in a URL a plus sign means "space", so an expression typed straight into the
browser address bar loses its plus operators. The page on <http://localhost:8080> avoids this by
encoding the expression with `encodeURIComponent`.

Spring Boot is added through its BOM (`spring-boot-dependencies` under `dependencyManagement`)
instead of the usual `spring-boot-starter-parent`, so the rest of the project is unaffected.
Three things the parent would otherwise have handled, and that are therefore set by hand in
`pom.xml`:

- `maven.compiler.parameters=true`, without which `@RequestParam` cannot see parameter names
- `<mainClass>` on `spring-boot-maven-plugin`, because the project has several main methods
- `spring-boot-starter-logging` (Logback) excluded in favour of `spring-boot-starter-log4j2`,
  so Spring logs through the same `log4j2.xml` as the rest of the project

The HTTP client is Spring's `RestClient`, and Jackson maps the JSON onto a record - the intro
topic does the same thing by hand with `HttpClient` and a regex.

## Tests

JUnit 5 (`junit-jupiter`), run with `mvn test` or from the IDE.

```
src/test/java/dsy/intro/calculatorapi/
  CalculatorApiCliTest.java    <- no network: validation, URL/JSON building, response parsing
  CalculatorApiLiveTest.java   <- calls the real API, tagged "api"
src/test/java/dsy/springboot/calculatorservice/
  CalculatorClientTest.java      <- plain unit tests of the expression rules
  CalculatorControllerTest.java  <- @WebMvcTest with a mocked client, no network
```

The live tests need an internet connection. Without one, skip them:

```
mvn test -DexcludedGroups=api
```

`maven-surefire-plugin` is pinned in `pom.xml` because the version Maven picks by default is
older than JUnit 5 and would silently run no tests.

## Logging

Log4j2 (`log4j-api` + `log4j-core`) is declared in `pom.xml`; IntelliJ downloads the jars when
the Maven project is reloaded. The configuration is `src/main/resources/log4j2.xml`, which Log4j
picks up automatically because it lies on the classpath, and it writes to two places:

| Destination | Level | Content |
|-------------|-------|---------|
| Console | INFO and up | Results and warnings |
| `logs/calculator.log` | DEBUG and up | The same, plus every request URL / JSON body and the raw HTTP response |

Levels used in the code: `info` for results, `warn` for bad input and API errors such as division
by zero, `error` with the stack trace when the API cannot be reached, `debug` for the HTTP traffic.

The log folder is git ignored.

## Adding a new topic or assignment

1. Create the package: `src/main/java/dsy/<topic>/<assignment>/`.
2. Put a class with `public static void main(String[] args)` in it.
3. Add a line to the table below.

Package names must be lowercase and cannot start with a digit, so use `intro`, `arrays`,
`databases` and so on rather than `1intro`.

Extra libraries go in the single `pom.xml` under `<dependencies>` and are then available
to every topic. There is nothing else to configure.

## Assignments

| Topic | Assignment | Main class | Description |
|-------|------------|------------|-------------|
| intro | calculatorapi | `dsy.intro.calculatorapi.CalculatorApiCli` | CLI client for the [Simple Calculator API](https://testpages.eviltester.com/apps/calculator-api/). Type expressions such as `2 + 3 * 4`; one operator uses `GET /calculate`, several use `POST /sequence` (left to right, no operator precedence). |
| springboot | calculatorservice | `dsy.springboot.calculatorservice.CalculatorServiceApplication` | The same calculator as a web service: `GET /calculate?expression=2 + 3 * 4`, calling the API with Spring's `RestClient`. |
