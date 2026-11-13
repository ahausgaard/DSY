package dsy.peer2peer.peer;

import dsy.peer2peer.addressserver.RemoteAddressServer;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Scanner;

/**
 * Starts one peer. Arguments: [alias] [server host]. Without an alias it asks for one;
 * the host defaults to localhost.
 * Then type commands like "msg <alias> <text>" or "add <media>"; "help" lists them all.
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
    System.out.println("Registered as '" + alias + "'.");
    printHelp();

    boolean running = true;
    while (running && in.hasNextLine())
    {
      String line = in.nextLine().trim();
      if (line.isEmpty())
      {
        continue;
      }

      // First word is the command, the rest is its argument (may contain spaces, e.g. song titles)
      String[] parts = line.split(" ", 2);
      String command = parts[0];
      String rest = parts.length > 1 ? parts[1].trim() : "";

      try
      {
        switch (command)
        {
          case "quit" -> running = false;
          case "help" -> printHelp();
          case "msg" ->
          {
            String[] msg = rest.split(" ", 2);
            if (msg.length < 2)
            {
              System.out.println("Usage: msg <alias> <text>");
            }
            else
            {
              peer.sendMessage(msg[0], msg[1]);
            }
          }
          case "add" ->
          {
            if (requireArgument(rest, "add <media>"))
            {
              peer.addMedia(rest);
              System.out.println("Added '" + rest + "'");
            }
          }
          case "remove" ->
          {
            if (requireArgument(rest, "remove <media>"))
            {
              peer.removeMedia(rest);
              System.out.println("Removed '" + rest + "'");
            }
          }
          case "library" -> printList("Your library", peer.getLibrary());
          case "seed" ->
          {
            if (requireArgument(rest, "seed <media>"))
            {
              peer.seed(rest);
              System.out.println("Seeding '" + rest + "'");
            }
          }
          case "browse" ->
          {
            if (requireArgument(rest, "browse <alias>"))
            {
              printList(rest + " seeds", peer.browse(rest));
            }
          }
          case "leech" ->
          {
            String[] leech = rest.split(" ", 2);
            if (leech.length < 2)
            {
              System.out.println("Usage: leech <alias> <media>");
            }
            else
            {
              peer.leech(leech[0], leech[1]);
              System.out.println("Leeched '" + leech[1] + "' from " + leech[0]);
            }
          }
          default -> System.out.println("Unknown command '" + command + "'. Type help for commands.");
        }
      }
      catch (IllegalArgumentException e)
      {
        System.out.println(e.getMessage());
      }
      catch (RemoteException e)
      {
        System.out.println("Remote call failed: " + e.getMessage());
      }
    }

    // Stop listening for RMI calls so the JVM can exit
    UnicastRemoteObject.unexportObject(peer, true);
  }

  private static void printHelp()
  {
    System.out.println("""
        Commands:
          msg <alias> <text>      send a message
          add <media>             add to your library
          remove <media>          remove from your library
          library                 list your library
          seed <media>            share media from your library
          browse <alias>          list what another peer seeds
          leech <alias> <media>   download media from another peer
          help                    show this list
          quit""");
  }

  /** Prints usage and returns false when the command is missing its argument. */
  private static boolean requireArgument(String rest, String usage)
  {
    if (rest.isEmpty())
    {
      System.out.println("Usage: " + usage);
      return false;
    }
    return true;
  }

  private static void printList(String title, List<String> items)
  {
    if (items.isEmpty())
    {
      System.out.println(title + ": (nothing)");
      return;
    }
    System.out.println(title + ":");
    for (String item : items)
    {
      System.out.println("  " + item);
    }
  }
}
