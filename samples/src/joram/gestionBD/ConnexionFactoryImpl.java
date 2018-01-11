package gestionBD;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import java.util.Hashtable;

public class ConnexionFactoryImpl extends UnicastRemoteObject implements ConnexionFactory
{
    private ConnexionPool connexionPool;
	private Hashtable<Integer, ConnexionImpl> connexions;
    
    public ConnexionFactoryImpl() throws RemoteException 
    {
        super();
        this.connexionPool = new ConnexionPool();
		this.connexions = new Hashtable();
    }
    
	@Override
    public synchronized Connexion getConnexion() throws RemoteException
    {
		int id = 0;
		while(this.connexions.containsKey(id))
			id++;
		ConnexionImpl connexion = new ConnexionImpl(this.connexionPool.getConnexion(), id);
		this.connexions.put(id, connexion);
        return (Connexion)connexion;
    }
    
    @Override
    public synchronized void libereConnexion(Connexion connexion) throws RemoteException
    {
		int id = connexion.getId();
		if(this.connexions.containsKey(id))
		{
			this.connexionPool.returnConnexion(this.connexions.get(id).getConnexion());
			this.connexions.remove(id);
			System.out.println("Connexion libérée.");
		}
    }
}
