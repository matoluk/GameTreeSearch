import java.util.*;

public class ABBreakthrough implements ABPosition{
    PositionBreakthrough position;
    Node node;
    Stack<Iterator<Node>> childrenIterators;
    Stack<Iterator<Object>> movesIterators;
    @Override
    public void clear() {
        position = null;
        node = null;
        childrenIterators = null;
        movesIterators = null;
    }
    static class Node {
        Object move;
        double value = -1;
        Node parent;
        HashSet<Node> children;
        int order;
        Node(Node parent, Object move) {
            this.parent = parent;
            this.move = move;
            if (parent != null) {
                parent.children.add(this);
                order = parent.order;
            }
        }
        Iterator<Node> sortIterate() {
            if (children == null)
                return Collections.emptyIterator();
            List<Node> childrenList = new ArrayList<>(children);
            childrenList.sort(Comparator
                    .comparingDouble((Node node) -> -node.value)
                    .thenComparingInt(node -> node.order));
            return childrenList.iterator();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Node v)) {
                return false;
            }
            return move.equals(v.move);
        }

        @Override
        public int hashCode() {
            return move.hashCode();
        }
    }

    @Override
    public void init(Position pos) {
        if (!(pos instanceof PositionBreakthrough))
            throw new IllegalArgumentException("Expected PositionBreakthrough");
        position = (PositionBreakthrough) pos;
        node = new Node(null, null);
    }
    void updateIterators() {
        childrenIterators.push(node.sortIterate());
        List<Object> moves = position.moves();
        Collections.shuffle(moves);
        movesIterators.push(moves.iterator());
    }
    @Override
    public boolean next(boolean leaf) {
        if (node.children == null)
            node.children = new HashSet<>();
        if (leaf) {
            if (movesIterators.peek().hasNext()) {
                node = new Node(node, movesIterators.peek().next());
                position.applyMove(node.move);
                return true;
            }
            return false;
        }
        if (childrenIterators.peek().hasNext()) {
            node = childrenIterators.peek().next();
            node.order = node.parent.order;
        }
        else {
            while (true) {
                if (!movesIterators.peek().hasNext())
                    return false;
                Object move = movesIterators.peek().next();
                if (!node.children.contains(new Node(null, move))) {
                    node = new Node(node, move);
                    break;
                }
            }
        }
        position.applyMove(node.move);
        updateIterators();
        return true;
    }

    @Override
    public void back(boolean leaf, double value) {
        node.value = value;
        position.revertMove(node.move);
        node.parent.order = node.order + 1;
        node = node.parent;
        if (leaf)
            return;
        childrenIterators.pop();
        movesIterators.pop();
    }

    @Override
    public void initIterator() {
        childrenIterators = new Stack<>();
        movesIterators = new Stack<>();
        updateIterators();
        node.order = 0;
    }

    @Override
    public Object getMove() {
        return node.move;
    }

    @Override
    public Position getPosition() {
        return position;
    }
}
