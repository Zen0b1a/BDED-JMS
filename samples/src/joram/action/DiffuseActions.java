package action;

import java.util.Enumeration;

import javax.jms.*;
import javax.naming.*;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Scanner;
import java.util.Set;
import java.util.Properties;

import javax.jms.ConnectionFactory;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnectionFactory;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleStatement;

import oracle.jdbc.dcn.DatabaseChangeEvent;
import oracle.jdbc.dcn.DatabaseChangeListener;
import oracle.jdbc.dcn.DatabaseChangeRegistration;
import oracle.jdbc.dcn.RowChangeDescription;
import oracle.jdbc.dcn.TableChangeDescription;

public class DiffuseActions 
{
	public static void main(String[] args) throws Exception 
	{
		DiffuseActions da = new DiffuseActions();
	}

	public DiffuseActions() throws Exception 
	{
		System.out.println();
		System.out.println("Préparation des topics d'actions");
		
		//Hashtable avec le nom du topic, associé au topic
		Hashtable<String, Topic> topics = new Hashtable();

		ConnectionFactory cf = TcpConnectionFactory.create(InetAddress.getLocalHost().getHostAddress(), 16010);
		AdminModule.connect(cf, "root", "root");

		//Ouverture de la connexion
		OracleConnection connexion = Connexion.connect();
		
		//Récupération des différentes actions
		String nom_tables = "";
		Statement stmt = connexion.createStatement();
		ResultSet rs = stmt.executeQuery("select nom_action from action order by id");
		while(rs.next())
		{
			//Récupération du nom de l'action
			String nom_action = rs.getString(1).toLowerCase();
			nom_tables += nom_action+",";
			//Création du topic
			Topic topic = Topic.create(nom_action);
			topic.setFreeReading();
			topic.setFreeWriting();
			topics.put(nom_action, topic);
			System.out.println("Topic : "+nom_action);
		}
		rs.close();
		stmt.close();
		
		//Définition de l'adresse ip à utiliser (pour le listner)
		//Récupération des adresses
		Enumeration nifs = NetworkInterface.getNetworkInterfaces();
		List<String> ip_disponibles = new ArrayList();
		String ip = InetAddress.getLocalHost().getHostAddress();
		while (nifs.hasMoreElements()) 
		{
			NetworkInterface ni = (NetworkInterface)nifs.nextElement();
			if(ni.isUp() && !ni.isLoopback())
			{
				Enumeration ia = ni.getInetAddresses();
				while(ia.hasMoreElements())
				{
					InetAddress a = (InetAddress)ia.nextElement();
					if(!a.isLinkLocalAddress())
						ip_disponibles.add(a.getHostAddress());
				}
			}
		}
		if(ip_disponibles.size()>1)
		{
			String choix_ip = "";
			Scanner sc = new Scanner(System.in);
			for(int i=0; i<ip_disponibles.size(); i++)
				System.out.println(i+" - "+ip_disponibles.get(i));
			//Tant que l'option est invalide on redemande
			while(choix_ip.equals(""))
			{
				System.out.println("Choisir le numéro de l'adresse ip de la machine :");
				choix_ip = sc.nextLine();
				try
				{
					if(Integer.parseInt(choix_ip)<0 || Integer.parseInt(choix_ip)>=ip_disponibles.size())
						choix_ip = "";
					else
						choix_ip = ip_disponibles.get(Integer.parseInt(choix_ip));
				}
				catch(NumberFormatException ex)
				{
					choix_ip = "";
				}
			}
			ip = choix_ip;
		}
		
		//Propriétés du DatabaseChangeRegistration
		Properties prop = new Properties();
		prop.setProperty(OracleConnection.DCN_NOTIFY_ROWIDS, "true");
		prop.setProperty(OracleConnection.NTF_LOCAL_HOST, ip);
		DatabaseChangeRegistration dcr = connexion.registerDatabaseChangeNotification(prop);
		//Ajout du listener
		DCNBDListener listener = new DCNBDListener();
		dcr.addListener(listener);
		
		//Ajout des tables dans le listener
		stmt = connexion.createStatement();
		((OracleStatement)stmt).setDatabaseChangeRegistration(dcr);
		rs = stmt.executeQuery("select * from "+nom_tables.substring(0, nom_tables.length()-1));
		rs.close();
		stmt.close();

		User.create("anonymous", "anonymous");

		//Binding
		javax.naming.Context jndiCtx = new javax.naming.InitialContext();
		TopicConnectionFactory tcf = TcpConnectionFactory.create(InetAddress.getLocalHost().getHostAddress(), 16010);
		jndiCtx.bind("cf", cf);
		jndiCtx.bind("tcf", tcf);
		//Enregistrement de la liste des actions disponibles
		Set<String> keys = topics.keySet();
		jndiCtx.bind("liste_actions", keys.toArray(new String[keys.size()]));
		//Enregistrement des topics
        for(String key : keys)
			jndiCtx.bind(key, topics.get(key));

		AdminModule.disconnect();
		System.out.println("Topics d'actions prêts.");
		
		System.out.println("Appuyer sur une touche pour arrêter la diffusion.");
		System.in.read();
		//Désenregistrement du listener
		connexion.unregisterDatabaseChangeNotification(dcr);
		connexion.close();
		//Unbinding
		jndiCtx.unbind("cf");
		jndiCtx.unbind("tcf");
		jndiCtx.unbind("liste_actions");
		for(String key : keys)
			jndiCtx.unbind(key);
		jndiCtx.close();
		System.out.println("Topics désenregistrés.");
	}
}

class DCNBDListener implements DatabaseChangeListener
{
	static Context ictx = null; 
	
	DCNBDListener() throws Exception
	{
		ictx = new InitialContext();
		System.out.println("Listener lancé.");
	}
	
	@Override
	public void onDatabaseChangeNotification(DatabaseChangeEvent e)
	{
		System.out.println("Nouvelle action détectée.");
		try
		{
			//Récupération des tables concernées par l'event
			TableChangeDescription[] tables = e.getTableChangeDescription();
			RowChangeDescription[] tuples;
			OracleConnection connexion = Connexion.connect();
			Statement stmt = connexion.createStatement();
			for(int i=0; i<tables.length; i++)
			{
				//Récupération des tuples de la table concernée
				tuples = tables[i].getRowChangeDescription();
				for(int j=0; j<tuples.length; j++)
				{
					//Récupération du topic correspondant
					Topic topic = (Topic) ictx.lookup(tables[i].getTableName().toLowerCase().split("\\.")[1]);
					TopicConnectionFactory tcf = (TopicConnectionFactory) ictx.lookup("tcf");
					TopicConnection cnx = tcf.createTopicConnection();
					TopicSession session = cnx.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
					TopicPublisher publisher = session.createPublisher(topic);
					//Récupération du tuple
					ResultSet rs = stmt.executeQuery("SELECT * FROM "+tables[i].getTableName()+" WHERE rowid='"+tuples[j].getRowid().stringValue()+"'");
					while(rs.next())
					{
						//Envoi du message
						String message = "Action : "+rs.getInt(1)+", cours : "+rs.getDouble(2);
						System.out.println("Message envoyé sur topic "+tables[i].getTableName().toLowerCase().split("\\.")[1]+" : "+message);
						MapMessage msg = session.createMapMessage();
						msg.setInt("id", rs.getInt(1));
						msg.setDouble("valeur", rs.getDouble(2));
						publisher.publish(msg);
					}
					rs.close();
				}
			}
			stmt.close();
			connexion.close();
		}
		catch (Exception ex) 
		{
			Logger.getLogger(DCNBDListener.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
