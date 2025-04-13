import java.util.*;

public class ABGomokuRelatedMoves implements ABPosition{
    PositionGomoku position;
    List<Set<PositionGomoku.Move>> moves;
    Stack<PositionGomoku.Move> moveStack;
    Stack<Iterator<PositionGomoku.Move>> iteratorStack;
    Stack<Integer> movesIndexStack;
    private static final int DISTANCE = 2;
    Set<PositionGomoku.Move> relatedMoves(PositionGomoku.Move move) {
        Set<PositionGomoku.Move> relatedMoves = new HashSet<>();
        PositionGomoku.Move from = new PositionGomoku.Move(
                Integer.max(0, move.x - DISTANCE),
                Integer.max(0, move.y - DISTANCE));
        PositionGomoku.Move to = new PositionGomoku.Move(
                Integer.min(position.size - 1, move.x + DISTANCE),
                Integer.min(position.size - 1, move.y + DISTANCE));
        PositionGomoku.Move m = new PositionGomoku.Move(from.x, from.y);
        while(true) {
            if (position.get(m) == 0 && moves.stream().noneMatch(set -> set.contains(m)))
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
        moves = new ArrayList<>();

        Set<PositionGomoku.Move> relatedMoves = new HashSet<>();
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

    @Override
    public boolean next(boolean updatePossibleMoves) {
        Iterator<PositionGomoku.Move> it = iteratorStack.peek();
        while (!it.hasNext()) {
            int movesIndex = movesIndexStack.pop() + 1;
            movesIndexStack.push(movesIndex);
            if (movesIndex >= moves.size())
                return false;
            it = moves.get(movesIndex).iterator();
            iteratorStack.pop();
            iteratorStack.push(it);
        }

        PositionGomoku.Move move = it.next();
        if (moveStack.contains(move))
            return next(updatePossibleMoves);

        position.applyMove(move);
        moveStack.push(move);
        iteratorStack.push(moves.get(0).iterator());
        movesIndexStack.push(0);

        if (updatePossibleMoves)
            moves.add(relatedMoves(move));
        return true;
    }

    @Override
    public void back(boolean updatePossibleMoves) {
        iteratorStack.pop();
        movesIndexStack.pop();
        position.revertMove(moveStack.pop());

        if (updatePossibleMoves)
            moves.remove(moves.size() - 1);
    }
    @Override
    public void initIterator() {
        moveStack = new Stack<>();
        iteratorStack = new Stack<>();
        iteratorStack.push(moves.get(0).iterator());
        movesIndexStack = new Stack<>();
        movesIndexStack.push(0);
    }

    @Override
    public Object getMove() {
        if (moveStack.isEmpty())
            return null;
        return moveStack.peek();
    }

    @Override
    public Position getPosition() {
        return position;
    }
}
