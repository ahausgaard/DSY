package slaughterhouse.messaging;

import tools.jackson.databind.json.JsonMapper;

// Turns station messages into the bytes RabbitMQ carries, and back.
// Jackson 3 handles UUID, BigDecimal and LocalDateTime without extra modules
public final class MessageJson
{
  private static final JsonMapper MAPPER = JsonMapper.builder().build();

  private MessageJson()
  {
  }

  public static byte[] toBytes(StationMessage message)
  {
    // Written as StationMessage so the "@type" name is always included
    return MAPPER.writerFor(StationMessage.class).writeValueAsBytes(message);
  }

  public static StationMessage fromBytes(byte[] body)
  {
    return MAPPER.readValue(body, StationMessage.class);
  }
}
