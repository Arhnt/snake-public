package com.codenjoy.dojo.snakebattle.strategy.turn;

/*-
 * #%L
 * Codenjoy - it's a dojo-like platform from developers to developers.
 * %%
 * Copyright (C) 2018 - 2019 Codenjoy
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.snakebattle.client.Board;
import lombok.extern.log4j.Log4j2;

import java.util.*;

import static com.codenjoy.dojo.snakebattle.model.Elements.*;

@Log4j2
public class TurnBasedStrategy {

    Map<Node, Double> visitedNodes = new HashMap<>();
    Queue<Node> nodesToVisit = new ArrayDeque<>();
    private Direction[] directions = {Direction.UP, Direction.LEFT, Direction.RIGHT, Direction.DOWN};

    public List<Direction> nextAction(Board board) {
        Node left = getNodeAt(board, Direction.LEFT, null);
        Node right = getNodeAt(board, Direction.RIGHT, null);
        Node up = getNodeAt(board, Direction.UP, null);
        Node down = getNodeAt(board, Direction.DOWN, null);

        nodesToVisit.add(left);
        nodesToVisit.add(right);
        nodesToVisit.add(up);
        nodesToVisit.add(down);

        bfs(500);

        Map<Direction, Double> results = new HashMap<>();
        results.put(Direction.LEFT, left.getValue());
        results.put(Direction.RIGHT, right.getValue());
        results.put(Direction.DOWN, down.getValue());
        results.put(Direction.UP, up.getValue());

        log.info("Reward results: {}", results);
        return Collections.singletonList(results.entrySet()
            .stream()
            .max(Comparator.comparingDouble(Map.Entry::getValue))
            .map(Map.Entry::getKey)
            .get());
    }

    private void bfs(long timeLimit) {
        long start = System.currentTimeMillis();
        while (!nodesToVisit.isEmpty() && (System.currentTimeMillis() - start) < timeLimit) {
            Node nodeToVisit = nodesToVisit.poll();
            Board board = (Board) new Board().forString(nodeToVisit.getBoard());
            double reward = calculateReward(board, nodeToVisit.getDirection());
            nodeToVisit.propagateValue(reward, 1);

            if (reward >= 0) {
                Arrays.stream(directions)
                    .filter(direction -> direction != nodeToVisit.getDirection().inverted())
                    .map(direction -> getNodeAt(board, direction, nodeToVisit))
                    .forEach(nodesToVisit::add);
            }
        }
    }

    private Double calculateReward(Board board, Direction direction) {
        if (!board.canMove(direction)) {
            return -20d;
        }

        double reward = 0;
//        Point newHead = board.getAt(direction);
//
//        if (board.isAt(newHead, APPLE)) {
//            reward = 1d;
//            board.moveSnake(direction, 0);
//        } else if (board.isAt(newHead, GOLD)) {
//            reward = 5d;
//            board.moveSnake(direction, 1);
//        } else if (board.isAt(newHead, STONE) && board.getMyFurry() > 0) {
//            reward = 10d;
//            board.moveSnake(direction, 1);
//        } else if (board.isAt(newHead, STONE)) {
//            reward = 7d;
//            board.moveSnake(direction, 4);
//        } else if (board.isAt(newHead, FURY_PILL)) {
//            reward = 0.2d;
//            board.moveSnake(direction, 1);
//            board.set(newHead, HEAD_EVIL);
//        } else if (board.isAt(newHead, FLYING_PILL)) {
//            reward = 0.1d;
//            board.moveSnake(direction, 1);
//            board.set(newHead, HEAD_FLY);
//        } else {
//            board.moveSnake(direction, 1);
//        }

        return reward;
    }

    private Node getNodeAt(Board board, Direction direction, Node parent) {
        Node node = Node.builder()
            .point(direction.change(board.getMe()))
            .direction(direction)
            .snakeLength(board.getMySnakeLength())
            .furry(board.getMyFurry())
            .flight(board.getMyFlight())
            .board(board.boardAsString())
            .parents(new ArrayList<>())
            .leaves(new ArrayList<>())
            .build();
        if (parent != null) {
            node.getParents().add(parent);
        }
        return node;
    }


}
