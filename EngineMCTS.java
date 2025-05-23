import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;

public class EngineMCTS implements Engine{
    private static final double EXPLORATION_PARAM = 1;
    private static final Random rand = new Random();
    private final PositionEvaluator simulate;
    EngineMCTS(PositionEvaluator simulate) {
        this.simulate = simulate;
    }
    @Override
    public Object choseMove(Position position, long deadline) {
        Node root = new Node(null, position, null);
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        int count = 0;
        while (bean.getCurrentThreadCpuTime() < deadline) {
            Node node = selectNode(root);
            switch (node.position.state()) {
                case WIN -> backPropagate(node, 1);  //TODO -upgrade- remove parent node
                case DRAW -> backPropagate(node, 0);
                case ONGOING -> backPropagate(node, simulate.eval(node.position));
            }
            count++;
        }
        System.out.println("Simulations: " + count);
        return root.getBestMove();
    }

    private Node selectNode(Node node) {
        while (!node.children.isEmpty())
            node = node.selectBestChild();
        if (node.visits == 1) {
            node.expand();
            if (!node.children.isEmpty())
                node = node.children.get(rand.nextInt(node.children.size()));
        }
        return node;
    }

    private void backPropagate(Node node, double result) {
        while (node != null) {
            node.visits++;
            node.wins += result;
            node = node.parent;
            result = -result;
        }
    }
    private static class Node {
        Node parent;
        Position position; //TODO remove;
        Object move;
        double wins = 0;
        int visits = 0;
        List<Node> children = new ArrayList<>();

        Node(Node parent, Position position, Object move) {
            this.parent = parent;
            this.position = position;
            this.move = move;
        }

        void expand() {
            if (position.state() == GameState.ONGOING)
                for (Object move : position.moves())
                    children.add(new Node(this, position.copy().applyMove(move), move));
        }

        Node selectBestChild() {
            return children.stream()
                    .max(Comparator.comparingDouble(n -> n.wins / (n.visits + 1) +
                            EXPLORATION_PARAM * Math.sqrt(Math.log(visits + 1) / (n.visits + 1))))
                    .orElse(children.get(rand.nextInt(children.size())));
        }

        Object getBestMove() {
            return children.stream()
                    .max(Comparator.comparingDouble(n -> n.wins / (n.visits + 1)))
                    .map(n -> n.move)
                    .orElse(children.get(rand.nextInt(children.size())).move);
        }
    }
}
