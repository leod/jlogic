package jlogic.interpret;

final class SearchResult {
    private final Frame frame;
    private final SearchNode node;

    private SearchResult(Frame frame, SearchNode node) {
        this.frame = frame;
        this.node = node;
    }

    public static SearchResult returnOneResultAndYieldControl(Frame frame, SearchNode node) {
        return new SearchResult(frame, node);
    }

    public static SearchResult yieldControl(SearchNode node) {
        return new SearchResult(null, node);
    }

    public SearchNode getNode() {
        return node;
    }

    public Frame getFrame() {
        return frame;
    }
}
