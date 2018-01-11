package venteEnLigneV1.facturation;

import gestionBD.Connexion;
import gestionBD.ConnexionFactory;

import java.util.Hashtable;

import javax.jms.*;
import javax.naming.*;

public class Facturation
{
	private static Context ictx = null; 
	
	public static void main (String argv[]) throws Exception
	{
		new Facturation();
	}
	
	public Facturation() throws Exception
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
		
		//Queue pour recevoir les messages du service preparation
		QueueConnectionFactory qcf = (QueueConnectionFactory) ictx.lookup("qcf");
		QueueConnection cnx = qcf.createQueueConnection("facturation", "facturation");
		QueueSession session = cnx.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
		Queue queue = (Queue) ictx.lookup("preparation");
		QueueReceiver receiver = session.createReceiver(queue);
		
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

				//Changement de l'état de la commande
				if(commande_valide)
				{
					commande_valide = connexion.setEtatCommande(id_commande, "payee");
					//Envoi du message
					this.envoiMessage(session, id_commande, commande_valide);
				}
				else
					commande_valide = connexion.setEtatCommande(id_commande, "initiee");
			}
			else
			{
				System.out.println("Commande "+id_commande+" invalide : réinitialisation de son état.");
				connexion.setEtatCommande(id_commande, "initiee");
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
			System.out.println("Envoi d'un message pour la commande "+id_commande+" facturée : "+commande_valide);
			
			Queue queue = (Queue) ictx.lookup("facturation");
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