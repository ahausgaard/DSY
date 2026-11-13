package dsy.peer2peer.addressserver;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class AddressServerMain
{
  public static void main(String[] args) throws Exception
  {
    AddressServer server = new AddressServer();

    // Start the RMI registry inside this JVM and publish the server under a known name
    Registry registry = LocateRegistry.createRegistry(RemoteAddressServer.REGISTRY_PORT);
    registry.rebind(RemoteAddressServer.NAME, server);

    System.out.println("Address server ready on port " + RemoteAddressServer.REGISTRY_PORT);
    // main ends here, but the exported server keeps the JVM running
  }
}
