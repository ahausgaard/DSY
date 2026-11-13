package dsy.peer2peer.peer;

import dsy.peer2peer.addressserver.RemoteAddressServer;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

/**
 * Starts one peer. Arguments: [alias] [server host]. Without an alias it asks for one;
 * the host defaults to localhost.
 * Then type "<alias> <message>" to send, or "quit" to stop.
 */
public class PeerMain
{
  public static void main(String[] args) throws Exception
  {
    Scanner in = new Scanner(System.in);

    String alias;
    if (args.length > 0)
    {
      alias = args[0];
    }
    else
    {
      System.out.print("Your alias: ");
      alias = in.nextLine().trim();
    }
    String host = args.length > 1 ? args[1] : "localhost";

    // Find the address server through the RMI registry it started
    Registry registry = LocateRegistry.getRegistry(host, RemoteAddressServer.REGISTRY_PORT);
    RemoteAddressServer server = (RemoteAddressServer) registry.lookup(RemoteAddressServer.NAME);

    Peer peer = new Peer(alias, server);
    peer.register();
    System.out.println("Registered as '" + alias + "'. Type: <alias> <message>, or quit");

    while (in.hasNextLine())
    {
      String line = in.nextLine().trim();
      if (line.equals("quit"))
      {
        break;
      }
      int space = line.indexOf(' ');
      if (space < 0)
      {
        System.out.println("Type: <alias> <message>");
        continue;
      }
      peer.sendMessage(line.substring(0, space), line.substring(space + 1));
    }

    // Stop listening for RMI calls so the JVM can exit
    UnicastRemoteObject.unexportObject(peer, true);
  }
}
