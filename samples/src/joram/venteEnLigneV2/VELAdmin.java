package venteEnLigneV2;

import java.net.InetAddress;

import java.util.Properties;

import org.objectweb.joram.client.jms.ConnectionFactory;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.ClusterConnectionFactory;
import org.objectweb.joram.client.jms.admin.ClusterQueue;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

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
		
		ClusterConnectionFactory cf = new ClusterConnectionFactory();
		cf.addConnectionFactory("server1", cf1);
		cf.addConnectionFactory("server2", cf2);
		cf.addConnectionFactory("server3", cf3);
		cf.addConnectionFactory("server4", cf4);
		//cf1.getParameters().addOutInterceptor("venteEnLigneV2.Interceptor");
		cf2.getParameters().addOutInterceptor("venteEnLigneV2.Interceptor");
		cf3.getParameters().addOutInterceptor("venteEnLigneV2.Interceptor");
		cf4.getParameters().addOutInterceptor("venteEnLigneV2.Interceptor");

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
		prop.setProperty("waitAfterClusterReq","0");
		
		//Queue validation->préparation
		Queue validation1 = Queue.create(1, "validation1", Queue.CLUSTER_QUEUE, prop);
		Queue validation2 = Queue.create(2, "validation2", Queue.CLUSTER_QUEUE, prop);
		validation1.addClusteredQueue(validation2);
		ClusterQueue validation = new ClusterQueue();
		validation.addDestination("server1", validation1);
		validation.addDestination("server2", validation2);
		validation.setWriter(validationUser);
		validation.setReader(preparationUser);
		System.out.println("Cluster de queues de validation créée.");
		
		//Queue préparation->facturation
		Queue preparation2 = Queue.create(2, "preparation2", Queue.CLUSTER_QUEUE, prop);
		Queue preparation3 = Queue.create(3, "preparation3", Queue.CLUSTER_QUEUE, prop);
		preparation2.addClusteredQueue(preparation3);
		ClusterQueue preparation = new ClusterQueue();
		preparation.addDestination("server2", preparation2);
		preparation.addDestination("server3", preparation3);
		preparation.setWriter(preparationUser);
		preparation.setReader(facturationUser);
		System.out.println("Cluster de queues de préparation créée.");
		
		//Queue facturation->expédition
		Queue facturation3 = Queue.create(3, "facturation3", Queue.CLUSTER_QUEUE, prop);
		Queue facturation4 = Queue.create(4, "facturation4", Queue.CLUSTER_QUEUE, prop);
		facturation3.addClusteredQueue(facturation4);
		ClusterQueue facturation = new ClusterQueue();
		facturation.addDestination("server3", facturation3);
		facturation.addDestination("server4", facturation4);
		facturation.setWriter(facturationUser);
		facturation.setReader(expeditionUser);
		System.out.println("Cluster de queues de facturation créée.");

		//Queue expédition
		Queue expedition = Queue.create(4);
		expedition.setWriter(expeditionUser);
		
		javax.naming.Context jndiCtx = new javax.naming.InitialContext();
		jndiCtx.bind("cf", cf);
		
		jndiCtx.bind("validation", validation);
		jndiCtx.bind("preparation", preparation);
		jndiCtx.bind("facturation", facturation);
		jndiCtx.bind("expedition", expedition);
		jndiCtx.close();

		AdminModule.disconnect();
		System.out.println("Admin closed.");
	}
}
