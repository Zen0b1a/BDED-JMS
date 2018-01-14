package venteEnLigneV2.facturation;

import javax.jms.*;
import javax.naming.*;

public class Facturation
{	
	public static void main (String argv[]) throws Exception
	{
		new Facturation();
	}
	
	public Facturation() throws Exception
	{
		Context ictx = new InitialContext();
		System.setProperty("location", "server3");
		
		//Queue pour recevoir les messages du service preparation
		ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
		Connection cnx = cf.createConnection("facturation", "facturation");
		Session session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Queue queue = (Queue) ictx.lookup("preparation");
		MessageConsumer receiver = session.createConsumer(queue);
		receiver.setMessageListener(new FacturationListener(session));
		cnx.start();
		
		System.in.read();
		
		//Fermeture
		ictx.close();
		cnx.close();
	}
}

class FacturationListener implements MessageListener 
{
	private Session session;
	
	public FacturationListener(Session session)
	{
		this.session = session;
		System.out.println("En attente d'une commande à facturer.");
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
					this.envoiMessage(id_commande, commande_valide);
					System.out.println("Commande "+id_commande+" facturée.");
				}

				System.out.println("En attente d'une commande à facturer.");
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
			System.out.println("Envoi d'un message pour la commande "+id_commande+" facturée : "+commande_valide);
			
			Context ictx = new InitialContext();
			Queue queue = (Queue) ictx.lookup("facturation");
			MessageProducer sender = session.createProducer(queue);
			
			MapMessage msg = this.session.createMapMessage();
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