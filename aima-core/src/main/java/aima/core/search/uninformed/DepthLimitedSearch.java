package aima.core.search.uninformed;

import java.util.List;
import java.util.function.Consumer;

import aima.core.agent.Action;
import aima.core.search.framework.Metrics;
import aima.core.search.framework.Node;
import aima.core.search.framework.NodeExpander;
import aima.core.search.framework.SearchForActions;
import aima.core.search.framework.SearchForStates;
import aima.core.search.framework.SearchUtils;
import aima.core.search.framework.problem.Problem;
import aima.core.util.CancelableThread;

/**
 * Artificial Intelligence A Modern Approach (3rd Edition): Figure 3.17, page
 * 88.<br>
 * <br>
 * 
 * <pre>
 * function DEPTH-LIMITED-SEARCH(problem, limit) returns a solution, or failure/cutoff
 *   return RECURSIVE-DLS(MAKE-NODE(problem.INITIAL-STATE), problem, limit)
 *   
 * function RECURSIVE-DLS(node, problem, limit) returns a solution, or failure/cutoff
 *   if problem.GOAL-TEST(node.STATE) then return SOLUTION(node)
 *   else if limit = 0 then return cutoff
 *   else
 *       cutoff_occurred? &lt;- false
 *       for each action in problem.ACTIONS(node.STATE) do
 *           child &lt;- CHILD-NODE(problem, node, action)
 *           result &lt;- RECURSIVE-DLS(child, problem, limit - 1)
 *           if result = cutoff then cutoff_occurred? &lt;- true
 *           else if result != failure then return result
 *       if cutoff_occurred? then return cutoff else return failure
 * </pre>
 * 
 * Figure 3.17 A recursive implementation of depth-limited search.
 * 
 * @author Ravi Mohan
 * @author Ciaran O'Reilly
 * @author Mike Stampone
 * @author Ruediger Lunde
 */
public class DepthLimitedSearch<S, A> implements SearchForActions<S, A>, SearchForStates<S, A> {

	public static final String METRIC_NODES_EXPANDED = "nodesExpanded";
	public static final String METRIC_PATH_COST = "pathCost";

	public final Node<S, A> cutoffNode = new Node<>(null);
	private final int limit;
	private final NodeExpander<S, A> nodeExpander;
	private Metrics metrics = new Metrics();

	public DepthLimitedSearch(int limit) {
		this(limit, new NodeExpander<>());
	}

	public DepthLimitedSearch(int limit, NodeExpander<S, A> nodeExpander) {
		this.limit = limit;
		this.nodeExpander = nodeExpander;
	}

	// function DEPTH-LIMITED-SEARCH(problem, limit) returns a solution, or
	// failure/cutoff
	/**
	 * Returns a list of actions to the goal if the goal was found, a list
	 * containing a single NoOp Action if already at the goal, an empty list if
	 * the goal could not be found.
	 * 
	 * @return if goal found, the list of actions to the Goal. If already at the
	 *         goal you will receive a List with a single NoOp Action in it. If
	 *         fail to find the Goal, an empty list will be returned to indicate
	 *         that the search failed.
	 */
	@Override
	public List<A> findActions(Problem<S, A> p) {
		nodeExpander.useParentLinks(true);
		Node<S, A> node = findNode(p);
		return (node == null || isCutoffNode(node)) ? SearchUtils.failure() : SearchUtils.getSequenceOfActions(node);
	}

	@Override
	public S findState(Problem<S, A> p) {
		nodeExpander.useParentLinks(false);
		Node<S, A> node = findNode(p);
		return (node == null || isCutoffNode(node)) ? null : node.getState();
	}
	
	public Node<S, A> findNode(Problem<S, A> p) {
		clearInstrumentation();
		// return RECURSIVE-DLS(MAKE-NODE(INITIAL-STATE[problem]), problem,
		// limit)
		return recursiveDLS(nodeExpander.createRootNode(p.getInitialState()), p, limit);
	}

	// function RECURSIVE-DLS(node, problem, limit) returns a solution, or
	// failure/cutoff
	
	// In Java 8 the result type should be Optional<Node>!
	/**
	 * Returns a solution node, the {@link #cutoffNode}, or null (failure).
	 */
	private Node<S, A> recursiveDLS(Node<S, A> node, Problem<S, A> problem, int limit) {
		// if problem.GOAL-TEST(node.STATE) then return SOLUTION(node)
		if (problem.testSolution(node)) {
			metrics.set(METRIC_PATH_COST, node.getPathCost());
			return node;
		} else if (0 == limit || CancelableThread.currIsCanceled()) {
			// else if limit = 0 then return cutoff
			return cutoffNode;
		} else {
			// else
			// cutoff_occurred? <- false
			boolean cutoff_occurred = false;
			// for each action in problem.ACTIONS(node.STATE) do
			metrics.incrementInt(METRIC_NODES_EXPANDED);
			for (Node<S, A> child : nodeExpander.expand(node, problem)) {
				// child <- CHILD-NODE(problem, node, action)
				// result <- RECURSIVE-DLS(child, problem, limit - 1)
				Node<S, A> result = recursiveDLS(child, problem, limit - 1);
				// if result = cutoff then cutoff_occurred? <- true
				if (isCutoffNode(result)) {
					cutoff_occurred = true;
				} else if (result != null) {
					// else if result != failure then return result
					return result;
				}
			}

			// if cutoff_occurred? then return cutoff else return failure
			if (cutoff_occurred) {
				return cutoffNode;
			} else {
				return null;
			}
		}
	}

	public boolean isCutoffNode(Node<S, A> node) {
		return node == cutoffNode;
	}

	/**
	 * Returns all the search metrics.
	 */
	@Override
	public Metrics getMetrics() {
		return metrics;
	}

	/**
	 * Sets the nodes expanded and path cost metrics to zero.
	 */
	private void clearInstrumentation() {
		metrics.set(METRIC_NODES_EXPANDED, 0);
		metrics.set(METRIC_PATH_COST, 0);
	}

	@Override
	public void addNodeListener(Consumer<Node<S, A>> listener)  {
		nodeExpander.addNodeListener(listener);
	}

	@Override
	public boolean removeNodeListener(Consumer<Node<S, A>> listener) {
		return nodeExpander.removeNodeListener(listener);
	}
}