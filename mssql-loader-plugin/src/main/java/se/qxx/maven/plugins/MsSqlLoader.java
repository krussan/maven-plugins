package se.qxx.maven.plugins;
 
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Mojo( name = "load")
public class MsSqlLoader extends AbstractMojo
{
	
	@Parameter( property = "load.url" )
	private String url;
	
    public void execute() throws MojoExecutionException
    {
		System.loadLibrary("sqljdbc_auth");
		
        try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			
			Connection con=DriverManager.getConnection(url);  
			
			Statement stmt=con.createStatement();  
			ResultSet rs=stmt.executeQuery("select 1");  
		} catch (SQLException sqlex) {
			throw new MojoExecutionException("Connection failed");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("Driver not found!");
			throw new MojoExecutionException("Driver not found");
		}
    }
}