package venteEnLigneV1.expedition;

import gestionBD.Connexion;
import gestionBD.ConnexionFactory;

import java.util.Hashtable;

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
		
		//Queue pour recevoir les messages du service facturation
		QueueConnectionFactory qcf = (QueueConnectionFactory) ictx.lookup("qcf");
		QueueConnection cnx = qcf.createQueueConnection("expedition", "expedition");
		QueueSession session = cnx.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
		Queue queue = (Queue) ictx.lookup("facturation");
		QueueReceiver receiver = session.createReceiver(queue);
		
		cnx.start();
		
		while(continuer)
		{
			System.out.println("En attente d'une commande à expédier.");
			MapMessage msg = (MapMessage)receiver.receive();
			System.out.println("Message reçu.");
			//Extraction de l'id de la commande, et du boolean de validation
			id_commande = msg.getInt("id");
			commande_valide = msg.getBoolean("valide");
			
			if(commande_valide)
			{
				System.out.println("Commande "+id_commande+" reçue, appuyer sur une touche pour l'expédier.");
				System.in.read();

				//Changement de l'état de la commande
				if(commande_valide)
				{
					commande_valide = connexion.setEtatCommande(id_commande, "expediee");
					System.out.println("Commande expédiée.");
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
}