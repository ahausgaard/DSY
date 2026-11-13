package dsy.peer2peer.peer;

import java.rmi.RemoteException;
import java.rmi.Remote;
import java.util.List;

public interface RemotePeer extends Remote
{
  void receiveMessage(String fromAlias, String text) throws RemoteException;

  List<String> getSeededMedia() throws RemoteException;

  String download(String media) throws RemoteException;
}
