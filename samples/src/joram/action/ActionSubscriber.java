package action;

import java.util.Scanner;
import javax.jms.*;
import javax.naming.*;

import java.util.List;
import java.util.Set;

/**
 * Subscribes and sets a listener to the topic.
 */
public class ActionSubscriber 
{
  static Context ictx = null; 

  public static void main(String[] args) throws Exception {
    System.out.println("Subscribes and listens to the topic...");

    ictx = new InitialContext();
	
	//Récupération des topics disponibles
	String[] liste_actions = (String[]) ictx.lookup("liste_actions");
	for(int i=0; i<liste_actions.length; i++)
		System.out.println(i+" - "+liste_actions[i]);
	
	//Choix du topic
	Scanner sc = new Scanner(System.in);
	String nom_topic = "";
	//Tant que l'option est invalide on redemande
	while(nom_topic.equals(""))
	{
		System.out.println("Choisir le numéro du topic voulu :");
		nom_topic = sc.nextLine();
		try
		{
			if(Integer.parseInt(nom_topic)<0 || Integer.parseInt(nom_topic)>=liste_actions.length)
				nom_topic = "";
			else
				nom_topic = liste_actions[Integer.parseInt(nom_topic)];
		}
		catch(NumberFormatException ex)
		{
			nom_topic = "";
		}
	}
	
	//Récupération du topic choisi
	System.out.println(nom_topic+" choisi.");
    Topic topic = (Topic) ictx.lookup(nom_topic);
    TopicConnectionFactory tcf = (TopicConnectionFactory) ictx.lookup("tcf");
    ictx.close();

    TopicConnection cnx = tcf.createTopicConnection();
    TopicSession session = cnx.createTopicSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
    TopicSubscriber subscriber = session.createSubscriber(topic);
    subscriber.setMessageListener(new MsgListener());

    cnx.start();

    System.in.read();

    cnx.close();

    System.out.println("Subscription closed.");
  }
}
