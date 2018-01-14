package venteEnLigneV2.preparation;

import java.util.List;

import javax.jms.*;
import javax.naming.*;

public class Preparation
{
	public static void main (String argv[]) throws Exception
	{
		new Preparation();
	}
	
	public Preparation() throws Exception
	{
		Context ictx = new InitialContext();
		int id_commande = -1;
		boolean commande_valide = true;
		System.setProperty("location", "server2");
		
		//Queue pour recevoir les messages du service validation
		ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
		Connection cnx = cf.createConnection("preparation", "preparation");
		Session session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Queue queue = (Queue) ictx.lookup("validation");
		MessageConsumer receiver = session.createConsumer(queue);
		receiver.setMessageListener(new PreparationListener(session));
		cnx.start();
		
		System.in.read();
		
		//Fermeture
		ictx.close();
		cnx.close();
	}
}

class PreparationListener implements MessageListener 
{
	private Session session;
	
	public PreparationListener(Session session)
	{
		this.session = session;
		System.out.println("En attente d'une commande à préparer.");
	}

	public void onMessage(Message message) 
	{
		try
		{
			if(message instanceof MapMessage && ((MapMessage)message).itemExists("id") && ((MapMessage)message).itemExists("valide"))
			{
				MapMessage msg = (MapMessage)message;
				System.out.println("Message reçu.");
				//Extraction de l'id de la commande, et du boolean de validation
				int id_commande = msg.getInt("id");
				boolean commande_valide = msg.getBoolean("valide");
			
				if(commande_valide)
				{
					//Envoi du message
					this.envoiMessage(id_commande, commande_valide);
					System.out.println("Commande "+id_commande+" préparée.");
				}

				System.out.println("En attente d'une commande à préparer.");
			}
		}
		catch(Exception ex)
		{
			System.out.println(ex);
		}
	}
	
	private boolean envoiMessage(int id_commande, boolean commande_valide)
	{
		try
		{
			System.out.println("Envoi d'un message pour la commande "+id_commande+" preparée : "+commande_valide);
			
			Context ictx = new InitialContext();
			Queue queue = (Queue) ictx.lookup("preparation");
			MessageProducer sender = session.createProducer(queue);
			
			MapMessage msg = this.session.createMapMessage();
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