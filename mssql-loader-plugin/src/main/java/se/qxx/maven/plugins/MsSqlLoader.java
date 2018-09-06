package se.qxx.maven.plugins;
 
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
 
/**
 * Says "Hi" to the user.
 *
 */
@Mojo( name = "load")
public class MsSqlLoader extends AbstractMojo
{
	static {
		System.loadLibrary("sqljdbc_auth.dll");
	}
	
    public void execute() throws MojoExecutionException
    {
        try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("Driver not found!");
			throw new MojoExecutionException("Driver not found");
		}
    }
}