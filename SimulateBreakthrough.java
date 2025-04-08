import java.util.List;
import java.util.Random;

public class SimulateBreakthrough implements PositionEvaluator{
    private static final Random rand = new Random();
    @Override
    public double eval(Position position) {
        if (!(position instanceof PositionBreakthrough))
            throw new IllegalArgumentException();

        PositionBreakthrough pos = new PositionBreakthrough((PositionBreakthrough) position);
        int pl = pos.actualPlayer;
        while (pos.state == GameState.ONGOING) {
            List<Object> moves = pos.moves();
            Object move = moves.get(rand.nextInt(moves.size()));
            pos.applyMove(move);
        }
        return pl == pos.actualPlayer ? 1 : -1;
    }
}
