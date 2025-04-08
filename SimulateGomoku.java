import java.util.List;
import java.util.Random;

public class SimulateGomoku implements PositionEvaluator{
    private static final Random rand = new Random();
    @Override
    public double eval(Position position) {
        if (!(position instanceof PositionGomoku))
            throw new IllegalArgumentException();

        PositionGomoku pos = new PositionGomoku((PositionGomoku) position);
        List<Object> moves = pos.moves();

        int pl = pos.actualPlayer();
        while (pos.state == GameState.ONGOING) {
            int index = rand.nextInt(moves.size());
            pos.applyMove(moves.get(index));

            int lastIndex = moves.size() - 1;
            moves.set(index, moves.get(lastIndex));
            moves.remove(lastIndex);
        }
        if (pos.state == GameState.WIN)
            return pl == pos.actualPlayer() ? 1 : -1;
        return 0;
    }
}
