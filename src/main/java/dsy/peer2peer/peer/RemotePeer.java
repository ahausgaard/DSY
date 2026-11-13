package dsy.peer2peer.peer;

import java.rmi.RemoteException;
import java.rmi.Remote;

public interface RemotePeer extends Remote
{
  void receiveMessage(String fromAlias, String text) throws RemoteException;
}
