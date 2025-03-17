import java.util.List;
import java.util.Random;

public class SimulateGomoku implements PositionEvaluator{
    private static final Random rand = new Random();
    @Override
    public double eval(Position position) {
        if (!(position instanceof PositionGomoku pos))
            throw new IllegalArgumentException();

        int size = pos.getSize();
        int[] board = pos.getBoard();
        int plOnTurn = pos.getPlOnTurn();
        List<PositionGomoku.Move> moves = pos.getPossibleMoves();

        int pl = 3 - plOnTurn;
        while (true) {
            int moveId = rand.nextInt(moves.size());
            PositionGomoku.Move move = moves.get(moveId);
            int lastMoveId = moves.size() - 1;
            moves.set(moveId, moves.get(lastMoveId));
            moves.remove(lastMoveId);

            PositionGomoku.set(board, move, plOnTurn);
            if (PositionGomoku.gameWin(size, board, move))
                return pl == plOnTurn ? 1 : -1;
            if (moves.isEmpty())
                return 0;
            plOnTurn = 3 - plOnTurn;
        }
    }
}
