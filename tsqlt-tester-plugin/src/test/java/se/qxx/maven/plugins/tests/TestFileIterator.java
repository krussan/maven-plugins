package se.qxx.maven.plugins.tests;

import se.qxx.maven.plugins.TSQLTTester;
import org.apache.maven.plugin.MojoExecutionException;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class TestFileIterator {
	@Test
	public void test_List_files() throws MojoExecutionException {
		TSQLTTester tester = new TSQLTTester();
		tester.setSrcPath("c:\\git\\NordaxDatabases\\AllNordaxLegacyDatabases\\DBApplication\\test");
		tester.setUrl("jdbc:sqlserver://localhost;databaseName=DBApplication");
		tester.setUsername("liquibase");
		tester.setPassword("liquibase");
		tester.setPreparationScripts( new String[]{
			"C:\\git\\NordaxDatabases\\tools\\tsqlt\\configureoptions.sql",
			"C:\\git\\NordaxDatabases\\tools\\tsqlt\\tSQLt.class.sql",
			"C:\\git\\NordaxDatabases\\tools\\tsqlt\\tSQLt.patches.sql"
		});
		tester.execute();
	}
}