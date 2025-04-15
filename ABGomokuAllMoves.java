import java.util.*;

public class ABGomokuAllMoves implements ABPosition{
    PositionGomoku position;
    PositionGomoku.Move[] moves;
    int movesCount;
    Stack<PositionGomoku.Move> moveStack;
    Stack<Integer> moveIndexStack;
    @Override
    public void init(Position pos) {
        if (!(pos instanceof PositionGomoku))
            throw new IllegalArgumentException("Expected PositionGomoku");

        position = (PositionGomoku) pos;
        List<Object> possibleMoves = position.moves();
        movesCount = possibleMoves.size();
        if (moves == null)
            moves = new PositionGomoku.Move[position.size * position.size];
        for (int i = 0; i < movesCount; i++)
            moves[i] = (PositionGomoku.Move) possibleMoves.get(i);
    }

    @Override
    public boolean next(boolean updatePossibleMoves) {
        int index = moveIndexStack.peek();
        if (index >= movesCount)
            return false;
        PositionGomoku.Move move = moves[index];
        position.applyMove(move);
        moveStack.push(move);
        moveIndexStack.push(0);

        if (updatePossibleMoves) {
            movesCount--;
            moves[index] = moves[movesCount];
        }
        return true;
    }

    @Override
    public void back(boolean updatePossibleMoves) {
        moveIndexStack.pop();
        int index = moveIndexStack.pop();
        PositionGomoku.Move move = moveStack.pop();
        position.revertMove(move);
        moveIndexStack.push(index + 1);

        if (updatePossibleMoves) {
            moves[movesCount] = moves[index];
            moves[index] = move;
            movesCount++;
        }
    }

    @Override
    public void initIterator() {
        moveStack = new Stack<>();
        moveIndexStack = new Stack<>();
        moveIndexStack.push(0);
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

    @Override
    public Set<Object> getMoves() {
        return new HashSet<>(Arrays.asList(this.moves).subList(0, movesCount));
    }
}
