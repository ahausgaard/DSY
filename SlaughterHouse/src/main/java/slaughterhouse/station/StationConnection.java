package slaughterhouse.station;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import slaughterhouse.messaging.MessageJson;
import slaughterhouse.messaging.Queues;
import slaughterhouse.messaging.StationMessage;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

// A station's link to RabbitMQ: one connection for the whole session, publishing to the shared queue.
// The station never waits for the server; the queue holds its messages until the server takes them
class StationConnection implements AutoCloseable
{
  private final Connection connection;
  private final Channel channel;

  StationConnection(String host) throws IOException, TimeoutException
  {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(host);
    connection = factory.newConnection();
    channel = connection.createChannel();
    // Durable, not exclusive, not auto-deleted: the queue and its messages outlive both station and server
    channel.queueDeclare(Queues.REGISTRATIONS, true, false, false, null);
  }

  void publish(StationMessage message) throws IOException
  {
    // Persistent, so the message also survives a broker restart
    channel.basicPublish("", Queues.REGISTRATIONS, MessageProperties.PERSISTENT_TEXT_PLAIN,
        MessageJson.toBytes(message));
  }

  @Override public void close() throws IOException, TimeoutException
  {
    channel.close();
    connection.close();
  }
}
