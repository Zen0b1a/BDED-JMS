package action;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleStatement;

public class ModifActions
{
	public static void main(String[] args) throws Exception 
	{
		//Ouverture de la connexion
		OracleConnection connexion = Connexion.connect();
		
		//Choix du topic
		Scanner sc = new Scanner(System.in);
		String choix_option = "";
		int option = 0;
		System.out.println("*****Menu*****\n1 - Ajouter une nouvelle action\n2 - Insérer un cours d'action dans la base");
		//Tant que l'option est invalide on redemande
		while(choix_option.equals(""))
		{
			System.out.println("Choisir le numéro de l'option à exécuter :");
			choix_option = sc.nextLine();
			try
			{
				if(Integer.parseInt(choix_option)==1 || Integer.parseInt(choix_option)==2)
					option = Integer.parseInt(choix_option);
				else
					choix_option = "";
			}
			catch(NumberFormatException ex)
			{
				choix_option = "";
			}
		}
		
		//Récupération des actions existantes
		List<String> liste_actions = new ArrayList();
		List<Integer> id_actions = new ArrayList();
		Statement stmt = connexion.createStatement();
		ResultSet rs = stmt.executeQuery("select nom_action, id from action order by id");
		while(rs.next())
		{
			//Récupération du nom et de l'id de l'action
			liste_actions.add(rs.getString(1).toLowerCase());
			id_actions.add(rs.getInt(2));
		}
		
		if(option==1)
		{
			//Ajout d'une action dans la base
			System.out.println("Choisir le nom de l'action :");
			String nom_action = sc.nextLine();
			while(liste_actions.contains(nom_action))
			{
				System.out.println("Choisir un nom d'action qui n'existe pas. Actions existantes : ");
				for(int i=0; i<liste_actions.size(); i++)
					System.out.println(liste_actions.get(i));
				nom_action = sc.nextLine();
			}
			
			//Choix du parent
			System.out.println("Actions disponibles : \n-1 - Pas de parent");
			for(int i=0; i<liste_actions.size(); i++)
				System.out.println(i + " - " + liste_actions.get(i));
			String nom_parent = "";
			int id_parent = 0;
			//Tant que l'option est invalide on redemande
			while(nom_parent.equals(""))
			{
				System.out.println("Choisir le numéro de l'action voulue :");
				nom_parent = sc.nextLine();
				try
				{
					if(Integer.parseInt(nom_parent)<-1 || Integer.parseInt(nom_parent)>=liste_actions.size())
						nom_parent = "";
					else if(Integer.parseInt(nom_parent)==-1)
						nom_parent = "pas de parent";
					else
					{
						id_parent = id_actions.get(Integer.parseInt(nom_parent));
						nom_parent = liste_actions.get(Integer.parseInt(nom_parent));
					}
				}
				catch(NumberFormatException ex)
				{
					nom_parent = "";
				}
			}
			
			stmt.executeUpdate("CREATE TABLE " + nom_action + "(id INTEGER PRIMARY KEY, cours NUMBER(6,2))");
			rs = stmt.executeQuery("select MAX(id) from action");
			rs.next();
			int id = rs.getInt(1)+1;
			stmt.executeUpdate("INSERT INTO action VALUES(" + id + ", " + id_parent + ", '" + nom_action + "')");
			
			System.out.print("Ajout de l'action " + nom_action + ", parent : " + nom_parent);
		}
		else if(option==2)
		{
			//Insertion d'un cours d'action
			//Choix de l'action
			System.out.println("Actions disponibles : ");
			for(int i=0; i<liste_actions.size(); i++)
				System.out.println(i + " - " + liste_actions.get(i));
			String nom_action = "";
			//Tant que l'option est invalide on redemande
			while(nom_action.equals(""))
			{
				System.out.println("Choisir le numéro de l'action voulue :");
				nom_action = sc.nextLine();
				try
				{
					if(Integer.parseInt(nom_action)<0 || Integer.parseInt(nom_action)>=liste_actions.size())
						nom_action = "";
					else
						nom_action = liste_actions.get(Integer.parseInt(nom_action));
				}
				catch(NumberFormatException ex)
				{
					nom_action = "";
				}
			}
			
			//Choix du montant
			Double montant = 10000.00;
			String choix_montant = "";
			//Tant que l'option est invalide on redemande
			while(choix_montant.equals(""))
			{
				System.out.println("Choisir le montant (de 0,00 à 9999,99) :");
				choix_montant = sc.nextLine();
				try
				{
					if(Double.parseDouble(choix_montant)<0 || Double.parseDouble(choix_montant)>9999.99)
						choix_montant = "";
					else
						montant = Double.parseDouble(choix_montant);
				}
				catch(NumberFormatException ex)
				{
					choix_montant = "";
				}
			}
			rs = stmt.executeQuery("select MAX(id) from "+nom_action);
			rs.next();
			int id = rs.getInt(1)+1;
			stmt.executeUpdate("INSERT INTO " + nom_action + " VALUES(" + id + ", " + montant + ")");
			
			System.out.println("Insertion du cours " + id + " avec un montant de " + montant + " dans " + nom_action);
		}
		
		connexion.close();
	}
}