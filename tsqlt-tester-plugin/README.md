# TSQLt tester plugin

A plugin to test a database with tSQLt

## Example configuration:
```
<plugin>
  <groupId>se.qxx.maven.plugins</groupId>
  <artifactId>tsqlt-tester-plugin</artifactId>
  <version>0.5</version>

  <configuration>
	<url>jdbc:sqlserver://${database.server};databaseName=${database.name}</url>			
	<username>${database.username}</username>
	<password>${database.password}</password>
	<srcpath>${project.basedir}/test</srcpath>
	<driver>com.microsoft.sqlserver.jdbc.SQLServerDriver</driver>
	<preparationScripts>
		<param>${project.basedir}/tsqlt/configureoptions.sql</param>
		<param>${project.basedir}/tsqlt/tSQLt.class.sql</param>
		<param>${project.basedir}/tsqlt/tSQLt.patches.sql</param>
	</preparationScripts>
	<srcPath>${project.basedir}/test</srcPath>
  </configuration>
  <executions>
	<execution>
	  <id>tsqlt-test</id>
	  <phase>test</phase>
	  <goals>
		<goal>test</goal>
	  </goals>			  
	</execution>
  </executions>
  
  <dependencies>
	<dependency>
	  <groupId>com.microsoft.sqlserver</groupId>
	  <artifactId>sqljdbc4</artifactId>
	  <version>4.1</version>
	  <scope>system</scope>
	  <systemPath>${project.basedir}/sqljdbc41.jar</systemPath>
	</dependency>
  </dependencies>		  
</plugin>
```

## Example execution:
```
mvn test -Ddatabase.server=localhost -Ddatabase.username=liquibase -Ddatabase.password=liquibase
```