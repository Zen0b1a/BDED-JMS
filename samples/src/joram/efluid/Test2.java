package efluid;

import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

public class Test2 {

	public static void main(String[] args) throws NamingException, JMSException {
		Properties props = new Properties();
//		props.setProperty("java.naming.factory.host", "192.168.1.109");
//		props.setProperty("java.naming.factory.port", "1099");
		props.setProperty("java.naming.factory.initial", "org.ow2.carol.jndi.spi.MultiOrbInitialContextFactory");

		
//		props.setProperty("carol.protocols", "jrmp");
//		props.setProperty("carol.jrmp.url", "rmi://192.168.1.106:1099");
//		props.setProperty("carol.jvm.rmi.local.registry", "true");
//		props.setProperty("providerURL", "jrmp://192.168.1.106:1099");

//		props.setProperty("initialContextFactory", "org.ow2.carol.jndi.spi.JRMPContextWrapperFactory");
//		props.setProperty("providerURL", "jrmp://192.168.1.106:1099");

		props.setProperty("initialContextFactory", "org.ow2.carol.jndi.spi.MultiOrbInitialContextFactory");
		props.setProperty("providerURL", "rmi://localhost:1099");
		
//		props.put(Context.INITIAL_CONTEXT_FACTORY, "org.ow2.carol.jndi.spi.MultiOrbInitialContextFactory");
//		props.put(Context.PROVIDER_URL, "rmi://192.168.1.80:1099");
//		
//		InitialContext ictx = new InitialContext(props);
//		
//		Destination queue1 = (Destination) ictx.lookup("queue1");
//		Destination queue2 = (Destination) ictx.lookup("queue2");
//		ConnectionFactory cf = (ConnectionFactory) ictx.lookup("JCF");
//		ictx.close();
		
		InitialContext ictx = new InitialContext(props);
		
		Destination queue = (Destination) ictx.lookup("queue");
		ConnectionFactory cf = (ConnectionFactory) ictx.lookup("JCF");
		ictx.close();

		System.out.println("JNDI lookup ok");
		
//		Connection cnx = cf.createConnection();
//		Session sess1 = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
//		MessageProducer prod = sess1.createProducer(queue1);
//		MessageConsumer recv = sess1.createConsumer(queue2);
//		cnx.start();
		
//		InitialContext ictx = new InitialContext();
//		Destination queue1 = (Destination) ictx.lookup("queue1");
//		Destination queue2 = (Destination) ictx.lookup("queue2");
//		ConnectionFactory cf = (ConnectionFactory) ictx.lookup("JCF");
//		ictx.close();
//
//		Connection cnx = cf.createConnection();
//		Session sess1 = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
//		Session sess2 = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
//		MessageProducer prod = sess2.createProducer(queue1);
//		MessageConsumer recv = sess2.createConsumer(queue2);
//		cnx.start();
		
		Connection cnx = cf.createConnection();
		Session sess1 = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
		MessageProducer prod = sess1.createProducer(queue);
		cnx.start();

		int i;
		for (i = 0; i < 10; i++) {
			TextMessage msg = sess1.createTextMessage("Test number " + i);
			prod.send(msg);
		}
		System.out.println(i + " messages sent.");

//		for (i = 0; i < 10; i++) {
//			Message msg = recv.receive();
//			if (msg instanceof TextMessage)
//				System.out.println("Msg received: " + ((TextMessage) msg).getText());
//			else
//				System.out.println("Msg received: " + msg);
//		}
//		System.out.println("10 messages received.");

		cnx.close();
	}
}
