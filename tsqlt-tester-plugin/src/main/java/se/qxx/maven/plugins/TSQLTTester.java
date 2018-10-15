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
import java.sql.CallableStatement;
import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.Writer;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLWarning;
import java.sql.ResultSet;

@Mojo( name = "test")
public class TSQLTTester extends AbstractMojo
{
	private static final String DEFAULT_DELIMITER = "\nGO";

	@Parameter( property = "url", required = true )
	public String url;

	@Parameter( property = "username", required = true )
	public String username;

	@Parameter( property = "password" , required = true)
	public String password;
	
	@Parameter( property = "srcpath", required = true )
	private String srcPath;
	
	@Parameter( property = "recurse" )
	private boolean recurse = true;
	
	@Parameter( property = "debug" )
	private boolean debug = false;
	
	@Parameter( property = "resultFile" )
	private String resultFile;
	
	@Parameter( property = "delimiter" )
	private String delimiter = DEFAULT_DELIMITER;
	
	@Parameter( property = "preparationScripts" )
	private String[] preparationScripts;
	
	@Parameter( property = "driver" )
	private String driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

	
	public void setUrl(String url) {
		this.url = url;
	}

	public void setSrcPath(String srcPath) {
		this.srcPath = srcPath;
	}
	
	public void setRecurse(boolean recurse) {
		this.recurse = recurse;
	}	
	
	public void setResultFile(String resultFile) {
		this.resultFile = resultFile;
	}	
	
	public void setUsername(String username) {
		this.username = username;
	}	

	public void setPassword(String password) {
		this.password = password;
	}	

	public void setPreparationScripts(String[] scripts) {
		this.preparationScripts = scripts;
	}	
	
	public void setDebug(boolean debug) {
		this.debug = debug;
	}		
	
    public void execute() throws MojoExecutionException
    {
		// Loop through each sql file in the srcPath directory and get content
		// parse away run statements
		// run command against an sql connection url
		try {
			Class.forName(this.driver);
			
			File folder = new File(this.srcPath);
			executeFiles(this.preparationScripts, false);
			
			executeFiles(
				listFilesForFolder(folder, "sql"),
				true);
				
			test();
				
		} catch (TSQLTFailedException tsqltEx) {
			throw new MojoExecutionException("TSQLT tests failed !!");
		}
		catch (IOException | SQLException ex) {
			ex.printStackTrace();
			throw new MojoExecutionException("Tsqlt executer failed", ex);
		}
		catch (ClassNotFoundException cex) {
			cex.printStackTrace();
			throw new MojoExecutionException("JDBC driver failed to initialize", cex);
		}
    }
	
	public void test() throws SQLException, TSQLTFailedException {
		int failCount = 0;
		
		Connection conn = DriverManager.getConnection(this.url, this.username, this.password);
		System.out.println("Running all tests ....");
		try (Statement stmt = conn.createStatement()) {
			
			stmt.execute("EXEC tSQLt.RunAll");

			ResultSet rs = stmt.executeQuery("SELECT [Name], [Result], [Msg] FROM tSQLt.TestResult");
			while (rs.next()) {
				String result = rs.getString("Result");
				String msg = rs.getString("Msg");
				String testName = rs.getString("Name");
				
				System.out.println(String.format("%s %s %s", testName, result, msg));
			}
			
			
			rs = stmt.executeQuery("SELECT Msg, FailCnt FROm tSQLt.TestCaseSummary()");
			if (rs.next()) {
				String msg = rs.getString("Msg");
				failCount = rs.getInt("FailCnt");
				
				System.out.println("");
				System.out.println(msg);
			}
			
			// Write result file
			if (this.resultFile != null && !this.resultFile.equals("")) {
				//Statement cStmt = conn.prepareCall("tSQLt.XmlResultFormatter");

				boolean hasResult = stmt.execute("EXEC tSQLt.XmlResultFormatter");
				//boolean hasResult = cStmt.execute();
				if (hasResult) {
					System.out.println(String.format("Writing result file %s", this.resultFile));
					rs = stmt.getResultSet();
					
					if (rs.next()) {
						String msg = rs.getString(1);
					
						try (Writer writer = new BufferedWriter(new OutputStreamWriter(
						  new FileOutputStream(this.resultFile), "utf-8"))) {
						   writer.write(msg);
						}
						catch (IOException ex) {
						  System.out.println("[ERROR] Writing result file failed");
						}
					}
				}
			}
			
			
		} catch (SQLException ex) {
			throw ex;
		}
		
		if (failCount > 0)
			throw new TSQLTFailedException();
	}
	
	public List<File> listFilesForFolder(File folder, String extension) {
		List<File> result = new ArrayList<File>();
		for (File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory() && this.recurse) {
				result.addAll(listFilesForFolder(fileEntry, extension));
			} else {
				if (fileEntry.getName().endsWith(extension)) {
					result.add(fileEntry);
				}
			}
		}
		
		return result;
	}
	
	public void executeFiles(List<File> files, boolean parseAwayTests) throws SQLException, IOException {
		for (File f : files) {
			deployFile(
				parseFile(
					readFile(f),
					parseAwayTests));
		}
	}
	
	public void executeFiles(String[] paths, boolean parseAwayTests) throws SQLException, IOException {
		if (paths != null) {
			for (String filePath : paths) {
				deployFile(
					parseFile(
						readFile(filePath),
						parseAwayTests));
			}
		}
	}	
	
	public void deployFile(String[] statements) throws SQLException {

		Connection conn = DriverManager.getConnection(this.url, this.username, this.password);
		try (Statement stmt = conn.createStatement()) {
			for (String statement : statements) {
				stmt.execute(statement);
				
				
				if (this.debug) {
					System.out.println(statement);
					System.out.println("------------------------------------------------------------");
				}
				
				SQLWarning warning = stmt.getWarnings();
				while (warning != null) {
					System.out.println(warning.getMessage());
					warning = warning.getNextWarning();
				}
			}
		} catch (SQLException ex) {
			throw ex;
		}
	}
	
	public String[] parseFile(String input, boolean parseAwayTests) {
		if (parseAwayTests)
			input = parseAwayTests(input);
		
		Pattern pattern = Pattern.compile(this.delimiter, Pattern.CASE_INSENSITIVE);
		String[] result = pattern.split(input);
		
		return result;
	}
	
	public String parseAwayTests(String input) {
		Pattern pattern = Pattern.compile("^\\s*EXEC(UTE)?\\s*\\[?tSQLt\\]?\\.\\[?Run\\]?\\s*\\'[^\\']*\\'.*?$", Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
		Matcher matcher = pattern.matcher(input);
		return matcher.replaceAll("");
	}
	
	public String readFile(File f) throws IOException {
		// Create a file path of the file
		Path filePath = Paths.get(f.getAbsolutePath());
		return readFile(filePath);
	}
	
	public String readFile(String filePath) throws IOException {
		Path path = Paths.get(filePath);
		
		// Reading and joining all lines in a file into a string
		return readFile(path);
	}
	
	public String readFile(Path filePath) throws IOException {
		// Reading and joining all lines in a file into a string
		return Files.lines(filePath, StandardCharsets.UTF_8)
			.collect(Collectors.joining("\n"));
	}
	
}