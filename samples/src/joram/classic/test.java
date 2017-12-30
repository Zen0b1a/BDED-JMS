package classic;

import java.util.Enumeration;

import javax.jms.*;
import javax.naming.*;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

public class test {
	public static void main(String[] args) throws Exception {
		admin();
		send();
		receive();
	}

	public static void admin() throws Exception {
		System.out.println();
		System.out.println("Classic administration...");

		ConnectionFactory cf = TcpConnectionFactory.create("localhost", 16010);
		AdminModule.connect(cf, "root", "root");

		Queue queue = Queue.create("queue");
		queue.setFreeReading();
		queue.setFreeWriting();
		Topic topic = Topic.create("topic");
		topic.setFreeReading();
		topic.setFreeWriting();

		User.create("anonymous", "anonymous");


		javax.naming.Context jndiCtx = new javax.naming.InitialContext();
		jndiCtx.bind("cf", cf);
		jndiCtx.bind("queue", queue);
		jndiCtx.bind("topic", topic);
		jndiCtx.close();

		AdminModule.disconnect();
		System.out.println("Admin closed.");
	}

	public static void send() throws Exception {
		InitialContext ictx = new InitialContext();
		Destination dest = (Destination) ictx.lookup("queue");
		ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
		ictx.close();

		Connection cnx = cf.createConnection();
		Session sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
		MessageProducer producer = sess.createProducer(dest);

		int i;
		for (i = 0; i < 10; i++) {
			TextMessage msg = sess.createTextMessage("Test number " + i);
			producer.send(msg);
		}

		System.out.println(i + " messages sent.");

		cnx.close();
	}

	public static void receive() throws Exception {
		System.out.println("Listens to queue");

		InitialContext ictx = new InitialContext();
		Destination dest = (Destination) ictx.lookup("queue");
		ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
		ictx.close();

		Connection cnx = cf.createConnection();
		Session sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
		MessageConsumer recv = sess.createConsumer(dest);

		recv.setMessageListener(new XXListener("Listener on queue"));

		cnx.start();

		System.in.read();
		cnx.close();

		System.out.println("Consumer closed.");
	}
}

class XXListener implements MessageListener {
	private String ident = null;

	public XXListener() {
		ident = "listener";
	}

	public XXListener(String ident) {
		this.ident = ident;
	}

	public void onMessage(Message msg) {
		try {
			Destination destination = msg.getJMSDestination();
			Destination replyTo = msg.getJMSReplyTo();

			System.out.println(ident + " receives message from=" + destination + ",replyTo=" + replyTo);
			Enumeration e = msg.getPropertyNames();
			while (e.hasMoreElements()) {
				String key = (String) e.nextElement();
				String value = msg.getStringProperty(key);
				System.out.println("\t" + key + " = " + value);
			}

			if (msg instanceof TextMessage) {
				System.out.println(ident + ": " + ((TextMessage) msg).getText());
			}
		} catch (JMSException jE) {
			System.err.println("Exception in listener: " + jE);
		}
	}
}
