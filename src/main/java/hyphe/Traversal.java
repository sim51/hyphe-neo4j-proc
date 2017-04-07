package hyphe;

import hyphe.exception.NodeFoundException;
import hyphe.result.NodeResult;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.traversal.Uniqueness;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.util.Iterator;
import java.util.stream.Stream;

public class Traversal {

	@Context
	public GraphDatabaseService graphDb;

	@Context
	public Log log;


	@Procedure(value = "hyphe.traverse", mode = Mode.READ)
	@Description("Find the first path")
	public Stream<NodeResult> search(@Name("start") Node start) {
		try {
			log.debug("Let's traverse !");
			// Create the traversal description
			for( Path path : this.graphDb
					.traversalDescription()
					.depthFirst()
					.relationships(RelationshipType.withName("PARENT"))
					.evaluator(new CustomEvaluator(this.log))
					.uniqueness(Uniqueness.NODE_GLOBAL)
					.traverse(start) ) {
				//need to consume the result to make the traverse
				log.debug(path.toString());
			}
		}
		catch(NodeFoundException e) {
			log.debug("Yeah !! => " + e.getNode().getId());
			return Stream.of(new NodeResult(e.getNode()));
		}

		return null;
	}


	class CustomEvaluator implements Evaluator {

		private Log log;

		public CustomEvaluator(Log log) {
			this.log = log;
		}

		@Override
		public Evaluation evaluate(Path path) {
			Node webEntity = null;

			if (path.endNode().hasLabel(Label.label("Stem"))) {
				log.debug(":Stem is present");
				Node endNode = path.endNode();
				Iterator<Relationship> iter = endNode.getRelationships(RelationshipType.withName("PREFIX"), Direction.OUTGOING).iterator();
				while (iter.hasNext()) {
					Relationship rel = iter.next();
					if (rel.getEndNode().hasLabel(Label.label("WebEntity"))) {
						webEntity = rel.getEndNode();
					}
				}

				if (webEntity != null) {
					log.debug(":WebEntity is found !!!");
					throw new NodeFoundException(webEntity);
				} else {
					log.debug(":WebEntity not found :( -> continue to parse the tree");
					return Evaluation.EXCLUDE_AND_CONTINUE;
				}
			} else {
				log.debug("Pruning this node due to :Stem not present");
				return Evaluation.EXCLUDE_AND_PRUNE;
			}

		}

	}
}
