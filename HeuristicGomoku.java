import java.util.ArrayList;
import java.util.List;

public class HeuristicGomoku implements PositionEvaluator{
    private static final int four = 25;
    private static final int _three__ = 10;
    private static final int _broken_three_ = 8;
    private static final int __three__ = 12;
    private static final int three__ = 6;
    private static final int __two__ = 4;
    private static final int _two___ = 3;
    private static final int _broken_two__ = 3;
    private static final int _broken__two_ = 2;
    private static final int __broken_two__ = 4;
    private static final int __two___ = 5;
    private static final int two___ = 1; // 1
    private static final int one = 0; // 1
    @Override
    public double eval(Position position) {
        if (!(position instanceof PositionGomoku pos))
            throw new IllegalArgumentException();

        int[] points = {0, 0};
        List<Line> lines = getLines(pos.board, pos.size);
        for (Line line : lines) {
            if (line.value == 0)
                continue;

            while (line.size >= 5) {
                int val = line.value & 0x3ff;
                if (val > 0) {
                    int player = 0;
                    if ((val & 0x2aa) == 0)
                        player = 1;
                    if ((val & 0x155) == 0)
                        player = 2;

                    if (player != 0) {
                        val /= player;
                        if (val == 0x55 || val == 0x115 || val == 0x145 || val == 0x151 || val == 0x154)
                            points[player - 1] += four;
                        if (val == 0x150 || val == 0x144 || val == 0x141 || val == 0x114 || val == 0x111 ||
                                val == 0x105 || val == 0x54 || val == 0x51 || val == 0x45 || val == 0x15)
                            points[player - 1] += three__;
                        if (val == 0x140 || val == 5)
                            points[player - 1] += two___;

                        if (line.size >= 6 && (line.value & 0xc00) == 0) {
                            if (val == 0x150 || val == 0x54)
                                points[player - 1] += _three__ - 2 * three__;
                            if (val == 0x144 || val == 0x114)
                                points[player - 1] += _broken_three_ - 2 * three__;
                            if (val == 0x50)
                                points[player - 1] += __two__;
                            if (val == 0x140 || val == 0x14)
                                points[player - 1] += _two___ - two___;
                            if (val == 0x110 || val == 0x44)
                                points[player - 1] += _broken_two__;
                            if (val == 0x104)
                                points[player - 1] += _broken__two_;
                            if (val == 0x40 || val == 0x10)
                                points[player - 1] += one;
                            
                            if (line.size >= 7 && (line.value & 0x3000) == 0) {
                                if (val == 0x150)
                                    points[player - 1] += __three__ - 2 * _three__ + three__;
                                if (val == 0x110)
                                    points[player - 1] += __broken_two__ - 2 * _broken_two__;
                                if (val == 0x140 || val == 0x50)
                                    points[player - 1] += __two___ - __two__ - _two___;
                                if (val == 0x40)
                                    points[player - 1] += -one;
                            }

                            if (line.size >= 8 && (line.value & 0xc000) == 0) {
                                val = (line.value & 0xffff) / player;
                                if (val == 0x1144)
                                    points[player - 1] += __three__ - 2 * _broken_three_;
                                if (val == 0x140)
                                    points[player - 1] += __two__ - __two___;
                            }
                        }
                    }
                }

                line.value >>= 2;
                line.size--;
            }
        }

        int pl = 3 - pos.actualPlayer();
        return (points[pl - 1] - points[2 - pl]) / (points[pl - 1] + points[2 - pl] + 1.0);
    }
    private static class Line {
        int value;
        int size;
        Line(int value, int size) {
            this.value = value;
            this.size = size;
        }
        void add(int value) {
            this.value += value;
        }
    }
    private static List<Line> getLines(int[] board, int size) {
        int goal = 5;
        int diagonals = 2 * (size - goal) + 1;
        List<Line> lines = new ArrayList<>(2 * (size + diagonals));
        for (int i = 0; i < size; i++)
            lines.add(new Line(board[i], size));
        for (int i = 0; i < size; i++)
            lines.add(new Line(0, size));
        for (int i = 0; i < 2; i++)
            for (int j = 0; j < diagonals; j++)
                lines.add(new Line(0, Integer.min(goal + j, goal + diagonals - j - 1)));

        for (PositionGomoku.Move move = new PositionGomoku.Move(); move != null; move = move.next(size)) {
            int value = PositionGomoku.get(board, move);
            if (value != 0) {
                lines.get(size + move.y).add(value << move.x * 2);
                if (move.x - move.y <= size - goal && move.y - move.x <= size - goal)
                    lines.get(3 * size - goal + move.x - move.y).add(value << Integer.min(move.x, move.y) * 2);
                if (move.x + move.y >= goal - 1 && move.x + move.y <= 2 * size - goal - 1)
                    lines.get(2 * size + diagonals - goal + 1 + move.x + move.y).add(value << Integer.min(move.x, size - 1 - move.y) * 2);
            }
        }
        return lines;
    }
}
