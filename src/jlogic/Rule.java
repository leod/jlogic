package jlogic;

public final class Rule {
    private final Structure head;
    private final Term[] body;

    public Rule(Structure head, Term[] body) {
        if (head == null)
            throw new IllegalArgumentException("head must not be null");

        this.head = head;
        this.body = body;
    }

    public Rule(Structure head) {
        this(head, null);
    }

    public boolean isFact() {
        return body == null;
    }

    public Structure getHead() {
        return head;
    }

    public Term[] getBody() {
        return body;
    }
}
