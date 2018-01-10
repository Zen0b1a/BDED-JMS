package gestionBD;

import java.rmi.Remote;
import java.rmi.RemoteException;

import java.util.List;

public interface ConnexionFactory extends Remote
{
    public Connexion getConnexion() throws RemoteException;
    public void libereConnexion(Connexion connexion) throws RemoteException;
}