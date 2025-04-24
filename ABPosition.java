import java.util.Set;

public interface ABPosition {
    void init(Position pos);
    boolean next(boolean leaf);
    void back(boolean leaf, double value);
    void initIterator();
    Object getMove();
    Position getPosition();
}
