package gestionBD;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import java.util.ArrayList;
import java.util.List;

public class ConnexionFactoryImpl extends UnicastRemoteObject implements ConnexionFactory
{
    private ConnexionPool connexionPool;
	private List<ConnexionImpl> connexions;
    
    public ConnexionFactoryImpl() throws RemoteException 
    {
        super();
        this.connexionPool = new ConnexionPool();
		this.connexions = new ArrayList();
    }
    
	@Override
    public synchronized Connexion getConnexion() throws RemoteException
    {
		ConnexionImpl connexion = new ConnexionImpl(this.connexionPool.getConnexion());
		this.connexions.add(connexion);
        return (Connexion)connexion;
    }
    
    @Override
    public synchronized void libereConnexion(Connexion connexion) throws RemoteException
    {
		if(this.connexions.contains(connexion))
		{
			this.connexionPool.returnConnexion(connexions.get(connexions.indexOf(connexion)).getConnexion());
			this.connexions.remove(connexion);
			System.out.println("Connexion libérée.");
		}
    }
}
