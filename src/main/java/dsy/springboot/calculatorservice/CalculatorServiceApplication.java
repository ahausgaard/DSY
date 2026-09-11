package dsy.springboot.calculatorservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot version of the calculator assignment: instead of a console loop, the calculator
 * is exposed as a small web service that itself calls the Simple Calculator API over HTTP.
 *
 * Start this class and try:  http://localhost:8080/calculate?expression=2 + 3 * 4
 *
 * Spring only scans this package and the ones below it, so the other topics in the project
 * are untouched by it.
 */
@SpringBootApplication
public class CalculatorServiceApplication
{
  public static void main(String[] args)
  {
    SpringApplication.run(CalculatorServiceApplication.class, args);
  }
}
