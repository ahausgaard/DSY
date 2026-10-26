package slaughterhouse.service;

// The request is well-formed but breaks a rule of the slaughterhouse, such as a full tray
public class RuleViolationException extends RuntimeException
{
  public RuleViolationException(String message)
  {
    super(message);
  }
}
