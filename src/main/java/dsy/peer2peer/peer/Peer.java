package dsy.peer2peer.peer;

import dsy.peer2peer.addressserver.RemoteAddressServer;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class Peer extends UnicastRemoteObject implements RemotePeer
{
  private final String alias;
  private final RemoteAddressServer server;
  private final List<String> mediaLibrary;
  private final List<String> seededMedia;

  // Extending UnicastRemoteObject exports the peer here, so other peers can call receiveMessage
  public Peer(String alias, RemoteAddressServer server) throws RemoteException
  {
    super();
    this.alias = alias;
    this.server = server;
    mediaLibrary = new ArrayList<>();
    seededMedia = new ArrayList<>();
  }

  /** Registers this peer at the address server. RMI sends a stub of this object, not the object itself. */
  public void register() throws RemoteException
  {
    server.register(alias, this);
  }

  /** Called locally: asks the server where the other peer is, then sends the message directly to it. */
  public void sendMessage(String toAlias, String text)
  {
    try
    {
      RemotePeer receiver = server.lookup(toAlias);
      if (receiver == null)
      {
        System.out.println("No peer called '" + toAlias + "'");
        return;
      }
      receiver.receiveMessage(alias, text);
    }
    catch (RemoteException e)
    {
      System.out.println(
          "Could not deliver to '" + toAlias + "': " + e.getMessage());
    }
  }

  public void addMedia(String media)
  {
    if (mediaLibrary.contains(media))
      throw new IllegalArgumentException(
          "'" + media + "' is already in the media library");

    mediaLibrary.add(media);
  }

  /** Removes media from the local library and stops seeding it, since a peer can't share what it doesn't have. */
  public void removeMedia(String media)
  {
    if (!mediaLibrary.remove(media))
    {
      throw new IllegalArgumentException(
          "'" + media + "' is not in the media library");
    }
    seededMedia.remove(media);
  }

  /** A copy, so callers can't change the library behind the peer's back. */
  public List<String> getLibrary()
  {
    return new ArrayList<>(mediaLibrary);
  }

  /** Starts sharing media from the local library. */
  public void seed(String media)
  {
    if (!mediaLibrary.contains(media))
      throw new IllegalArgumentException(
          "'" + media + "' is not in the media library");

    if (seededMedia.contains(media))
      throw new IllegalArgumentException(
          "'" + media + "' is already being seeded");

    seededMedia.add(media);
  }

  @Override public List<String> getSeededMedia() throws RemoteException
  {
    return seededMedia;
  }

  @Override public String download(String media) throws RemoteException
  {
    if (!seededMedia.contains(media))
      throw new IllegalArgumentException(
          "'" + media + "' is not being seeded by " + alias + " currently.");
    return media;
  }

  /** Asks another peer which media it seeds. */
  public List<String> browse(String toAlias) throws RemoteException
  {

      RemotePeer receiver = server.lookup(toAlias);

      if (receiver == null)
        throw new IllegalArgumentException("No peer called " + toAlias);

      return receiver.getSeededMedia();
  }

  /** Downloads media that another peer seeds into the local library. */
  public void leech(String toAlias, String media) throws RemoteException
  {
    RemotePeer receiver = server.lookup(toAlias);
    if (receiver == null)
      throw new IllegalArgumentException("No peer called " + toAlias);

    // download throws IllegalArgumentException if the other peer doesn't seed it; RMI passes that back to us
    String downloaded = receiver.download(media);
    addMedia(downloaded);
  }

  /** Called remotely by other peers. */
  @Override public void receiveMessage(String fromAlias, String text)
      throws RemoteException
  {
    System.out.println(fromAlias + ": " + text);
  }
}
