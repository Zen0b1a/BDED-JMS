package venteEnLigneV2;

import java.net.InetAddress;

import java.util.Properties;

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

		AdminModule.connect("root", "root");
		
		ConnectionFactory cf1 = TcpConnectionFactory.create(InetAddress.getLocalHost().getHostAddress(), 16011);
		ConnectionFactory cf2 = TcpConnectionFactory.create(InetAddress.getLocalHost().getHostAddress(), 16012);
		ConnectionFactory cf3 = TcpConnectionFactory.create(InetAddress.getLocalHost().getHostAddress(), 16013);
		ConnectionFactory cf4 = TcpConnectionFactory.create(InetAddress.getLocalHost().getHostAddress(), 16014);

		//Définition des utilisateurs
		User validationUser = User.create("validation", "validation", 1);
		User preparationUser = User.create("preparation", "preparation", 2);
		User facturationUser = User.create("facturation", "facturation", 3);
		User expeditionUser = User.create("expedition", "expedition", 4);
		
		Properties prop = new Properties();
		prop.setProperty("period","100");
		prop.setProperty("producThreshold","1000");
		prop.setProperty("consumThreshold","5");
		prop.setProperty("autoEvalThreshold","false");
		prop.setProperty("waitAfterClusterReq","10000");
		
		//Queue validation->préparation
		Queue validation1 = Queue.create(1, "validation1", Queue.CLUSTER_QUEUE, prop);
		validation1.setWriter(validationUser);
		Queue validation2 = Queue.create(2, "validation2", Queue.CLUSTER_QUEUE, prop);
		validation2.setReader(preparationUser);
		validation1.addClusteredQueue(validation2);
		
		//Queue préparation->facturation
		Queue preparation2 = Queue.create(2, "preparation2", Queue.CLUSTER_QUEUE, prop);
		preparation2.setWriter(preparationUser);
		Queue preparation3 = Queue.create(3, "preparation3", Queue.CLUSTER_QUEUE, prop);
		preparation3.setReader(facturationUser);
		preparation2.addClusteredQueue(preparation3);
		
		//Queue facturation->expédition
		Queue facturation3 = Queue.create(3, "facturation3", Queue.CLUSTER_QUEUE, prop);
		facturation3.setWriter(facturationUser);
		Queue facturation4 = Queue.create(4, "facturation4", Queue.CLUSTER_QUEUE, prop);
		facturation4.setReader(expeditionUser);
		facturation3.addClusteredQueue(facturation4);

		javax.naming.Context jndiCtx = new javax.naming.InitialContext();
		jndiCtx.bind("cf1", cf1);
		jndiCtx.bind("cf2", cf2);
		jndiCtx.bind("cf3", cf3);
		jndiCtx.bind("cf4", cf4);
		
		jndiCtx.bind("validation1", validation1);
		jndiCtx.bind("validation2", validation2);
		jndiCtx.bind("preparation2", preparation2);
		jndiCtx.bind("preparation3", preparation3);
		jndiCtx.bind("facturation3", facturation3);
		jndiCtx.bind("facturation4", facturation4);
		jndiCtx.close();

		AdminModule.disconnect();
		System.out.println("Admin closed.");
	}
}
