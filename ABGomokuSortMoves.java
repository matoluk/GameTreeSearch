import java.util.*;

public class ABGomokuSortMoves implements ABPosition{
    PositionGomoku position;
    Node node;
    Stack<HashSet<PositionGomoku.Move>> moves;
    Stack<Iterator<PositionGomoku.Move>> movesIterators;
    Stack<Iterator<Node>> childrenIterators;
    int distance;
    ABGomokuSortMoves() {
        distance = 2;
    }
    ABGomokuSortMoves(int distance) {
        this.distance = distance;
    }
    class Node {
        PositionGomoku.Move move;
        double value;
        Node parent;
        HashSet<Node> children;
        Node(PositionGomoku.Move move) {
            this.move = move;
        }
        Node(Node parent, PositionGomoku.Move move) {
            this.parent = parent;
            this.move = move;
        }
        Iterator<Node> sortIterate() {
            if (children == null)
                return Collections.emptyIterator();
            List<Node> childrenList = new ArrayList<>(children);
            childrenList.sort(Comparator.comparingDouble(node -> -node.value));
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
    HashSet<PositionGomoku.Move> relatedMoves(PositionGomoku.Move move) {
        HashSet<PositionGomoku.Move> relatedMoves = new HashSet<>();
        PositionGomoku.Move from = new PositionGomoku.Move(
                Integer.max(0, move.x - distance),
                Integer.max(0, move.y - distance));
        PositionGomoku.Move to = new PositionGomoku.Move(
                Integer.min(position.size - 1, move.x + distance),
                Integer.min(position.size - 1, move.y + distance));
        PositionGomoku.Move m = new PositionGomoku.Move(from.x, from.y);
        while(true) {
            if (position.get(m) == 0 && !moves.contains(m))
                relatedMoves.add(new PositionGomoku.Move(m.x, m.y));
            m.x++;
            if (m.x > to.x) {
                m.x = from.x;
                m.y++;
                if (m.y > to.y)
                    return relatedMoves;
            }
        }
    }
    @Override
    public void init(Position pos) {
        if (!(pos instanceof PositionGomoku))
            throw new IllegalArgumentException("Expected PositionGomoku");

        position = (PositionGomoku) pos;
        node = new Node(null);
        moves = new Stack<>();

        HashSet<PositionGomoku.Move> relatedMoves = new HashSet<>();
        for (PositionGomoku.Move move = new PositionGomoku.Move(); move != null; move = move.next(position.size))
            if (position.get(move) != 0)
                relatedMoves.addAll(relatedMoves(move));
        if (relatedMoves.isEmpty()) {
            PositionGomoku.Move center = new PositionGomoku.Move(position.size / 2, position.size / 2);
            if (position.get(center) == 0)
                relatedMoves.add(center);
        }
        moves.add(relatedMoves);
    }
    private void newNode(PositionGomoku.Move move) {
        position.applyMove(move);
        Node child = new Node(node, move);
        if (node.children == null)
            node.children = new HashSet<>();
        node.children.add(child);
        node = child;
    }
    private void updateMoves(PositionGomoku.Move move) {
        childrenIterators.push(node.sortIterate());
        HashSet<PositionGomoku.Move> newMoves = new HashSet<>(moves.peek());
        newMoves.addAll(relatedMoves(move));
        newMoves.remove(move);
        moves.push(newMoves);
        movesIterators.push(newMoves.iterator());
    }
    @Override
    public boolean next(boolean leaf) {
        if (leaf) {
            if (movesIterators.peek().hasNext()) {
                newNode(movesIterators.peek().next());
                return true;
            }
            return false;
        }
        if (node.children == null)
            node.children = new HashSet<>();
        if (childrenIterators.peek().hasNext()) {
            Node child = childrenIterators.peek().next();
            position.applyMove(child.move);
            node = child;
            updateMoves(node.move);
            return true;
        }
        while (movesIterators.peek().hasNext()) {
            PositionGomoku.Move move = movesIterators.peek().next();
            if (node.children.contains(new Node(move)))
                continue;
            newNode(move);
            updateMoves(move);
            return true;
        }
        return false;
    }

    @Override
    public void back(boolean leaf, double value) {
        node.value = value;
        position.revertMove(node.move);
        node = node.parent;
        if (leaf)
            return;
        childrenIterators.pop();
        moves.pop();
        movesIterators.pop();
    }

    @Override
    public void initIterator() {
        childrenIterators = new Stack<>();
        childrenIterators.push(node.sortIterate());
        movesIterators = new Stack<>();
        movesIterators.push(moves.peek().iterator());
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
