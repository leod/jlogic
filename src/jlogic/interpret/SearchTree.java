package jlogic.interpret;

import java.util.Map;

import jlogic.Knowledge;
import jlogic.term.Structure;
import jlogic.term.Term;
import jlogic.term.Variable;

import fj.data.List;

/**
 * Interprets queries on a knowledge base using a tree. Each node in the tree
 * contains a list of goals which must be fulfilled.
 * 
 * This implementation is quite inefficient as it copies terms every time their
 * variables are instantiated.
 */
public final class SearchTree {
    private final Knowledge knowledge;
    private final InternalVariableFactory internalVariableFactory;

    private final SearchNode root;
    private final Frame queryFrame;

    // Keeps track of where we are evaluating in the tree right now.
    // This allows us to resume evaluation at the right point after having
    // returned one result from searchOne() and then being asked for the next
    // result.
    private SearchNode current;

    public SearchTree(Knowledge knowledge, Structure query) {
        this.knowledge = knowledge;
        internalVariableFactory = new InternalVariableFactory();

        // To prevent conflicts, first replace all free variables in the query
        // by internal variables. These instantiations are kept in `queryFrame'.
        queryFrame = new Frame();
        InternalizeFreeVariables internalizer =
                new InternalizeFreeVariables(internalVariableFactory, queryFrame);
        query = (Structure) internalizer.visit(query);

        root = current = new SearchNode(knowledge, internalVariableFactory,
                null, new Frame(), List.single((Term) query));
    }

    /**
     * Searches for one match for the given query, returning the frame of
     * matching variable instantiations or null, if no match was found. If null
     * was returned, it is futile to call searchOne() again.
     */
    public Frame searchOne() {
        Frame frame = null;
        while (current != null && frame == null) {
            SearchResult searchResult = current.searchOne();

            frame = searchResult.getFrame();
            current = searchResult.getNode();
        }

        if (frame != null)
            return createResultFrame(frame);
        else
            return null;
    }

    /**
     * Creates a frame containing only the free variables in the original query,
     * instantiated with the given frame of a successful match.
     */
    Frame createResultFrame(Frame frame) {
        Frame result = new Frame();

        Instantiate instantiate = new Instantiate(frame);
        for (Map.Entry<Variable, Term> entry : queryFrame.getInstantiations().
                entrySet()) {
            Term term = frame.getInstantiation((Variable) entry.getValue());
            Term instantiatedTerm = term.accept(instantiate);

            result.instantiate(entry.getKey(), instantiatedTerm);
        }

        return result;
    }

    SearchNode getRoot() {
        return root;
    }
}
