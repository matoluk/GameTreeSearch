import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class PositionGomoku implements Position{
    private final int size;
    private final int[] board;
    private int plOnTurn = 1;
    List<Move> possibleMoves;
    private final Stack<Move> moveStack = new Stack<>();
    final Stack<Integer> childOrdStack = new Stack<>();
    private GameState state = GameState.ONGOING;
    private class Move {
        int x = 0;
        int y = 0;
        Move() {}
        Move(int x, int y) {
            this.x = x;
            this.y = y;
        }
        Move(Move m1, int mul, Move m2) {
            x = m1.x + mul * m2.x;
            y = m1.y + mul * m2.y;
        }
        Move next(){
            if (x == size - 1 && y == size - 1)
                return null;
            return new Move(y == size - 1 ? x + 1 : x, (y + 1) % size);
        }
        void add(Move move) {
            x += move.x;
            y += move.y;
        }
        boolean equals(Move move) {
            return x == move.x && y == move.y;
        }
        boolean isValid() {
            return x >= 0 && y >= 0 && x < size && y < size;
        }
    }

    PositionGomoku(int size) {
        this.size = size;
        board = new int[this.size];
        findPossibleMoves();
        childOrdStack.push(0);
    }
    PositionGomoku(PositionGomoku pos) {
        // not copy Stacks
        size = pos.size;
        board = Arrays.copyOf(pos.board, size);
        plOnTurn = pos.plOnTurn;
        possibleMoves = new ArrayList<>(pos.possibleMoves);
        state = pos.state;
    }
    PositionGomoku(PositionGomoku pos, int moveId) {
        assert pos.possibleMoves != null && pos.possibleMoves.size() > moveId && moveId >= 0;
        moveStack.push(pos.possibleMoves.get(moveId));
        assert pos.get(moveStack.peek()) == 0;

        size = pos.size;
        board = Arrays.copyOf(pos.board, size);
        set(moveStack.peek(), pos.plOnTurn);
        plOnTurn = 3 - pos.plOnTurn;

        int newSize = pos.possibleMoves.size() - 1;
        possibleMoves = new ArrayList<>(pos.possibleMoves);
        possibleMoves.set(moveId, possibleMoves.get(newSize));
        possibleMoves.remove(newSize);

        childOrdStack.push(0);
        checkGameState();
    }

    private int get(Move move) {
        return ((board[move.x] >> (move.y * 2)) & 3);
    }
    private void set(Move move, int pl) {
        board[move.x] |= (pl << (move.y * 2));
    }
    private void findPossibleMoves() {
        possibleMoves = new ArrayList<>();
        for (Move move = new Move(); move != null; move = move.next())
            if (get(move) == 0)
                possibleMoves.add(move);
    }
    private void checkGameState() {
        Move move = moveStack.peek();
        int lastPl = 3 - plOnTurn;
        for (Move dir : List.of(new Move(1,0), new Move(0,1), new Move(1,1), new Move(1,-1))) {
            int count = 0;
            Move end = new Move(move, 5, dir);
            for (Move pos = new Move(move, -4, dir); !pos.equals(end); pos.add(dir)) {
                if (pos.isValid()) {
                    if (get(pos) == lastPl)
                        count++;
                    else
                        count = 0;
                    if (count >= 5) {
                        state = GameState.WIN;
                        return;
                    }
                }
            }
        }
        if (possibleMoves.isEmpty())
            state = GameState.DRAW;
    }
    @Override
    public List<Position> getChildren() {
        List<Position> children = new ArrayList<>(possibleMoves.size());
        for (int i = 0; i < possibleMoves.size(); i++)
            children.add(new PositionGomoku(this, i));
        return children;
    }

    @Override
    public boolean advanceToNext() {
        int childOrd = childOrdStack.peek();
        if (childOrd >= possibleMoves.size())
            return false;

        Move move = possibleMoves.get(childOrd);
        set(move, plOnTurn);
        plOnTurn = 3 - plOnTurn;

        int newSize = possibleMoves.size() - 1;
        possibleMoves.set(childOrd, possibleMoves.get(newSize));
        possibleMoves.remove(newSize);
        moveStack.push(move);
        childOrdStack.push(0);

        checkGameState();
        return true;
    }

    @Override
    public void revertToParent() {
        childOrdStack.pop();
        int childOrd = childOrdStack.pop();
        Move move = moveStack.pop();
        set(move, 0);
        plOnTurn = 3 - plOnTurn;

        possibleMoves.add(possibleMoves.get(childOrd));
        possibleMoves.set(childOrd, move);
        childOrdStack.push(childOrd + 1);

        state = GameState.ONGOING;
    }

    @Override
    public GameState state() {
        return state;
    }
}
