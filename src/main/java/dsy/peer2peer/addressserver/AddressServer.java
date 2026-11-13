package dsy.peer2peer.addressserver;

import dsy.peer2peer.peer.RemotePeer;

import java.rmi.RemoteException;

public class AddressServer implements RemoteAddressServer
{
  @Override public void register(String alias, RemotePeer peer)
      throws RemoteException
  {

  }

  @Override public RemotePeer lookup(String alias) throws RemoteException
  {
    return null;
  }
}
