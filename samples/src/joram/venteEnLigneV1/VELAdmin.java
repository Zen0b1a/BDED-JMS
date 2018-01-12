package venteEnLigneV1;

import gestionBD.ConnexionFactory;
import gestionBD.ConnexionFactoryImpl;

import java.net.InetAddress;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.util.Hashtable;

import javax.jms.ConnectionFactory;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnectionFactory;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

/**
 * Administers an agent server for the classic samples.
 */
public class VELAdmin 
{
	public static void main(String[] args) throws Exception 
	{
		System.out.println();
		System.out.println("Vente en ligne administration...");

		ConnectionFactory cf = TcpConnectionFactory.create(InetAddress.getLocalHost().getHostAddress(), 16010);
		AdminModule.connect(cf, "root", "root");

		//Définition des utilisateurs
		User validationUser = User.create("validation", "validation", 0);
		User preparationUser = User.create("preparation", "preparation", 0);
		User facturationUser = User.create("facturation", "facturation", 0);
		User expeditionUser = User.create("expedition", "expedition", 0);
		
		//Queue validation->préparation
		Queue validation = Queue.create("validation");
		validation.setReader(preparationUser);
		validation.setWriter(validationUser);
		//Queue préparation->facturation
		Queue preparation = Queue.create("preparation");
		preparation.setReader(facturationUser);
		preparation.setWriter(preparationUser);
		//Queue facturation->expédition
		Queue facturation = Queue.create("facturation");
		facturation.setReader(expeditionUser);
		facturation.setWriter(facturationUser);

		QueueConnectionFactory qcf = TcpConnectionFactory.create(InetAddress.getLocalHost().getHostAddress(), 16010);
		
		ConnexionFactory cfBD = new ConnexionFactoryImpl();
		Registry registry = LocateRegistry.createRegistry(1099);
		Hashtable<Object,Object> env = new Hashtable();
		env.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.rmi.registry.RegistryContextFactory");
		env.put(javax.naming.Context.PROVIDER_URL, "rmi://" + InetAddress.getLocalHost().getHostAddress() + ":1099");
		
		javax.naming.Context jndiCtx = new javax.naming.InitialContext();
		jndiCtx.bind("env_rmi", env);
		jndiCtx.bind("cf", cf);
		jndiCtx.bind("qcf", qcf);
		jndiCtx.bind("validation", validation);
		jndiCtx.bind("preparation", preparation);
		jndiCtx.bind("facturation", facturation);
		
		//Enregistrement de l'ObjectFactory pour les connexions à la BD
		jndiCtx = new javax.naming.InitialContext(env);
		jndiCtx.bind("cfBD", cfBD);
		jndiCtx.close();

		AdminModule.disconnect();
		System.out.println("Admin terminée.");
	}
}
