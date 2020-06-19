import org.junit.Test;
import org.neo4j.driver.v1.*;

public class Neo4jTest {

    @Test
    public void CypherTest(){
        Driver driver = GraphDatabase.driver("bolt://localhost:7687",
                AuthTokens.basic("neo4j","1234"));
        try(Session session = driver.session()){
            try(Transaction tx = session.beginTransaction()){
                tx.run("USING PERIODIC COMMIT 300\n" +
                        "LOAD CSV  WITH HEADERS FROM \"file:///relation.csv\" AS line\n" +
                        "MATCH (entity1:community{community:line.community}) , (entity2:house_type{title:line.house_type})\n" +
                        "CREATE (entity1)-[:户型 { type: line.relation }]->(entity2)");
                tx.success();
            }

        }
        driver.close();

    }
}
