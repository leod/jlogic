package jlogic.interpret;

import jlogic.Knowledge;
import jlogic.Predicate;
import jlogic.Rule;
import jlogic.term.Structure;
import jlogic.term.Term;

import fj.F2;
import fj.data.List;

/**
 * Interprets queries on a knowledge base using a tree. Each node in the tree
 * contains a list of goals which must be fulfilled.
 * 
 * This implementation is quite inefficient as it copies terms every time
 * they're instantiated.
 */
public final class SearchTree {
    private final Knowledge knowledge;
    private final Node root;

    private Node current;
    private Frame result;

    public SearchTree(Knowledge knowledge, Structure query) {
        this.knowledge = knowledge;

        System.out.println("Query: " + query);

        root = current = new Node(null, List.single((Term) query));
    }

    public Frame searchOne() {
        result = null;
        while (current != null && result == null)
            current.searchOne();
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
            System.out.println("Looking up " + structure.getFullName());
            return knowledge.getPredicate(structure.getFullName());
        }
        assert false; // TODO
        throw new AssertionError(term.toString());
    }

    private final class Node {
        private final Node parent;

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

        public Node(Node parent, List<Term> goals) {
            this.parent = parent;
            this.goals = goals;

            this.goal = goals.isEmpty() ? null : goals.head();
            this.goalPredicate = goal != null ? getPredicate(this.goal) : null;
            this.numClauses = this.goalPredicate != null ? this.goalPredicate.getClauses().length : 0;

            System.out.println("New Node with clauses " + goals.foldLeft(new F2<String, Term, String>() {
                @Override
                public String f(String accum, Term term) {
                    return accum + term.toString();
                }
            }, ""));
        }

        public void searchOne() {
            if (goals.isEmpty()) {
                System.out.println("Reached empty node");

                // An empty goal list means that we have found a valid result.
                // Hand control to our parent and return our frame of variable
                // instantiations.
                current = parent;
                result = new Frame(); // TODO: Hack
                return;
            }

            if (currentClause == numClauses || goalPredicate == null) {
                // All clauses in our predicate were tried already, or our
                // predicate does not exist: backtrack to parent.
                current = parent;
                return;
            }

            Frame frame;
            do {
                Rule clause = this.goalPredicate.getClauses()[currentClause];
                ++currentClause;

                frame = Matcher.match(new Frame(), goal, clause.getHead());

                // Create a new child with the first clause in our predicate
                // that matches
                if (frame != null) {
                    System.out.println("Matched in " + frame);
                    Instantiator instantiator = new Instantiator(frame);
                    List<Term> body = instantiator.visit(getTerms(clause));

                    Node childNode = new Node(this, goals.tail().append(body));
                    children = children.cons(childNode);

                    // Give control to our new child
                    current = childNode;
                    return;
                }
            } while (frame == null && currentClause != numClauses);

            // No matching clause was found. Backtrack to our parent.
            current = parent;
        }

        public int toDOT(StringBuilder builder) {
            int thisId = idCounter;
            idCounter += 1;

            builder.append("\tN");
            builder.append(thisId);
            builder.append(" [label=\"");

            for (Term goal : goals) {
                builder.append(goal);
                builder.append(",\\n");
            }

            if (!goals.isEmpty()) // Remove last comma and newline if existing
                builder.delete(builder.length() - 3, builder.length());

            builder.append("\"];\n");

            for (Node child : children) {
                int childId = child.toDOT(builder);

                builder.append("\tN");
                builder.append(thisId);
                builder.append(" -> ");
                builder.append("N");
                builder.append(childId);
                builder.append(";\n");
            }

            return thisId;
        }
    }
}
