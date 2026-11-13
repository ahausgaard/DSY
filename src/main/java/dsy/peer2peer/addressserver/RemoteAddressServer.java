package dsy.peer2peer.addressserver;
import dsy.peer2peer.peer.RemotePeer;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteAddressServer extends Remote
{
  void register(String alias, RemotePeer peer) throws RemoteException;
  RemotePeer lookup(String alias) throws RemoteException;
}
