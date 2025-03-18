import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class PositionGomoku implements Position{
    private final int size;
    private final int[] board;
    private int plOnTurn = 1;
    private List<Move> possibleMoves;
    private final Stack<Move> moveStack = new Stack<>();
    private final Stack<Integer> childOrdStack = new Stack<>();
    private GameState state = GameState.ONGOING;
    static class Move {
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
        Move next(int size){
            if (x == size - 1 && y == size - 1)
                return null;
            return new Move(y == size - 1 ? x + 1 : x, (y + 1) % size);
        }
        void add(Move move) {
            x += move.x;
            y += move.y;
        }
        boolean notEquals(Move move) {
            return x != move.x || y != move.y;
        }
        boolean isValid(int size) {
            return x >= 0 && y >= 0 && x < size && y < size;
        }
    }

    PositionGomoku(int size) {
        this.size = size;
        board = new int[this.size];
        findPossibleMoves();
        childOrdStack.push(0);
    }
    PositionGomoku(PositionGomoku pos, int moveId) {
        assert pos.possibleMoves != null && pos.possibleMoves.size() > moveId && moveId >= 0;
        Move lastMove = pos.possibleMoves.get(moveId);
        assert pos.get(lastMove) == 0;

        size = pos.size;
        board = Arrays.copyOf(pos.board, size);
        set(lastMove, pos.plOnTurn);
        plOnTurn = 3 - pos.plOnTurn;

        int newSize = pos.possibleMoves.size() - 1;
        possibleMoves = new ArrayList<>(pos.possibleMoves);
        possibleMoves.set(moveId, possibleMoves.get(newSize));
        possibleMoves.remove(newSize);

        moveStack.push(lastMove);
        childOrdStack.push(0);
        state = computeState(this, lastMove);
    }

    public int getSize() {
        return size;
    }

    public int[] getBoard() {
        return Arrays.copyOf(board, size);
    }

    public int getPlOnTurn() {
        return plOnTurn;
    }

    public List<Move> getPossibleMoves() {
        return new ArrayList<>(possibleMoves);
    }

    private int get(Move move) {
        return ((board[move.x] >> (move.y * 2)) & 3);
    }
    static int get(int[] board, Move move) {
        return ((board[move.x] >> (move.y * 2)) & 3);
    }
    private void set(Move move, int pl) {
        board[move.x] = (board[move.x] & ~(3 << (move.y * 2))) | (pl << (move.y * 2));
    }
    static void set(int[] board, Move move, int pl) {
        board[move.x] = (board[move.x] & ~(3 << (move.y * 2))) | (pl << (move.y * 2));
    }
    private void findPossibleMoves() {
        possibleMoves = new ArrayList<>();
        for (Move move = new Move(); move != null; move = move.next(size))
            if (get(move) == 0)
                possibleMoves.add(move);
    }
    static boolean gameWin(int size, int[] board, Move lastMove) {
        int lastPl = PositionGomoku.get(board, lastMove);
        for (Move dir : List.of(new Move(1,0), new Move(0,1), new Move(1,1), new Move(1,-1))) {
            int count = 0;
            Move end = new Move(lastMove, 5, dir);
            for (Move square = new Move(lastMove, -4, dir); square.notEquals(end); square.add(dir)) {
                if (square.isValid(size)) {
                    if (PositionGomoku.get(board, square) == lastPl)
                        count++;
                    else
                        count = 0;
                    if (count >= 5)
                        return true;
                }
            }
        }
        return false;
    }
    static GameState computeState(PositionGomoku position, Move lastMove) {
        if (gameWin(position.size, position.board, lastMove))
            return GameState.WIN;
        if (position.possibleMoves.isEmpty())
            return GameState.DRAW;
        return GameState.ONGOING;
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

        state = computeState(this, move);
        return true;
    }

    @Override
    public void revertToParent() {
        childOrdStack.pop();
        int childOrd = childOrdStack.pop();
        Move move = moveStack.pop();
        set(move, 0);
        plOnTurn = 3 - plOnTurn;

        if (childOrd == possibleMoves.size())
            possibleMoves.add(move);
        else {
            possibleMoves.add(possibleMoves.get(childOrd));
            possibleMoves.set(childOrd, move);
        }
        childOrdStack.push(childOrd + 1);

        state = GameState.ONGOING;
    }

    @Override
    public GameState state() {
        return state;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PositionGomoku o))
            return false;
        if (size != o.size)
            return false;
        for (int i = 0; i < size; i++)
            if (board[i] != o.board[i])
                return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder line = new StringBuilder("│\n├───");
        for (int i = 1; i < size; i++)
            line.append("┼───");
        line.append("┤\n");

        StringBuilder stringBuilder = new StringBuilder("┌───");
        for (int i = 1; i < size; i++)
            stringBuilder.append("┬───");
        stringBuilder.append("┐\n");

        for (Move move = new Move(); move != null; move = move.next(size)) {
            stringBuilder.append("│");
            stringBuilder.append(List.of("   ", " X ", " O ", "Err").get(get(move)));
            if (move.y == size - 1 && move.x < size - 1)
                stringBuilder.append(line);
        }

        stringBuilder.append("│\n└───");
        for (int i = 1; i < size; i++)
            stringBuilder.append("┴───");
        stringBuilder.append("┘\n");
        return stringBuilder.toString();
    }
}
