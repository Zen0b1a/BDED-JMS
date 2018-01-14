package venteEnLigneV1.facturation;

import gestionBD.Connexion;
import gestionBD.ConnexionFactory;

import java.util.Hashtable;

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
		//Récupération des paramètres pour accéder au rmiregistry
		Context ictx = new InitialContext();
		Hashtable<Object,Object> env = (Hashtable)ictx.lookup("env_rmi");
		//Récupération de la ConnexionFactory depuis le rmiregistry
		ictx = new InitialContext(env);
		ConnexionFactory cfBD = (ConnexionFactory)ictx.lookup("cfBD");
		Connexion connexion = cfBD.getConnexion();
		
		//Retour sur l'annuaire jndi
		ictx = new InitialContext();
		
		//Queue pour recevoir les messages du service preparation
		QueueConnectionFactory qcf = (QueueConnectionFactory) ictx.lookup("qcf");
		QueueConnection cnx = qcf.createQueueConnection("facturation", "facturation");
		QueueSession session = cnx.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
		Queue queue = (Queue) ictx.lookup("preparation");
		QueueReceiver receiver = session.createReceiver(queue);
		receiver.setMessageListener(new FacturationListener(connexion, session));
		cnx.start();
		
		System.in.read();
		
		//Fermeture
		cfBD.libereConnexion(connexion);
		ictx.close();
		cnx.close();
	}
}

class FacturationListener implements MessageListener 
{
	private Connexion connexion;
	private QueueSession session;
	
	public FacturationListener(Connexion connexion, QueueSession session)
	{
		this.connexion = connexion;
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
					System.out.println("Commande "+id_commande+" reçue.");

					//Changement de l'état de la commande
					if(commande_valide)
					{
						commande_valide = this.connexion.setEtatCommande(id_commande, "payee");
						//Envoi du message
						this.envoiMessage(id_commande, commande_valide);
						System.out.println("Commande "+id_commande+" facturée.");
					}
					else
						this.connexion.razCommande(id_commande);
				}
				else
				{
					System.out.println("Commande "+id_commande+" invalide : réinitialisation de son état.");
					this.connexion.razCommande(id_commande);
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
			QueueSender sender = this.session.createSender(queue);
			
			MapMessage msg = this.session.createMapMessage();
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