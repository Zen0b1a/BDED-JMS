package venteEnLigneV1.validation;

import java.util.List;
import java.util.Scanner;

import javax.jms.*;
import javax.naming.*;

import venteEnLigneV1.Connexion;

public class Validation 
{
	private static Context ictx = null; 
	
	public static void main (String argv[]) throws Exception
	{
		new Validation();
	}
	
	public Validation() throws Exception
	{
		Connexion connexion = new Connexion();
		int id_commande = -1;
		boolean commande_valide = true;
		
		//Liste des commande avec etat = initiee
		List<Integer> commandes = connexion.getCommandes("initiee");
		
		//Choix de la commande à valider
		Scanner sc = new Scanner(System.in);
		String choix_commande = "";
		for(int i=0; i<commandes.size(); i++)
			System.out.println("Commande "+commandes.get(i));
		if(commandes.size()>0)
		{
			//Tant que l'option est invalide on redemande
			while(choix_commande.equals(""))
			{
				System.out.println("Choisir le numéro de commande à valider :");
				choix_commande = sc.nextLine();
				try
				{
					if(!commandes.contains(Integer.parseInt(choix_commande)))
						choix_commande = "";
					else
						id_commande = Integer.parseInt(choix_commande);
				}
				catch(NumberFormatException ex)
				{
					choix_commande = "";
				}
			}
			
			//Vérification du stock des produits
			commande_valide = connexion.stockSuffisant(id_commande);
			
			if(commande_valide)
			{
				//Changement de l'état en BD
				if(commande_valide)
					commande_valide = connexion.setEtatCommande(id_commande, "validee");
				
				//Envoi du message
				this.envoiMessage(id_commande, commande_valide);
			}
			else
				System.out.println("Validation de la commande "+id_commande+" impossible : stock insuffisant.");
		}
		else
			System.out.println("Il n'y a pas de commande en attente de validation");
		
		//Fermeture de la connexion
		connexion.closeConnexion();
	}
	
	private boolean envoiMessage(int id_commande, boolean commande_valide)
	{
		try
		{
			System.out.println("Envoi d'un message pour la commande "+id_commande+" validée : "+commande_valide);
			ictx = new InitialContext();
			Queue queue = (Queue) ictx.lookup("validation");
			QueueConnectionFactory qcf = (QueueConnectionFactory) ictx.lookup("qcf");
			ictx.close();

			QueueConnection cnx = qcf.createQueueConnection("validation", "validation");
			QueueSession session = cnx.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			QueueSender sender = session.createSender(queue);
			
			MapMessage msg = session.createMapMessage();
			msg.setInt("id", id_commande);
			msg.setBoolean("valide", commande_valide);
			
			sender.send(msg);
			
			cnx.close();
			return true;
		}
		catch(Exception ex)
		{
			return false;
		}
	}
}