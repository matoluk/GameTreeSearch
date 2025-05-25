import java.util.*;

public class MCTSBreakthrough implements MCTSPosition{
    PositionBreakthrough positionOrig;
    PositionBreakthrough position;
    int size;
    int size2;
    int boardSize;
    int lastRow;
    int maxSimDeep;
    PositionEvaluator heuristic = new HeuristicBreakthrough();
    MCTSBreakthrough() {
        maxSimDeep = 177;
    }
    MCTSBreakthrough(int maxSimulationDeep) {
        maxSimDeep = maxSimulationDeep;
    }
    private static final Random rand = new Random();
    @Override
    public void init(Position pos) {
        if (!(pos instanceof PositionBreakthrough))
            throw new IllegalArgumentException("Expected PositionBreakthrough");

        positionOrig = (PositionBreakthrough) pos;
        size = positionOrig.size;
        size2 = 2 * size;
        boardSize = size * size;
        lastRow = boardSize - size;
    }

    @Override
    public void init() {
        position = new PositionBreakthrough(positionOrig);
    }

    @Override
    public void applyMove(Object move) {
        position.applyMove(move);
    }

    @Override
    public Map<Object, Double> getEvaluatedMoves() {
        Map<Object, Double> moves = new HashMap<>();
        for (Object move : position.moves())
            moves.put(move, 0.0);
        return moves;
    }

    @Override
    public double simulate() {
        int[] board = new int[boardSize];
        int[][] pieces = new int[2][];
        int[] piecesCount = new int[2];
        for (int pl = 0; pl < 2; pl++) {
            piecesCount[pl] = position.pieces[pl].size();
            pieces[pl] = new int[piecesCount[pl]];
            int sign = 1 - 2 * pl;
            int index = 0;
            for (int piece : position.pieces[pl]) {
                pieces[pl][index] = piece;
                index++;
                board[piece] = sign * index;
            }
        }

        int pl = position.actualPlayer;
        for (int deep = rand.nextInt(2); deep < maxSimDeep; deep++) {
            int sign = 1 - 2 * pl;
            List<Integer> dest = new ArrayList<>();
            int piece = 0;
            int from = 0;
            while (dest.isEmpty()) {
                piece = rand.nextInt(pieces[pl].length);
                from = pieces[pl][piece];
                if (from == -1)
                    continue;
                int forward = from + sign * size;
                if (forward >= lastRow || forward < size)
                    return pl == position.actualPlayer ? -1 : 1;
                int left = forward - 1;
                int right = forward + 1;
                if (from % size > 0 && board[left] * sign <= 0)
                    dest.add(left);
                if (board[forward] == 0)
                    dest.add(forward);
                if (from % size < size - 1 && board[right] * sign <= 0)
                    dest.add(right);
            }

            int to = dest.get(rand.nextInt(dest.size()));
            if (board[to] != 0) {
                if (--piecesCount[1 - pl] == 0)
                    return pl == position.actualPlayer ? -1 : 1;
                pieces[1 - pl][-sign * board[to] - 1] = -1;
            }
            pieces[pl][piece] = to;
            board[to] = sign * (piece + 1);
            board[from] = 0;
            pl = 1 - pl;
        }

        //position.actualPlayer = 1 - pl;
        for (pl = 0; pl < 2; pl++) {
            position.pieces[pl].clear();
            for (int piece : pieces[pl])
                if (piece != -1)
                    position.pieces[pl].add(piece);
        }
        double score = heuristic.eval(position);

        if (score == 0)
            return 0;
        if (score > 0.25)
            return 1;
        if (score < -0.25)
            return -1;
        return score * 4;
    }

    @Override
    public Position getPosition() {
        return position;
    }
}
