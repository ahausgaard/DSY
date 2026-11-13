package dsy.peer2peer.addressserver;
import dsy.peer2peer.peer.RemotePeer;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteAddressServer extends Remote
{
  /** Name the server is bound under in the RMI registry, shared by server and peers. */
  String NAME = "AddressServer";
  int REGISTRY_PORT = 1099;

  /** Registers a peer under an alias. An alias that is already taken is replaced, so a restarted peer can register again. */
  void register(String alias, RemotePeer peer) throws RemoteException;

  /** Returns the peer registered under the alias, or null if no peer has that alias. */
  RemotePeer lookup(String alias) throws RemoteException;
}
