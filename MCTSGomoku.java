import java.util.*;
import java.util.function.IntUnaryOperator;

public class MCTSGomoku implements MCTSPosition{
    PositionGomoku positionOrig;
    PositionGomoku position;
    int[] movesPriorityOrig;
    int[] movesPriority;
    int[] movesEvalOrig;
    int[] movesEval;
    IntUnaryOperator f;
    int maxSimulationLength;
    long sim = 0;
    int distance;
    private static final Random rand = new Random();
    private static final byte[] BIT_COUNT_POW2_5BIT = new byte[32];
    static {
        for (int i = 0; i < 32; i++) {
            int x = Integer.bitCount(i);
            BIT_COUNT_POW2_5BIT[i] = (byte) (x * x);
        }
    }
    MCTSGomoku(IntUnaryOperator function, int maxSimulationLength) {
        distance = 2;
        f = function;
        this.maxSimulationLength = maxSimulationLength;
    }
    int moveToInt(PositionGomoku.Move move) {
        return move.x * positionOrig.size + move.y;
    }
    int indexOf(PositionGomoku.Move move) {
        return movesPriorityOrig.length / 2 + move.x * positionOrig.size + move.y;
    }
    PositionGomoku.Move moveAt(int index) {
        index -= movesPriorityOrig.length / 2;
        return new PositionGomoku.Move(index / positionOrig.size, index % positionOrig.size);
    }
    boolean isRelated(PositionGomoku.Move move) {
        PositionGomoku.Move from = new PositionGomoku.Move(
                Integer.max(0, move.x - distance),
                Integer.max(0, move.y - distance));
        PositionGomoku.Move to = new PositionGomoku.Move(
                Integer.min(positionOrig.size - 1, move.x + distance),
                Integer.min(positionOrig.size - 1, move.y + distance));
        PositionGomoku.Move m = new PositionGomoku.Move(from.x, from.y);
        while(true) {
            if (positionOrig.get(m) != 0)
                return true;
            m.x++;
            if (m.x > to.x) {
                m.x = from.x;
                m.y++;
                if (m.y > to.y)
                    return false;
            }
        }
    }
    int eval(PositionGomoku.Move move) {
        int points = 0;
        for (PositionGomoku.Move dir : List.of(new PositionGomoku.Move(1,0), new PositionGomoku.Move(0,1), new PositionGomoku.Move(1,1), new PositionGomoku.Move(1,-1))) {
            int cursor = 0;
            int emptyFrom = 0;
            int player = 0;
            int state = 0;
            PositionGomoku.Move end = new PositionGomoku.Move(move, 5, dir);
            for (PositionGomoku.Move square = new PositionGomoku.Move(move, -4, dir); !square.equals(end); square.add(dir)) {
                if (square.isValid(positionOrig.size)) {
                    int pl = positionOrig.get(square);
                    if (pl == 0)
                        cursor++;
                    else {
                        if (player == 0)
                            player = pl;
                        if (pl != player) {
                            cursor -= emptyFrom;
                            state = 0;
                            player = pl;
                        }
                        state |= 1 << cursor;
                        cursor++;
                        emptyFrom = cursor;
                    }
                    if (cursor == 5) {
                        points += BIT_COUNT_POW2_5BIT[state];
                        cursor--;
                        if (emptyFrom > 0)
                            emptyFrom--;
                        state >>= 1;
                    }
                }
            }
        }
        return points;
    }
    @Override
    public void init() {
        position = new PositionGomoku(positionOrig);
        movesEval = Arrays.copyOf(movesEvalOrig, movesEvalOrig.length);
        movesPriority = Arrays.copyOf(movesPriorityOrig, movesPriorityOrig.length);
    }
    @Override
    public void init(Position pos) {
        if (!(pos instanceof PositionGomoku))
            throw new IllegalArgumentException("Expected PositionGomoku");

        positionOrig = (PositionGomoku) pos;
        int movesCount = positionOrig.size * positionOrig.size;
        movesEvalOrig = new int[movesCount];
        movesPriorityOrig = new int[(Integer.highestOneBit(movesCount - 1) << 2) - 1];

        if (positionOrig.deep == 0)
            movesPriorityOrig[indexOf(new PositionGomoku.Move(positionOrig.size/2, positionOrig.size/2))] = 1;
        else {
            for (PositionGomoku.Move m = new PositionGomoku.Move(); m != null; m = m.next(positionOrig.size)) {
                if (positionOrig.get(m) == 0) {
                    int value = eval(m);
                    movesEvalOrig[moveToInt(m)] = value;
                    if (isRelated(m))
                        movesPriorityOrig[indexOf(m)] = f.applyAsInt(value) + 1;
                }
            }
        }
        for (int i = movesPriorityOrig.length - 2; i >= 0; i -= 2)
            movesPriorityOrig[i / 2] = movesPriorityOrig[i] + movesPriorityOrig[i + 1];
    }
    void updatePriority(int index, int value) {
        movesPriority[index] = value;
        if (index > 0) {
            value += movesPriority[index + (index & 1) * 2 - 1];
            updatePriority((index - 1) / 2, value);
        }
    }
    void addRelated(PositionGomoku.Move move) {
        PositionGomoku.Move from = new PositionGomoku.Move(
                Integer.max(0, move.x - distance),
                Integer.max(0, move.y - distance));
        PositionGomoku.Move to = new PositionGomoku.Move(
                Integer.min(position.size - 1, move.x + distance),
                Integer.min(position.size - 1, move.y + distance));
        PositionGomoku.Move m = new PositionGomoku.Move(from.x, from.y);
        while(true) {
            if (position.get(m) == 0) {
                int index = indexOf(m);
                if (movesPriority[index] == 0)
                    updatePriority(index, f.applyAsInt(movesEval[moveToInt(m)]) + 1);
            }
            m.x++;
            if (m.x > to.x) {
                m.x = from.x;
                m.y++;
                if (m.y > to.y)
                    break;
            }
        }
    }
    void updateEval(PositionGomoku.Move move) {
        int player = position.get(move);
        int opponent = 3 - player;
        for (PositionGomoku.Move dir : List.of(new PositionGomoku.Move(1,0), new PositionGomoku.Move(0,1), new PositionGomoku.Move(1,1), new PositionGomoku.Move(1,-1))) {
            int[] myRange = {5, 5};
            int[] opRange = {5, 5};
            List<Integer> opStones = new ArrayList<>();
            for (int i = 0; true; i++) {
                PositionGomoku.Move m = new PositionGomoku.Move(move, 1, dir);
                for (int j = 1; true; j++) {
                    if (!m.isValid(position.size)) {
                        if (myRange[i] == 5)
                            myRange[i] = j;
                        if (opRange[i] == 5)
                            opRange[i] = j;
                        break;
                    }

                    int pl = position.get(m);
                    if (pl == player && opRange[i] == 5)
                        opRange[i] = j;
                    if (pl == opponent) {
                        if (myRange[i] == 5)
                            myRange[i] = j;
                        if (opRange[i] == 5)
                            opStones.add(4 + j * (i * 2 - 1));
                    }

                    if (j == 4 || (myRange[i] != 5 && opRange[i] != 5))
                        break;
                    m.add(dir);
                }
                if (i == 1)
                    break;
                dir.x = -dir.x;
                dir.y = -dir.y;
            }

            int[] score = new int[9];
            if (myRange[0] + myRange[1] > 5) {
                int start = 5 - myRange[0];
                int end = 5 + myRange[1];
                int inc = end - 5;
                int dec = start + 5;
                int value = 0;
                for (int i = start; i < end - 1; i++) {
                    if (i < inc)
                        value++;
                    else if (i >= dec)
                        value--;
                    score[i] = value;
                }
            }
            if (opRange[0] + opRange[1] > 5 && !opStones.isEmpty()) {
                int[] acceleration = new int[11];
                int start = 5 - opRange[0];
                int end = 5 + opRange[1];
                for (int stone : opStones) {
                    int from = Integer.max(start, stone - 4);
                    int to = Integer.min(end, stone + 6);
                    acceleration[from]++;
                    acceleration[to - 5]--;
                    acceleration[from + 5]--;
                    acceleration[to]++;
                }
                int acc = 0;
                int value = 0;
                for (int i = start; i < end - 1; i++) {
                    acc += acceleration[i];
                    value += acc;
                    score[i] -= value;
                }
            }

            for (int i = 0; i < 9; i++) {
                if (score[i] != 0) {
                    PositionGomoku.Move m = new PositionGomoku.Move(move, i - 4, dir);
                    if (position.get(m) == 0) {
                        int evalIndex = moveToInt(m);
                        int index = indexOf(m);
                        movesEval[evalIndex] += score[i];
                        if (movesPriority[index] != 0)
                            updatePriority(index, f.applyAsInt(movesEval[evalIndex]) + 1);
                    }
                }
            }
        }
    }
    @Override
    public void applyMove(Object move) {
        position.applyMove(move);
        PositionGomoku.Move m = (PositionGomoku.Move) move;
        updateEval(m);
        addRelated(m);
        updatePriority(indexOf(m), 0);
    }

    void add(int i, Map<Object, Double> moves) {
        if (movesPriority[i] == 0)
            return;
        if (2 * i + 1 >= movesPriority.length)
            moves.put(moveAt(i), movesPriority[i] / (double) Integer.MAX_VALUE);
        else {
            add(2 * i + 1, moves);
            add(2 * i + 2, moves);
        }
    }
    @Override
    public Map<Object, Double> getEvaluatedMoves() {
        Map<Object, Double> moves = new HashMap<>();
        add(0, moves);
        return moves;
    }

    @Override
    public double simulate() {
        int simStep = 0;
        for (; simStep < maxSimulationLength && position.state == GameState.ONGOING; simStep++) {
            int r = rand.nextInt(movesPriority[0]);
            int i = 0;
            while (i * 2 + 1 < movesPriority.length) {
                i = i * 2 + 1;
                if (movesPriority[i] <= r) {
                    r -= movesPriority[i];
                    i++;
                }
            }
            applyMove(moveAt(i));
            sim++;
        }
        if (position.state == GameState.WIN)
            return  1 - (simStep & 1) * 2;
        return 0;
    }

    @Override
    public Position getPosition() {
        return position;
    }
}
