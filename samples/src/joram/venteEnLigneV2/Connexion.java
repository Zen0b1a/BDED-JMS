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
	public Connexion()
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
			try
			{
				this.connexion = dr.connect(Connexion.URL_EXTERIEUR, prop);
			}
			catch (SQLException ex2) 
			{
				
			}
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
	public boolean majStockValidation(int id_commande)
	{
		try
		{
			PreparedStatement stmt = this.connexion.prepareStatement("SELECT cp.quantite, p.stock, p.stock_pour_commandes, p.id FROM "+
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
				int stock_produit_pour_commandes = rs.getInt(3);
				int id_produit = rs.getInt(4);
				int nouveau_stock = stock_produit - quantite_commande;
				int nouveau_stock_pour_commandes = stock_produit_pour_commandes + quantite_commande;
				PreparedStatement stmt2 = this.connexion.prepareStatement("UPDATE jms_produit SET stock_pour_commandes=?, stock=? WHERE id=?");
				stmt2.setInt(1, nouveau_stock_pour_commandes);
				stmt2.setInt(2, nouveau_stock);
				stmt2.setInt(3, id_produit);
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
	
	public boolean majStockPreparation(int id_commande)
	{
		try
		{
			PreparedStatement stmt = this.connexion.prepareStatement("SELECT cp.quantite, p.stock_pour_commandes, p.id FROM "+
				"jms_commande_produit cp "+
				"INNER JOIN jms_commande c ON c.id=cp.id_commande "+
				"INNER JOIN jms_produit p ON p.id=cp.id_produit "+
				"WHERE c.id=?");
			stmt.setInt(1, id_commande);
			ResultSet rs = stmt.executeQuery();
			while(rs.next())
			{
				int quantite_commande = rs.getInt(1);
				int stock_produit_pour_commandes = rs.getInt(2);
				int id_produit = rs.getInt(3);
				int nouveau_stock_pour_commandes = stock_produit_pour_commandes - quantite_commande;
				PreparedStatement stmt2 = this.connexion.prepareStatement("UPDATE jms_produit SET stock_pour_commandes=? WHERE id=?");
				stmt2.setInt(1, nouveau_stock_pour_commandes);
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
	
	public boolean razCommande(int id_commande)
	{
		try
		{
			PreparedStatement stmt = this.connexion.prepareStatement("SELECT cp.quantite, p.stock_pour_commandes, p.id, c.etat FROM "+
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
				int stock_produit_pour_commandes = rs.getInt(3);
				int id_produit = rs.getInt(4);
				String etat = rs.getString(5);
				
				PreparedStatement stmt2 = this.connexion.prepareStatement("UPDATE jms_produit SET stock_pour_commandes=?, stock=? WHERE id=?");
				if(!"initiee".equals(etat))
				{
					if("validee".equals(etat))
					{
						//Le stock du produit qui concerne des commandes validées mais non préparées n'est est diminué de la quantité de ce produit dans la commande
						//Le stock du produit est augmenté de la quantité de ce produit dans la commande
						stmt2.setInt(1, stock_produit_pour_commandes-quantite_commande);
						stmt2.setInt(2, stock_produit+quantite_commande);
						stmt2.setInt(3, id_produit);
						stmt2.executeUpdate();
					}
					else if("preparee".equals(etat) || "payee".equals(etat) || "expediee".equals(etat))
					{
						//Le stock du produit qui concerne des commandes validées mais non préparées n'est pas modifié
						//Le stock du produit est augmenté de la quantité de ce produit dans la commande
						stmt2.setInt(1, stock_produit_pour_commandes);
						stmt2.setInt(2, stock_produit+quantite_commande);
						stmt2.setInt(3, id_produit);
						stmt2.executeUpdate();
					}
				}
				int nouveau_stock_pour_commandes = stock_produit_pour_commandes - quantite_commande;
				stmt2.close();
			}
			//Remise de l'état à "initiee"
			stmt = this.connexion.prepareStatement("UPDATE jms_commande SET etat='initiee' WHERE id=?");
			stmt.setInt(1, id_commande);
			stmt.executeUpdate();
			
			rs.close();
			stmt.close();
			return true;
		}
		catch(SQLException ex)
		{
			return false;
		}
	}
	
	public boolean razBD()
	{
		try
		{
			PreparedStatement stmt = this.connexion.prepareStatement("UPDATE jms_commande SET etat='initiee'");
			stmt.executeUpdate();
			stmt = this.connexion.prepareStatement("UPDATE jms_produit SET stock=10, stock_pour_commandes=0");
			stmt.executeUpdate();
			stmt.close();
			return true;
		}
		catch(SQLException ex)
		{
			return false;
		}
	}
	
	public void closeConnexion()
	{
		try
		{
			this.connexion.close();
		}
		catch(SQLException ex)
		{}
	}
}