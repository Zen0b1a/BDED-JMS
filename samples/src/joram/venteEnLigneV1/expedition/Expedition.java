package venteEnLigneV1.expedition;

import gestionBD.Connexion;
import gestionBD.ConnexionFactory;

import java.util.Hashtable;

import javax.jms.*;
import javax.naming.*;

public class Expedition
{
	public static void main (String argv[]) throws Exception
	{
		new Expedition();
	}
	
	public Expedition() throws Exception
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
		
		//Queue pour recevoir les messages du service facturation
		QueueConnectionFactory qcf = (QueueConnectionFactory) ictx.lookup("qcf");
		QueueConnection cnx = qcf.createQueueConnection("expedition", "expedition");
		QueueSession session = cnx.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
		Queue queue = (Queue) ictx.lookup("facturation");
		QueueReceiver receiver = session.createReceiver(queue);
		receiver.setMessageListener(new ExpeditionListener(connexion));
		cnx.start();
		
		System.in.read();
		
		//Fermeture
		cfBD.libereConnexion(connexion);
		ictx.close();
		cnx.close();
	}
}

class ExpeditionListener implements MessageListener 
{
	private Connexion connexion;
	
	public ExpeditionListener(Connexion connexion)
	{
		this.connexion = connexion;
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
					System.out.println("Commande "+id_commande+" reçue.");

					//Changement de l'état de la commande
					if(commande_valide)
					{
						commande_valide = this.connexion.setEtatCommande(id_commande, "expediee");
						System.out.println("Commande expédiée.");
					}
					else
						this.connexion.razCommande(id_commande);
				}
				else
				{
					System.out.println("Commande "+id_commande+" invalide : réinitialisation de son état.");
					this.connexion.razCommande(id_commande);
				}

				System.out.println("En attente d'une commande à expédier.");
			}
		}
		catch(Exception ex)
		{
			System.out.println(ex);
		}
	}
}