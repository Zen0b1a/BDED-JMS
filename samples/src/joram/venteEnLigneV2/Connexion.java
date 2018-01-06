package venteEnLigneV2;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleDriver;

public class Connexion
{
	final private static String LOGIN = "ag092850";
    final private static String MDP = "ag092850";
    final private static String URL_FAC = "jdbc:oracle:thin:@butor:1521:ensb2017";
    final private static String URL_EXTERIEUR = "jdbc:oracle:thin:@ufrsciencestech.u-bourgogne.fr:25561:ensb2017";
	private Connection connexion;
	
	//Initialisation de la connexion
	public Connexion() throws SQLException
	{
		OracleDriver dr = new OracleDriver();
		Properties prop = new Properties();
		prop.setProperty("user", Connexion.LOGIN);
		prop.setProperty("password", Connexion.MDP);
		try
		{
			this.connexion = dr.connect(Connexion.URL_FAC, prop);
		}
		catch (SQLException ex) 
		{
			this.connexion = dr.connect(Connexion.URL_EXTERIEUR, prop);
		}
	}
	
	//Obtenir l'état d'une commande
	public String getEtatCommande(int id_commande)
	{
		String etat = "";
		try
		{
			PreparedStatement stmt = this.connexion.prepareStatement("SELECT etat FROM jms_commande WHERE id=?");
			stmt.setInt(1, id_commande);
			ResultSet rs = stmt.executeQuery();
			while(rs.next())
				etat = rs.getString(1);
			rs.close();
			stmt.close();
		}
		catch(SQLException ex){}
		return etat;
	}
	
	//Changer l'état d'une commande
	public boolean setEtatCommande(int id_commande, String etat)
	{
		if(etat.equals("initiee") || etat.equals("validee") || etat.equals("preparee") || etat.equals("payee") || etat.equals("expediee"))
		{
			try
			{
				PreparedStatement stmt = this.connexion.prepareStatement("UPDATE jms_commande SET etat=? WHERE id=?");
				stmt.setString(1, etat);
				stmt.setInt(2, id_commande);
				stmt.executeUpdate();
				stmt.close();
				return true;
			}
			catch(SQLException ex)
			{
				return false;
			}
		}
		else
			return false;
	}
	
	//Liste des commandes dans un état donné
	public List<Integer> getCommandes(String etat)
	{
		List<Integer> commandes = new ArrayList();
		try
		{
			PreparedStatement stmt = this.connexion.prepareStatement("SELECT id FROM jms_commande WHERE etat=?");
			stmt.setString(1, etat);
			ResultSet rs = stmt.executeQuery();
			while(rs.next())
				commandes.add(rs.getInt(1));
			rs.close();
			stmt.close();
		}
		catch(SQLException ex){}
		return commandes;
	}
	
	//Vérification du stock
	public boolean stockSuffisant(int id_commande)
	{
		boolean stockOK = true;
		try
		{
			PreparedStatement stmt = this.connexion.prepareStatement("SELECT cp.quantite, p.stock FROM "+
				"jms_commande_produit cp "+
				"INNER JOIN jms_commande c ON c.id=cp.id_commande "+
				"INNER JOIN jms_produit p ON p.id=cp.id_produit "+
				"WHERE c.id=?");
			stmt.setInt(1, id_commande);
			ResultSet rs = stmt.executeQuery();
			while(stockOK && rs.next())
			{
				int quantite_commande = rs.getInt(1);
				int stock_produit = rs.getInt(2);
				if(stock_produit<quantite_commande)
					stockOK = false;
			}
			rs.close();
			stmt.close();
			return stockOK;
		}
		catch(SQLException ex)
		{
			return false;
		}
	}
	
	//Mise à jour du stock
	public boolean majStock(int id_commande)
	{
		try
		{
			PreparedStatement stmt = this.connexion.prepareStatement("SELECT cp.quantite, p.stock, p.id FROM "+
				"jms_commande_produit cp "+
				"INNER JOIN jms_commande c ON c.id=cp.id_commande "+
				"INNER JOIN jms_produit p ON p.id=cp.id_produit "+
				"WHERE c.id=?");
			stmt.setInt(1, id_commande);
			ResultSet rs = stmt.executeQuery();
			while(rs.next())
			{
				int quantite_commande = rs.getInt(1);
				int stock_produit = rs.getInt(2);
				int id_produit = rs.getInt(3);
				int nouveau_stock = stock_produit - quantite_commande;
				PreparedStatement stmt2 = this.connexion.prepareStatement("UPDATE jms_produit SET stock=? WHERE id=?");
				stmt2.setInt(1, nouveau_stock);
				stmt2.setInt(2, id_produit);
				stmt2.executeUpdate();
				stmt2.close();
			}
			rs.close();
			stmt.close();
			return true;
		}
		catch(SQLException ex)
		{
			return false;
		}
	}
	
	//Fermeture de la connexion
	public void closeConnexion() throws SQLException
	{
		this.connexion.close();
	}
}