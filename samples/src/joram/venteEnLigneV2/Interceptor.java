package venteEnLigneV2;

import javax.jms.*;

import org.objectweb.joram.client.jms.MessageInterceptor;

import venteEnLigneV2.Connexion;

public class Interceptor implements MessageInterceptor 
{
	public Interceptor() 
	{
	}

	public void handle(Message message, Session session) 
	{
		try
		{
			if(message instanceof MapMessage && ((MapMessage)message).itemExists("service") && ((MapMessage)message).itemExists("id") && ((MapMessage)message).itemExists("valide"))
			{
				Connexion connexion = new Connexion();
				MapMessage msg = (MapMessage)message;
				String service = msg.getString("service");
				int id_commande = msg.getInt("id");
				boolean commande_valide = msg.getBoolean("valide");
				
				if("preparation".equals(service))
				{					
					if(commande_valide)					
						//Changement du stock et de l'état de la commande
						commande_valide = connexion.majStockPreparation(id_commande) && connexion.setEtatCommande(id_commande, "preparee");
						
					if(!commande_valide)
					{
						msg.setBoolean("valide", false);
						connexion.razCommande(id_commande);
					}
				}
				else if("facturation".equals(service))
				{
					if(commande_valide)
						connexion.setEtatCommande(id_commande, "payee");
					else
					{
						msg.setBoolean("valide", false);
						connexion.razCommande(id_commande);
					}
				}
				else if("expedition".equals(service))
				{
					if(commande_valide)
						connexion.setEtatCommande(id_commande, "expediee");
					else
					{
						msg.setBoolean("valide", false);
						connexion.razCommande(id_commande);
					}
				}
				connexion.closeConnexion();
			}
		}
		catch(Exception ex)
		{
			System.out.println(ex);
		}
	}
}
