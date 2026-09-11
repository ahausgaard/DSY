# DSY

School work for DSY, collected in one Maven project. Every topic is a package, every
assignment is a subpackage with its own `main` method that prints to the console.

## Layout

```
src/main/java/dsy/
  intro/                       <- topic
    calculatorapi/             <- assignment
      CalculatorApiCli.java    <- has main(), run this one
src/test/java/dsy/
  intro/
    calculatorapi/             <- tests for that assignment (if any)
```

## Running

Open the class with the `main` method and press the green arrow in IntelliJ, or from a terminal:

```
mvn compile
java -cp target/classes dsy.intro.calculatorapi.CalculatorApiCli
```

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
