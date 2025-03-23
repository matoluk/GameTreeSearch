import java.util.List;
import java.util.Random;
import java.util.TreeSet;

public class SimulateBreakthrough implements PositionEvaluator{
    private static final Random rand = new Random();
    @Override
    public double eval(Position position) {
        if (!(position instanceof PositionBreakthrough pos))
            throw new IllegalArgumentException();

        int size = pos.getSize();
        TreeSet<Integer>[] pieces = pos.getPieces();
        int plOnTurn = pos.getPlOnTurn();

        int pl = plOnTurn;
        while (true) {
            List<PositionBreakthrough.Move> moves = PositionBreakthrough.getPossibleMoves(pieces, size, plOnTurn);
            PositionBreakthrough.Move move = moves.get(rand.nextInt(moves.size()));

            pieces[plOnTurn].remove(move.from);
            pieces[plOnTurn].add(move.to);
            plOnTurn = 1 - plOnTurn;
            pieces[plOnTurn].remove(move.to);

            if (PositionBreakthrough.gameWin(size, move, pieces, plOnTurn))
                return pl == plOnTurn ? 1 : -1;
        }
    }
}
