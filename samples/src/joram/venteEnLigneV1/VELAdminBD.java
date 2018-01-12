package venteEnLigneV1;

import gestionBD.ConnexionFactory;
import gestionBD.ConnexionFactoryImpl;

import java.net.InetAddress;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.util.Hashtable;

import org.objectweb.joram.client.jms.admin.AdminModule;

/**
 * Administers an agent server for the classic samples.
 */
public class VELAdminBD
{
	public static void main(String[] args) throws Exception 
	{
		System.out.println();
		System.out.println("Mise à disposition de la ConnexionFactory.");

		AdminModule.connect("root", "root");
		
		ConnexionFactory cfBD = new ConnexionFactoryImpl();
		Registry registry = LocateRegistry.createRegistry(1099);
		Hashtable<Object,Object> env = new Hashtable();
		env.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.rmi.registry.RegistryContextFactory");
		env.put(javax.naming.Context.PROVIDER_URL, "rmi://" + InetAddress.getLocalHost().getHostAddress() + ":1099");
		
		javax.naming.Context jndiCtx = new javax.naming.InitialContext();
		jndiCtx.bind("env_rmi", env);
		
		//Enregistrement de l'ObjectFactory pour les connexions à la BD
		jndiCtx = new javax.naming.InitialContext(env);
		jndiCtx.bind("cfBD", cfBD);
		jndiCtx.close();

		AdminModule.disconnect();
		System.out.println("Administration terminée.");
	}
}
