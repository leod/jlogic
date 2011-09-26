package jlogic.interpret;

import java.util.Map;

import jlogic.Knowledge;
import jlogic.Predicate;
import jlogic.Rule;
import jlogic.term.AnonymousVariable;
import jlogic.term.Atom;
import jlogic.term.Structure;
import jlogic.term.Term;
import jlogic.term.Variable;
import jlogic.term.Visitor;

import fj.F;
import fj.F2;
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
    private final Node root;
    private final Frame queryFrame;

    /**
     * Assigned to by Nodes. Keeps track of the node that currently has control.
     */
    private Node current;

    /**
     * Assigned to by Nodes. Holds the instantiations of a successful query. If
     * this is assigned a non-null value, the loop in searchOne terminates and
     * returns one result.
     */
    private Frame result;

    public SearchTree(Knowledge knowledge, Structure query) {
        this.knowledge = knowledge;
        internalVariableFactory = new InternalVariableFactory();

        // To prevent conflicts, first replace all free variables in the query
        // by internal variables. These instantiations are kept in `queryFrame'.
        queryFrame = new Frame();
        InternalizeFreeVariables internalizer =
                new InternalizeFreeVariables(internalVariableFactory, queryFrame);
        query = (Structure) internalizer.visit(query);

        root = current = new Node(null, new Frame(), List.single((Term) query));
    }

    public Frame searchOne() {
        result = null;
        while (current != null && result == null)
            current.searchOne();

        if (result != null)
            return createResultFrame(result);
        else
            return null;
    }

    /**
     * Creates a frame containing only the free variables in the original query,
     * instantiated with the frame of a successful match.
     */
    private Frame createResultFrame(Frame frame) {
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

    // Used to generate node names in Node.toDOT
    private int idCounter = 0;

    public String toDOT() {
        idCounter = 0;

        StringBuilder builder = new StringBuilder();
        builder.append("digraph {\n");
        root.toDOT(builder);
        builder.append("}");
        return builder.toString();
    }

    // TODO: Hack
    private List<Term> getTerms(Rule rule) {
        List<Term> result = List.nil();

        if (!rule.isFact()) {
            for (Term term : rule.getBody()) {
                result = result.append(List.single(term));
            }
        }

        return result;
    }

    private Predicate getPredicate(Term term) {
        if (term instanceof Structure) {
            Structure structure = (Structure) term;
            return knowledge.getPredicate(structure.getFullName());
        }
        assert false; // TODO
        throw new AssertionError(term.toString());
    }

    // Useful in debugging as there's no List.toString
    @SuppressWarnings("unused")
    private String clausesToString(List<Term> clauses) {
        return clauses.foldLeft(new F2<String, Term, String>() {
            @Override
            public String f(String accum, Term term) {
                return accum + term.toString();
            }
        }, "");
    }

    private final class Node {
        private final Node parent;

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
        // list of children there.
        private List<Node> children = List.nil();

        public Node(Node parent, Frame frame, List<Term> goals) {
            this.parent = parent;
            this.goals = goals;
            this.frame = frame;

            this.goal = goals.isEmpty() ? null : goals.head();
            this.goalPredicate = goal != null ? getPredicate(this.goal) : null;
            this.numClauses = this.goalPredicate != null ? this.goalPredicate.getClauses().length : 0;
        }

        /**
         * Searches for exactly one match of this node's goals, creating child
         * nodes and backtracking as needed.
         */
        public void searchOne() {
            if (goals.isEmpty()) {
                // System.out.println("Reached empty node");

                // An empty goal list means that we have found a valid result.
                // Hand control to our parent and return our frame of variable
                // instantiations.
                current = parent;
                result = frame;
                return;
            }

            if (currentClause == numClauses || goalPredicate == null) {
                // All clauses in our predicate were tried already, or our
                // predicate does not exist: backtrack to parent.
                current = parent;
                return;
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

                // System.out.println("Matching: " + goal + " vs. " + clauseHead
                // + " with " + frame);
                matchFrame = Match.match(frame, goal, clauseHead);

                // Create a new child with the first clause in our predicate
                // that matches
                if (matchFrame != null) {
                    matchFrame = new Frame(matchFrame);
                    // System.out.println("Matched in " + matchFrame);

                    // Instantiate the new goal list with known variables,
                    // then replace all remaining free variables by internal
                    // variables
                    instantiate = new Instantiate(matchFrame);
                    internalizer = new InternalizeFreeVariables(internalVariableFactory, new Frame(matchFrame));

                    clauseBody = internalizer.visit(clauseBody);

                    List<Term> childGoals = clauseBody.append(goals.tail());
                    childGoals = instantiate.visit(childGoals);

                    Node childNode = new Node(this, matchFrame, childGoals);
                    children = children.cons(childNode);

                    // Give control to our new child
                    current = childNode;
                    return;
                }
            } while (matchFrame == null && currentClause != numClauses);

            // No matching clause was found. Backtrack to our parent.
            current = parent;
        }

        /**
         * Returns a list of variables, who were newly instantiated in this
         * node, when compared to its parent. This is used by toDOT.
         */
        public List<Map.Entry<Variable, Term>> frameDeltaToParent() {
            assert parent != null;

            List<Map.Entry<Variable, Term>> delta = List.nil();
            for (Map.Entry<Variable, Term> entry : frame.getInstantiations().entrySet()) {
                Term parentTerm = parent.frame.getInstantiation(entry.getKey());
                if (parentTerm == null || !parentTerm.equals(entry.getValue())) {
                    entry.setValue(entry.getValue().accept(new Instantiate(frame)));
                    delta = delta.cons(entry);
                }
            }
            return delta;
        }

        /**
         * Filters a list of variable instantiations, returning only those
         * variables which are explicitly referred to in the parent's goal list.
         * This is used by toDOT.
         */
        public List<Map.Entry<Variable, Term>> filterFrameDelta(List<Map.Entry<Variable, Term>> delta) {
            assert parent != null;
            assert parent.goals != null;

            return delta.filter(new F<Map.Entry<Variable, Term>, Boolean>() {
                @Override
                public Boolean f(final Map.Entry<Variable, Term> entry) {
                    class ContainsVariable implements Visitor<Boolean> {
                        @Override
                        public Boolean visit(AnonymousVariable anonymousVariable) {
                            return false;
                        }

                        @Override
                        public Boolean visit(Atom atom) {
                            return false;
                        }

                        @Override
                        public Boolean visit(Structure structure) {
                            Boolean result = false;
                            for (Term argument : structure.getArguments())
                                result = result || argument.accept(this);
                            return result;
                        }

                        @Override
                        public Boolean visit(Variable variable) {
                            return variable.equals(entry.getKey());
                        }

                        public Boolean visit(List<Term> terms) {
                            final Visitor<Boolean> outer = this;
                            return terms.foldLeft(new F2<Boolean, Term, Boolean>() {
                                @Override
                                public Boolean f(Boolean b, Term term) {
                                    return b || term.accept(outer);
                                }
                            }, false);
                        }
                    };

                    return (new ContainsVariable()).visit(parent.goals);
                }
            });
        }

        /**
         * Creates a DOT string representation of this node and its children.
         * 
         * @return An unique identifier used while generating.
         */
        public int toDOT(StringBuilder builder) {
            int thisId = idCounter++;

            builder.append("\tN");
            builder.append(thisId);
            builder.append(" [label=\"");

            for (Term goal : goals) {
                builder.append(goal);
                builder.append(",\\n");
            }

            if (!goals.isEmpty()) // Remove last comma and newline if existing
                builder.delete(builder.length() - 3, builder.length());
            else {
                Frame resultFrame = createResultFrame(frame);
                for (Map.Entry<Variable, Term> entry : resultFrame.getInstantiations().entrySet()) {
                    builder.append(entry.getKey());
                    builder.append(" = ");
                    builder.append(entry.getValue());
                    builder.append("\\n");
                }
            }

            builder.append("\"");

            if (goals.isEmpty())
                builder.append(" color=green");
            else if (children.isEmpty())
                builder.append(" color=red");

            builder.append("];\n");

            for (Node child : children) {
                int childId = child.toDOT(builder);
                List<Map.Entry<Variable, Term>> delta = child.filterFrameDelta(
                        child.frameDeltaToParent());

                builder.append("\tN");
                builder.append(thisId);
                builder.append(" -> ");
                builder.append("N");
                builder.append(childId);
                builder.append(" [label=\"");
                for (Map.Entry<Variable, Term> entry : delta) {
                    builder.append(entry.getKey());
                    builder.append(" = ");
                    builder.append(entry.getValue());
                    builder.append("\\n");
                }
                builder.append("\"];\n");
            }

            return thisId;
        }
    }
}
