import java.util.Map;

public interface MCTSPosition {
    void init(Position pos);
    void init();
    void applyMove(Object move);
    Map<Object, Double> getEvaluatedMoves(); //todo type?
    double simulate();
    Position getPosition();
}
