package gestionBD;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnexionPool
{
    final private String LOGIN = "ag092850";
    final private String MDP = "ag092850";
    final private String URL_FAC = "jdbc:oracle:thin:@butor:1521:ensb2017";
    final private String URL_EXTERIEUR = "jdbc:oracle:thin:@ufrsciencestech.u-bourgogne.fr:25561:ensb2017";
    static final int INITIAL_CAPACITY = 10;
    private LinkedList<Connection> pool;
    
    private Connection choixConnexion()
    {
        Connection connexion = null;
        try
        {
            //Connexion depuis la fac
            connexion = DriverManager.getConnection(URL_FAC, LOGIN, MDP);
        }
        catch (SQLException ex) 
        {
            try 
            {
                //Connexion depuis l'extérieur
                connexion = DriverManager.getConnection(URL_EXTERIEUR, LOGIN, MDP);
            } 
            catch (SQLException ex1) 
            {
                Logger.getLogger(ConnexionPool.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
        return connexion;
    }
    
    public ConnexionPool()
    {
        try
        {
            this.pool = new LinkedList();
            Class.forName("oracle.jdbc.driver.OracleDriver");        
            for(int i=0; i<INITIAL_CAPACITY; i++)
            {
                this.pool.add(this.choixConnexion());
                System.out.println("Connexion "+i+" ouverte.");
            }
        } 
        catch (ClassNotFoundException ex)
        {
            Logger.getLogger(ConnexionPool.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public synchronized Connection getConnexion()
    {
        if(this.pool.isEmpty())
            this.pool.add(this.choixConnexion());
        return this.pool.pop();
    }
    
    public synchronized void returnConnexion(Connection connexion)
    {
        this.pool.push(connexion);
    }
}
