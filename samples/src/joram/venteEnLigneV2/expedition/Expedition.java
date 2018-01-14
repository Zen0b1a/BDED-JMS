package venteEnLigneV2.expedition;

import javax.jms.*;
import javax.naming.*;

public class Expedition
{
	private static Context ictx = null; 
	
	public static void main (String argv[]) throws Exception
	{
		new Expedition();
	}
	
	public Expedition() throws Exception
	{
		ictx = new InitialContext();
		int id_commande = -1;
		boolean commande_valide = true;
		boolean continuer = true;
		System.setProperty("location", "server4");
		
		//Queue pour recevoir les messages du service facturation
		ConnectionFactory cf = (ConnectionFactory)ictx.lookup("cf");
		Connection cnx = cf.createConnection("expedition", "expedition");
		Session session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Queue queue = (Queue) ictx.lookup("facturation");
		MessageConsumer receiver = session.createConsumer(queue);
		receiver.setMessageListener(new ExpeditionListener(session));
		cnx.start();
		
		System.in.read();
		
		//Fermeture
		ictx.close();
		cnx.close();
	}
}

class ExpeditionListener implements MessageListener 
{
	private Session session;
	
	public ExpeditionListener(Session session)
	{
		this.session = session;
		System.out.println("En attente d'une commande à expédier.");
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
					System.out.println("Commande "+id_commande+" expédiée.");
				}

				System.out.println("En attente d'une commande à expédier.");
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
			System.out.println("Envoi d'un message pour la commande "+id_commande+" expédiée : "+commande_valide);
			
			Context ictx = new InitialContext();
			Queue queue = (Queue) ictx.lookup("expedition");
			MessageProducer sender = session.createProducer(queue);
			
			MapMessage msg = this.session.createMapMessage();
			msg.setString("service", "expedition");
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