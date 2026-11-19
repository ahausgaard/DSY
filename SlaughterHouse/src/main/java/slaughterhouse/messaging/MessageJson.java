package slaughterhouse.messaging;

import tools.jackson.databind.json.JsonMapper;

// Turns message records into the bytes RabbitMQ carries, and back.
// Jackson 3 handles UUID, BigDecimal and LocalDateTime without extra modules
public final class MessageJson
{
  private static final JsonMapper MAPPER = JsonMapper.builder().build();

  private MessageJson()
  {
  }

  public static byte[] toBytes(Object message)
  {
    return MAPPER.writeValueAsBytes(message);
  }

  public static <T> T fromBytes(byte[] body, Class<T> type)
  {
    return MAPPER.readValue(body, type);
  }
}
