package efluid;

import java.net.ConnectException;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminException;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

public class Admin {

	/**
	 * @param args
	 * @throws AdminException 
	 * @throws JMSException 
	 * @throws ConnectException 
	 * @throws NamingException 
	 */
	public static void main(String[] args) throws Exception {
		javax.jms.ConnectionFactory cf = TcpConnectionFactory.create("localhost", 16011);
	    AdminModule.connect(cf);
		InitialContext ictx = new InitialContext();
	    
	    ictx.rebind("JCF", cf);

	    User.create("anonymous", "anonymous");
	    
	    Queue queue = Queue.create("queue");
	    queue.setFreeReading();
	    queue.setFreeWriting();
	    ictx.bind("queue", queue);

	    ictx.close();
	    AdminModule.disconnect();
	}
}
