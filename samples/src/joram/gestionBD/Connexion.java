package gestionBD;

import java.rmi.Remote;
import java.rmi.RemoteException;

import java.util.List;

public interface Connexion extends Remote
{
    public String getEtatCommande(int id_commande) throws RemoteException;
	public boolean setEtatCommande(int id_commande, String etat) throws RemoteException;
	public List<Integer> getCommandes(String etat) throws RemoteException;
	public boolean stockSuffisant(int id_commande) throws RemoteException;
	public boolean majStock(int id_commande) throws RemoteException;
	public int getId() throws RemoteException;
}