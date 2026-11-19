package slaughterhouse.station;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;

// Reads operator input and asks again on a typo, so a mistake never stops the station
class ConsoleInput
{
  private final Scanner scanner = new Scanner(System.in);

  String line(String prompt)
  {
    System.out.print(prompt);
    return scanner.nextLine().trim();
  }

  UUID uuid(String prompt)
  {
    while (true)
    {
      try
      {
        return UUID.fromString(line(prompt));
      }
      catch (IllegalArgumentException e)
      {
        System.out.println("Not a valid id. Try again.");
      }
    }
  }

  // One or more ids separated by commas
  Set<UUID> uuids(String prompt)
  {
    while (true)
    {
      try
      {
        Set<UUID> ids = new HashSet<>();
        for (String part : line(prompt).split(","))
          ids.add(UUID.fromString(part.trim()));
        return ids;
      }
      catch (IllegalArgumentException e)
      {
        System.out.println("Give one or more valid ids, separated by commas. Try again.");
      }
    }
  }

  // A code from a fixed list, e.g. part type 101
  int code(String prompt, List<Integer> allowed)
  {
    while (true)
    {
      try
      {
        int value = Integer.parseInt(line(prompt));
        if (allowed.contains(value))
          return value;
      }
      catch (NumberFormatException e)
      {
        // falls through to the message below
      }
      System.out.println("Choose one of " + allowed + ". Try again.");
    }
  }

  // A name from a fixed list, e.g. species "Pig"
  String oneOf(String prompt, List<String> allowed)
  {
    while (true)
    {
      String value = line(prompt);
      if (allowed.contains(value))
        return value;
      System.out.println("Choose one of " + allowed + ". Try again.");
    }
  }

  BigDecimal positiveDecimal(String prompt)
  {
    while (true)
    {
      try
      {
        BigDecimal value = new BigDecimal(line(prompt));
        if (value.signum() > 0)
          return value;
      }
      catch (NumberFormatException e)
      {
        // falls through to the message below
      }
      System.out.println("Give a positive number, e.g. 12.5. Try again.");
    }
  }
}
