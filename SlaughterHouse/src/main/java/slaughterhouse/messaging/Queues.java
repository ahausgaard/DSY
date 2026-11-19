package slaughterhouse.messaging;

// Queue names shared by the stations (publish) and the server (listen), so the two sides can't disagree
public final class Queues
{
  // One queue for all three stations. A queue keeps the order messages arrived in, so after the server
  // has been down it still sees "tray closed" before the "product packed" that needs that tray.
  // Separate queues per station would be drained side by side, and could reject work in the wrong order
  public static final String REGISTRATIONS = "slaughterhouse.registrations";

  private Queues()
  {
  }
}
