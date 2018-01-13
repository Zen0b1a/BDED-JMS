package venteEnLigneV1.preparation;

import gestionBD.Connexion;
import gestionBD.ConnexionFactory;

import java.util.Hashtable;
import java.util.List;

import javax.jms.*;
import javax.naming.*;

public class Preparation
{
	private static Context ictx = null; 
	
	public static void main (String argv[]) throws Exception
	{
		new Preparation();
	}
	
	public Preparation() throws Exception
	{
		//Récupération des paramètres pour accéder au rmiregistry
		ictx = new InitialContext();
		Hashtable<Object,Object> env = (Hashtable)ictx.lookup("env_rmi");
		//Récupération de la ConnexionFactory depuis le rmiregistry
		ictx = new InitialContext(env);
		ConnexionFactory cfBD = (ConnexionFactory)ictx.lookup("cfBD");
		//Retour sur l'annuaire jndi
		ictx = new InitialContext();
		Connexion connexion = cfBD.getConnexion();
		int id_commande = -1;
		boolean commande_valide = true;
		boolean continuer = true;
		
		//Queue pour recevoir les messages du service validation
		QueueConnectionFactory qcf = (QueueConnectionFactory) ictx.lookup("qcf");
		QueueConnection cnx = qcf.createQueueConnection("preparation", "preparation");
		QueueSession session = cnx.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
		Queue queue = (Queue) ictx.lookup("validation");
		QueueReceiver receiver = session.createReceiver(queue);
		
		cnx.start();
		
		while(continuer)
		{
			System.out.println("En attente d'une commande à préparer.");
			MapMessage msg = (MapMessage)receiver.receive();
			System.out.println("Message reçu.");
			//Extraction de l'id de la commande, et du boolean de validation
			id_commande = msg.getInt("id");
			commande_valide = msg.getBoolean("valide");
			
			if(commande_valide)
			{
				System.out.println("Commande "+id_commande+" reçue, appuyer sur une touche pour la préparer.");
				System.in.read();
				
				//Mise à jour du stock
				commande_valide = connexion.majStockPreparation(id_commande);
				
				//Changement de l'état de la commande
				if(commande_valide)
				{
					commande_valide = connexion.setEtatCommande(id_commande, "preparee");
					//Envoi du message
					this.envoiMessage(session, id_commande, commande_valide);
					System.out.println("Commande "+id_commande+" préparée.");
				}
				else
					connexion.razCommande(id_commande);
			}
			else
			{
				System.out.println("Commande "+id_commande+" invalide : réinitialisation de son état.");
				connexion.razCommande(id_commande);
			}
			
			//Prévoir cas d'arrêt
			continuer = false;
		}
		
		//Fermeture
		cfBD.libereConnexion(connexion);
		ictx.close();
		cnx.close();
	}
	
	private boolean envoiMessage(QueueSession session, int id_commande, boolean commande_valide)
	{
		try
		{
			System.out.println("Envoi d'un message pour la commande "+id_commande+" preparée : "+commande_valide);
			
			Queue queue = (Queue) ictx.lookup("preparation");
			QueueSender sender = session.createSender(queue);
			
			MapMessage msg = session.createMapMessage();
			msg.setInt("id", id_commande);
			msg.setBoolean("valide", commande_valide);
			
			sender.send(msg);
			
			return true;
		}
		catch(Exception ex)
		{
			return false;
		}
	}
}