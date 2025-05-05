import java.util.Collection;
import java.util.List;

public interface MCTSPosition {
    void init(Position pos);
    void init();
    void applyMove(Object move);
    Collection<Object> getMoves(); //todo type?
    double simulate();
    Position getPosition();
}
