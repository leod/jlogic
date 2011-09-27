package jlogic.interpret;

import jlogic.Knowledge;
import jlogic.Predicate;
import jlogic.Rule;
import jlogic.term.Structure;
import jlogic.term.Term;

import fj.data.List;

final class SearchNode {
    // These two references are invariant across all search nodes in one tree
    private final Knowledge knowledge;
    private final InternalVariableFactory internalVariableFactory;

    private final SearchNode parent;
    private final Frame frame;

    // List of goals that must be fulfilled
    private final List<Term> goals;

    // Null if goals is empty, head of goals otherwise
    private final Term goal;

    // Null if goals is empty or there is no predicate of the name that
    // goal requires
    private final Predicate goalPredicate;

    private final int numClauses;
    private int currentClause = 0;

    // The children list is only used to create pretty graphs using toDOT.
    // Since the actual evaluation is depth-first, control is always
    // immediately given to newly created children. Hence, we don't need a
    // list of children in the evaluation.
    private List<SearchNode> children = List.nil();

    private Predicate getPredicate(Term term) {
        if (term instanceof Structure) {
            Structure structure = (Structure) term;
            return knowledge.getPredicate(structure.getFullName());
        }
        throw new AssertionError(term.toString()); // TODO
    }

    // TODO: Hack
    private static List<Term> getTerms(Rule rule) {
        List<Term> result = List.nil();

        if (!rule.isFact()) {
            for (Term term : rule.getBody()) {
                result = result.append(List.single(term));
            }
        }

        return result;
    }

    public SearchNode(Knowledge knowledge,
            InternalVariableFactory internalVariableFactory, SearchNode parent,
            Frame frame, List<Term> goals) {
        this.knowledge = knowledge;
        this.internalVariableFactory = internalVariableFactory;
        this.parent = parent;
        this.goals = goals;
        this.frame = frame;

        goal = goals.isEmpty() ? null : goals.head();
        goalPredicate = goal != null ? getPredicate(this.goal) : null;
        numClauses = goalPredicate != null ? goalPredicate.getClauses().length : 0;
    }

    public SearchNode getParent() {
        return parent;
    }

    public List<SearchNode> getChildren() {
        return children;
    }

    public List<Term> getGoals() {
        return goals;
    }

    /**
     * Returns a copy of this node's frame.
     */
    public Frame getFrame() {
        return new Frame(frame);
    }

    /**
     * Searches for exactly one match of this node's goals, creating child nodes
     * and backtracking as needed. This is the meat of the evaluation algorithm.
     */
    public SearchResult searchOne() {
        if (goals.isEmpty()) {
            // An empty goal list means that we have found a valid result.
            // Hand control to our parent and return our frame of variable
            // instantiations.
            return SearchResult.returnOneResultAndYieldControl(frame, parent);
        }

        if (currentClause == numClauses || goalPredicate == null) {
            // All clauses in our predicate were tried already, or our
            // predicate does not exist: backtrack to parent.
            return SearchResult.yieldControl(parent);
        }

        Frame matchFrame;
        do {
            Rule clause = this.goalPredicate.getClauses()[currentClause];
            ++currentClause;

            // Replace all free variables in the clause's arguments and body
            // by internal variables before matching
            Frame augmentedFrame = new Frame(frame);
            InternalizeFreeVariables internalizer =
                    new InternalizeFreeVariables(internalVariableFactory,
                            augmentedFrame);
            Instantiate instantiate = new Instantiate(augmentedFrame);

            Term clauseHead = clause.getHead().accept(internalizer);
            List<Term> clauseBody = instantiate.visit(getTerms(clause));

            matchFrame = Match.match(frame, goal, clauseHead);

            // Create a new child with the first clause in our predicate
            // that matches
            if (matchFrame != null) {
                matchFrame = new Frame(matchFrame);

                // Instantiate the new goal list with known variables,
                // then replace all remaining free variables by internal
                // variables
                instantiate = new Instantiate(matchFrame);
                internalizer =
                        new InternalizeFreeVariables(internalVariableFactory,
                                new Frame(matchFrame));

                clauseBody = internalizer.visit(clauseBody);

                List<Term> childGoals = clauseBody.append(goals.tail());
                childGoals = instantiate.visit(childGoals);

                SearchNode childNode = new SearchNode(knowledge,
                        internalVariableFactory, this, matchFrame, childGoals);
                children = children.append(List.single(childNode));

                // Give control to our new child
                return SearchResult.yieldControl(childNode);
            }
        } while (matchFrame == null && currentClause != numClauses);

        // No matching clause was found. Backtrack to our parent.
        return SearchResult.yieldControl(parent);
    }

}