import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class GomokuRelatedMovesTesting {
    public static final ThreadMXBean bean = ManagementFactory.getThreadMXBean();
    static int deep = 4;
    public static void main(String[] args) {
        //Set<PositionGomoku> positions = (Set<PositionGomoku>) read("setGomokuPositions");
        //Set<PositionGomoku> positions = (Set<PositionGomoku>) read("2v3deep1");
        //Set<PositionGomoku> positions = (Set<PositionGomoku>) read("2v3deep2");
        Set<PositionGomoku> positions = (Set<PositionGomoku>) read("2v3deep3");

        System.out.println("Positions count: " + positions.size());

        //Set<PositionGomoku> out = new HashSet<>();

        EngineABGivenDeep[] engines = {
                new EngineABGivenDeep(new HeuristicGomoku(), new ABGomokuRelatedMoves()),
                new EngineABGivenDeep(new HeuristicGomoku(), new ABGomokuRelatedMoves(3))
        };
        int sameRes = 0;
        int betterRes = 0;
        int tests = 0;
        long[] time = {0, 0};

        boolean smallSet = positions.size() < 200;
        for (PositionGomoku pos : positions) {
            if (smallSet)
                System.out.println(pos);
            else if (tests % 100 == 0)
                System.out.println("Count: " + tests);

            List<TreeSet<PositionGomoku.Move>> moves = List.of(new TreeSet<>(), new TreeSet<>());
            long[] durations = new long[2];
            for (int i = 0; i < 2; i++) {
                long startTime = bean.getCurrentThreadCpuTime();
                List<Object> bestMoves = engines[i].choseMove(pos.copy(), deep);
                durations[i] = bean.getCurrentThreadCpuTime() - startTime;
                time[i] += durations[i];
                for (Object m : bestMoves)
                    moves.get(i).add((PositionGomoku.Move) m);
                if (smallSet) {
                    System.out.println("Turn duration: " + durations[i] / 1_000_000.0 + "ms");
                    System.out.println("Moves: " + moves.get(i));
                }
            }
            tests++;

            if (moves.get(1).equals(moves.get(0))) {
                sameRes++;
            }
            else {
                //out.add(pos);
                if (!smallSet) {
                    System.out.println(pos);
                    for (int i = 0; i < 2; i++)
                        System.out.println("Moves: " + moves.get(i));
                }
                if (moves.get(0).isEmpty() || !moves.get(1).contains(moves.get(0).first()))
                    betterRes++;
            }

            if (smallSet) {
                for (int d = deep - 1; d >= 1; d--) {
                    System.out.println("Deep: " + d);
                    for (int i = 0; i < 2; i++) {
                        List<Object> bestMoves = engines[i].choseMove(pos.copy(), d);
                        TreeSet<PositionGomoku.Move> mvs = new TreeSet<>();
                        for (Object move : bestMoves)
                            mvs.add((PositionGomoku.Move) move);
                        System.out.println("Moves: " + mvs);
                    }
                }
            }
        }

        System.out.println("Success: " + sameRes + " / " + tests);
        System.out.println("Found better: " + betterRes);
        System.out.println("Time: " + time[0] / 1_000_000.0 + ", " + time[1] / 1_000_000.0);
        //write(out, "2v3deep5fromD3");
    }
    static void write(Object obj, String fileName) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName + ".dat"))) {
            out.writeObject(obj);
            System.out.println("File wrote.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    static Object read(String fileName) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName + ".dat"))) {
            return in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}