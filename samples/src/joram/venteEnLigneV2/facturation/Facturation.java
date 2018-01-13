package venteEnLigneV2.facturation;

import javax.jms.*;
import javax.naming.*;

import venteEnLigneV2.Connexion;

public class Facturation
{
	private static Context ictx = null; 
	
	public static void main (String argv[]) throws Exception
	{
		new Facturation();
	}
	
	public Facturation() throws Exception
	{
		ictx = new InitialContext();
		int id_commande = -1;
		boolean commande_valide = true;
		boolean continuer = true;
		System.setProperty("location", "server3");
		
		//Queue pour recevoir les messages du service preparation
		ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
		Connection cnx = cf.createConnection("facturation", "facturation");
		Session session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Queue queue = (Queue) ictx.lookup("preparation");
		MessageConsumer receiver = session.createConsumer(queue);
		
		cnx.start();
		
		while(continuer)
		{
			System.out.println("En attente d'une commande à facturer.");
			MapMessage msg = (MapMessage)receiver.receive();
			System.out.println("Message reçu.");
			//Extraction de l'id de la commande, et du boolean de validation
			id_commande = msg.getInt("id");
			commande_valide = msg.getBoolean("valide");
			
			if(commande_valide)
			{
				System.out.println("Commande "+id_commande+" reçue, appuyer sur une touche pour la facturer.");
				System.in.read();

				this.envoiMessage(session, id_commande, commande_valide);
				System.out.println("Commande "+id_commande+" facturée.");
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
			System.out.println("Envoi d'un message pour la commande "+id_commande+" facturée : "+commande_valide);
			
			Queue queue = (Queue) ictx.lookup("facturation");
			MessageProducer sender = session.createProducer(queue);
			
			MapMessage msg = session.createMapMessage();
			msg.setString("service", "facturation");
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