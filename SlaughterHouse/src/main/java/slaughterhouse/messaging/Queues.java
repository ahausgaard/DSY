package slaughterhouse.messaging;

// Queue names shared by the stations (publish) and the server (listen), so the two sides can't disagree
public final class Queues
{
  public static final String ANIMALS = "slaughterhouse.animals";

  private Queues()
  {
  }
}
