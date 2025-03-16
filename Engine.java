public interface Engine {
    Position choseMove(Position position, long deadlineNano);
}
