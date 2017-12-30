package efluid;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

public class Test {
	public static void main(String[] args) throws Exception {
		ConnectionFactory cf = (ConnectionFactory) TcpConnectionFactory.create("localhost", 16010);
		Connection cnx = cf.createConnection();
		Session session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Destination queue = session.createQueue("queue1");
		MessageProducer prod = session.createProducer(queue);
		cnx.start();

		int i;
		for (i = 0; i < 10; i++) {
			TextMessage msg = session.createTextMessage("Test number " + i);
			prod.send(msg);
                        Thread.sleep(100L);
		}
		System.out.println(i + " messages sent.");
		cnx.close();
	}

	public static void main2(String[] args) throws Exception {
		InitialContext ictx = new InitialContext();
		
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
		MessageProducer prod = sess1.createProducer(null);
//		QueueSender qs = (QueueSender) prod;
		cnx.start();

		int i;
		for (i = 0; i < 10; i++) {
			TextMessage msg = sess1.createTextMessage("Test number " + i);
			prod.send(queue, msg, msg.getJMSDeliveryMode(), msg.getJMSPriority(), msg.getJMSExpiration());
                        Thread.sleep(1000L);
//			prod.send(msg);
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
