package venteEnLigneV2.preparation;

import java.util.List;

import javax.jms.*;
import javax.naming.*;

import venteEnLigneV2.Connexion;

public class Preparation
{
	private static Context ictx = null; 
	
	public static void main (String argv[]) throws Exception
	{
		new Preparation();
	}
	
	public Preparation() throws Exception
	{
		ictx = new InitialContext();
		int id_commande = -1;
		boolean commande_valide = true;
		boolean continuer = true;
		System.setProperty("location", "server2");
		
		//Queue pour recevoir les messages du service validation
		ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
		Connection cnx = cf.createConnection("preparation", "preparation");
		Session session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Queue queue = (Queue) ictx.lookup("validation");
		MessageConsumer receiver = session.createConsumer(queue);
		
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
				
				//Envoi du message
				this.envoiMessage(session, id_commande, commande_valide);
				System.out.println("Commande "+id_commande+" préparée.");
			}
			else
				System.out.println("Commande "+id_commande+" invalide.");
			
			//Prévoir cas d'arrêt
			continuer = false;
		}
		
		//Fermeture
		ictx.close();
		cnx.close();
	}
	
	private boolean envoiMessage(Session session, int id_commande, boolean commande_valide)
	{
		try
		{
			System.out.println("Envoi d'un message pour la commande "+id_commande+" preparée : "+commande_valide);
			
			Queue queue = (Queue) ictx.lookup("preparation");
			MessageProducer sender = session.createProducer(queue);
			
			MapMessage msg = session.createMapMessage();
			msg.setString("service", "preparation");
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