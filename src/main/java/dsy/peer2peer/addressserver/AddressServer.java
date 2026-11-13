package dsy.peer2peer.addressserver;

import dsy.peer2peer.peer.RemotePeer;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AddressServer extends UnicastRemoteObject implements RemoteAddressServer
{
  // RMI handles each incoming call on its own thread, so the map must be thread-safe
  private final Map<String, RemotePeer> peers = new ConcurrentHashMap<>();

  // Extending UnicastRemoteObject exports the object here, which can fail with RemoteException
  public AddressServer() throws RemoteException
  {
    super();
  }

  @Override public void register(String alias, RemotePeer peer)
      throws RemoteException
  {
    peers.put(alias, peer);
    System.out.println("Registered: " + alias);
  }

  @Override public RemotePeer lookup(String alias) throws RemoteException
  {
    RemotePeer peer = peers.get(alias);
    System.out.println("Lookup: " + alias + (peer == null ? " (not found)" : ""));
    return peer;
  }
}
