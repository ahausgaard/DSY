package dsy.rabbitmq.chatSystem;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.stream.Environment;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Send
{
  private static final String EXCHANGE_NAME = "logs";

  static void main(String[] argv) throws IOException
  {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");

    try (Connection connection = factory.newConnection();

        Channel channel = connection.createChannel())
    {
      channel.exchangeDeclare(EXCHANGE_NAME, "fanout");

      String message =
          argv.length < 1 ? "info: Hello World!" : String.join(" ", argv);

      channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes("UTF-8"));
      System.out.println(" [x] Sent '" + message + "'");
    }
    catch (TimeoutException e)
    {
      throw new RuntimeException(e);
    }

  }
}
