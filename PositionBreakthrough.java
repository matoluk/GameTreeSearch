import java.util.*;

public class PositionBreakthrough implements Position{
    private final int size;
    private int plOnTurn = 0;
    @SuppressWarnings("unchecked")
    private final TreeSet<Integer>[] pieces = new TreeSet[2];
    private List<Move> possibleMoves;
    private final Stack<RevertableMove> moveStack = new Stack<>();
    private final Stack<Integer> childOrdStack = new Stack<>();
    private GameState state = GameState.ONGOING;
    static class Move {
        int from;
        int to;
        Move(int from, int to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Move o))
                return false;
            return from == o.from && to == o.to;
        }
    }
    static class RevertableMove extends Move {
        boolean takes;
        RevertableMove(Move move, boolean takes) {
            super(move.from, move.to);
            this.takes = takes;
        }
    }
    PositionBreakthrough(int size) {
        this.size = size;
        pieces[0] = new TreeSet<>();
        pieces[1] = new TreeSet<>();

        int lastRow = size * (size - 1);
        int rowBefore = lastRow - size;
        for (int i = 0; i < size; i++) {
            pieces[0].add(i);
            pieces[0].add(size + i);
            pieces[1].add(lastRow + i);
            pieces[1].add(rowBefore + i);
        }
        childOrdStack.push(0);
    }
    PositionBreakthrough(PositionBreakthrough pos, int moveId) {
        assert pos.possibleMoves.size() > moveId && moveId >= 0;
        Move move = pos.possibleMoves.get(moveId);

        size = pos.size;
        pieces[0] = new TreeSet<>(pos.pieces[0]);
        pieces[1] = new TreeSet<>(pos.pieces[1]);
        plOnTurn = pos.plOnTurn;
        set(move);
        childOrdStack.push(0);
    }
    public int getSize() {
        return size;
    }
    @SuppressWarnings("unchecked")
    public TreeSet<Integer>[] getPieces() {
        return new TreeSet[] {new TreeSet<>(pieces[0]), new TreeSet<>(pieces[1])};
    }
    public static List<Move> getPossibleMoves(TreeSet<Integer>[] pieces, int size, int plOnTurn) {
        List<Move> possibleMoves = new ArrayList<>();
        int dir = (1 - 2 * plOnTurn) * size;
        for (int piece : pieces[plOnTurn]) {
            int to = piece + dir;
            if (piece % size > 0 && !pieces[plOnTurn].contains(to - 1))
                possibleMoves.add(new Move(piece, to - 1));
            if (!pieces[1 - plOnTurn].contains(to) && !pieces[plOnTurn].contains(to))
                possibleMoves.add(new Move(piece, to));
            if (piece % size < size - 1 && !pieces[plOnTurn].contains(to + 1))
                possibleMoves.add(new Move(piece, to + 1));
        }
        return possibleMoves;
    }
    public int getPlOnTurn() {
        return plOnTurn;
    }
    private void set(Move move) {
        pieces[plOnTurn].remove(move.from);
        pieces[plOnTurn].add(move.to);
        plOnTurn = 1 - plOnTurn;
        pieces[plOnTurn].remove(move.to);
        possibleMoves = null;
        if (gameWin(size, move, pieces, plOnTurn))
            state = GameState.WIN;
    }
    private void findPossibleMoves() {
        if (possibleMoves != null)
            return;
        possibleMoves = getPossibleMoves(pieces, size, plOnTurn);
    }
    static boolean gameWin(int size, Move lastMove, TreeSet<Integer>[] pieces, int opponent) {
        int row = lastMove.to / size;
        int lastRow = (opponent) * (size - 1);
        return row == lastRow || pieces[opponent].isEmpty();
    }
    @Override
    public List<Position> getChildren() {
        findPossibleMoves();
        List<Position> children = new ArrayList<>(possibleMoves.size());
        for (int i = 0; i < possibleMoves.size(); i++)
            children.add(new PositionBreakthrough(this, i));
        return children;
    }

    @Override
    public boolean advanceToNext() {
        int childOrd = childOrdStack.peek();
        findPossibleMoves();
        if (childOrd >= possibleMoves.size())
            return false;

        Move move = possibleMoves.get(childOrd);
        boolean takes = pieces[1 - plOnTurn].contains(move.to);
        set(move);

        moveStack.push(new RevertableMove(move, takes));
        childOrdStack.push(0);
        return true;
    }

    @Override
    public void revertToParent() {
        childOrdStack.pop();
        childOrdStack.push(childOrdStack.pop() + 1);
        RevertableMove move = moveStack.pop();

        if (move.takes)
            pieces[plOnTurn].add(move.to);
        plOnTurn = 1 - plOnTurn;
        pieces[plOnTurn].remove(move.to);
        pieces[plOnTurn].add(move.from);
        possibleMoves = null;
        state = GameState.ONGOING;
    }

    @Override
    public GameState state() {
        return state;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PositionBreakthrough o) || size != o.size || plOnTurn != o.plOnTurn)
            return false;
        return pieces[0].equals(o.pieces[0]) && pieces[1].equals(o.pieces[1]);
    }

    @Override
    public String toString() {
        StringBuilder line = new StringBuilder("│\n├───");
        line.append("┼───".repeat(size - 1));
        line.append("┤\n");

        StringBuilder stringBuilder = new StringBuilder("┌───");
        stringBuilder.append("┬───".repeat( size - 1));
        stringBuilder.append("┐\n");

        for (int i = size * (size - 1); i >= 0; i -= size) {
            for (int j = 0; j < size; j++) {
                int pos = i + j;
                if (pieces[0].contains(pos))
                    stringBuilder.append("│ A ");
                else if (pieces[1].contains(pos))
                    stringBuilder.append("│ V ");
                else
                    stringBuilder.append("│   ");
            }
            if (i > 0)
                stringBuilder.append(line);
        }

        stringBuilder.append("│\n└───");
        stringBuilder.append("┴───".repeat(size - 1));
        stringBuilder.append("┘\n");
        return stringBuilder.toString();
    }
}
