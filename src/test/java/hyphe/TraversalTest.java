package hyphe;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.driver.v1.*;
import org.neo4j.harness.junit.Neo4jRule;

import java.io.File;


public class TraversalTest {

	// This rule starts a Neo4j instance for us
	@Rule
	public Neo4jRule neo4j = new Neo4jRule()
			// This is the Procedure we want to test
			.withProcedure( Traversal.class )
			.copyFrom(new File("src/test/resources/graph.db"));

	@Test
	public void search_should_return() throws Throwable {
		// In a try-block, to make sure we close the driver and session after the test
		try(Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.build()
				.withEncryptionLevel( Config.EncryptionLevel.NONE ).toConfig() );
				Session session = driver.session() )
		{
			StatementResult rsExpected = session.run(""
					+ "MATCH (sourcePage:Page)\n"
					+ "MATCH path = (sourcePage)-[:PARENT*0..]->(:Stem)-[:PREFIX]->(:WebEntity)\n"
					+ "WITH sourcePage, path\n"
					+ "ORDER BY length(path) ASC\n" + "RETURN  sourcePage, head(collect(last(nodes(path)))) AS sourceWe");

			StatementResult rs = session.run("MATCH (sourcePage:Page) WITH sourcePage CALL hyphe.traverse(sourcePage) YIELD node RETURN sourcePage, node");

			Assert.assertEquals(rsExpected.list().size(), rs.list().size());
		}
	}
}
