package com.codenjoy.dojo.snakebattle.strategy.distant;

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
import com.codenjoy.dojo.services.PointImpl;
import com.codenjoy.dojo.snakebattle.client.Board;
import com.codenjoy.dojo.snakebattle.client.Snake;
import com.codenjoy.dojo.snakebattle.model.Elements;
import com.google.common.collect.Sets;
import javafx.util.Pair;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.stream.Collectors;

import static com.codenjoy.dojo.services.Direction.*;
import static com.codenjoy.dojo.snakebattle.model.Elements.*;

@Log4j2
public class BoardUtils {

    private static Direction[] directions = {UP, LEFT, RIGHT, DOWN};
    public static Set<Point> deadEnds = Sets.newHashSet(
        new PointImpl(23, 10),
        new PointImpl(19, 10),
        new PointImpl(21, 10),
        new PointImpl(8, 20),
        new PointImpl(9, 20),
        new PointImpl(8, 22),
        new PointImpl(9, 22)
    );

    // find equal moves from A to B
    public static Set<Direction> findMovesFromTo(Board board, Point from, Point to) {
        return  findMovesFromToAndStartDirection(board, from, to).getValue();
    }

    public static Pair<Integer, Set<Direction>> findMovesFromToAndStartDirection(Board board, Point from, Point to) {
        long start = System.currentTimeMillis();
        log.debug("Finding route from {} to {}", from, to);
        List<Point> mySnake = board.getMySnakeBody();
        List<Pair<Direction, Integer>> stepsByDirection = Arrays.stream(directions)
            .map(d -> new Pair<>(d, d.change(from)))
            .filter(p -> p.getValue().getX() >= 0)
            .filter(p -> p.getValue().getY() >= 0)
            .filter(p -> p.getValue().getX() <= 29)
            .filter(p -> p.getValue().getY() <= 29)
            .filter(p -> !board.isAt(p.getValue(), Elements.WALL))
            .filter(p -> canEatStone(board, p.getValue()))
            .filter(p -> !p.getValue().equals(mySnake.get(1))) // filter neck
            .map(p -> new Pair<>(p.getKey(), countMoves(board, p.getValue(), to)))
            .filter(p -> p.getValue() >= 0)
            .sorted(Comparator.comparingInt(Pair::getValue))
            .collect(Collectors.toList());

        int minSteps = stepsByDirection.isEmpty() ? -1 : stepsByDirection.get(0).getValue();
        Set<Direction> possibleDirections = stepsByDirection
            .stream()
            .filter(p -> p.getValue() == minSteps)
            .map(Pair::getKey)
            .collect(Collectors.toSet());

        log.debug("findMovesFromTo took {}ms", System.currentTimeMillis() - start);
        return new Pair<>(minSteps, possibleDirections);
    }

    private static boolean canEatStone(Board board, Point point) {
        if (!board.isAt(point, Elements.STONE)) {
            return true;
        }
        return (board.getMyFurry() > 0 || board.getMySnakeLength() > 5);
    }

    public static int countMoves(Board board, Point from, Point to) {
        PriorityQueue<Node> nodesToVisit = new PriorityQueue<>(Comparator.comparingInt(n -> n.movesToDestination + n.level));
        nodesToVisit.add(new Node(from, 0, distance(from, to)));

        Set<Point> addedPoints = new HashSet<>();
        addedPoints.add(from);

        while (!nodesToVisit.isEmpty()) {
            Node node = nodesToVisit.poll();
            if (node.point.equals(to)) {
                return node.level;
            }
            if (board.isAt(node.point, Elements.WALL, Elements.ENEMY_HEAD_SLEEP, Elements.START_FLOOR, Elements.ENEMY_TAIL_INACTIVE)) {
                continue;
            }
            Arrays.stream(directions)
                .map(d -> d.change(node.point))
                .filter(p -> p.getX() >= 0)
                .filter(p -> p.getY() >= 0)
                .filter(p -> p.getX() <= 29)
                .filter(p -> p.getY() <= 29)
                .filter(p -> !addedPoints.contains(p))
                .forEach(p -> {
                    addedPoints.add(p);
                    nodesToVisit.add(new Node(p, node.level + 1, distance(p, to)));
                });
        }
        return -1;
    }

    public static Map<Point, Integer> calcDistanceMatrix(Board board) {
        return calcDistanceMatrix(board, board.getMe());
    }

    public static Map<Point, Integer> calcDistanceMatrix(Board board, Point startingPoint) {
        return calcDistanceMatrix(board, startingPoint, new HashSet<>());
    }

    public static Map<Point, Integer> calcDistanceMatrix(Board board, Point startingPoint, Set<Point> ignore) {
        Map<Point, Integer> pointsByDistance = new HashMap<>();
        Queue<Node> nodesToVisit = new ArrayDeque<>();
        nodesToVisit.add(new Node(startingPoint, 0));

        Set<Point> visitedPoints = new HashSet<>();
        visitedPoints.add(startingPoint);
        visitedPoints.addAll(ignore);
        visitedPoints.addAll(deadEnds);

        while (!nodesToVisit.isEmpty()) {
            Node node = nodesToVisit.poll();
            if (board.isAt(node.point, Elements.WALL, Elements.ENEMY_HEAD_SLEEP, Elements.START_FLOOR, Elements.ENEMY_TAIL_INACTIVE)) {
                continue;
            }
            if (node.point != startingPoint && board.isAt(node.point, Elements.APPLE, Elements.GOLD, Elements.STONE, Elements.FURY_PILL)) {
                pointsByDistance.put(node.point, node.level);
            }
            Arrays.stream(directions)
                .map(d -> d.change(node.point))
                .filter(p -> p.getX() >= 0)
                .filter(p -> p.getY() >= 0)
                .filter(p -> p.getX() <= 29)
                .filter(p -> p.getY() <= 29)
                .filter(p -> !visitedPoints.contains(p))
                .forEach(p -> {
                    visitedPoints.add(p);
                    nodesToVisit.add(new Node(p, node.level + 1));
                });
        }
        return pointsByDistance;
    }

    // calculates manhattan distance
    public static int distance(Point from, Point point) {
        return Math.abs(from.getX() - point.getX()) + Math.abs(from.getY() - point.getY());
    }

    public static List<Snake> findEnemies(Board board) {
        List<Point> enemyHeads = board.get(ENEMY_HEAD_DOWN, ENEMY_HEAD_LEFT, ENEMY_HEAD_RIGHT, ENEMY_HEAD_UP, ENEMY_HEAD_EVIL, ENEMY_HEAD_FLY);
        return enemyHeads.stream()
            .map(head -> BoardUtils.findEnemySnake(board, head))
            .collect(Collectors.toList());
    }

    public static Snake findEnemySnake(Board board, Point head) {
        Snake snake = new Snake();
        List<Point> body = new ArrayList<>();
        body.add(head);
        if (board.isAt(head, ENEMY_HEAD_EVIL, ENEMY_HEAD_FLY)) {
            if (board.isAt(head, ENEMY_HEAD_EVIL)) {
                snake.setFurry(1);
            }
            Direction nextDirection = findNearestBodyPart(board, head);
            Point nextPoint = nextDirection == null ? null : nextDirection.change(head);
            while (nextPoint != null && (board.isAt(nextPoint, Board.ENEMY_BODY) || board.isAt(nextPoint, Board.ENEMY_TAIL))) {
                body.add(nextPoint);
                nextDirection = getDirectionByEnemyBody(board.getAt(nextPoint), nextDirection.inverted());
                nextPoint = nextDirection == null ? null : nextDirection.change(nextPoint);
            }
        } else if (board.isAt(head, Board.ENEMY_HEAD)) {
            Direction nextDirection = getDirectionByEnemyHead(board.getAt(head));
            Point nextPoint = nextDirection.change(head);
            while (nextPoint != null && (board.isAt(nextPoint, Board.ENEMY_BODY) || board.isAt(nextPoint, Board.ENEMY_TAIL))) {
                body.add(nextPoint);
                nextDirection = getDirectionByEnemyBody(board.getAt(nextPoint), nextDirection.inverted());
                nextPoint = nextDirection == null ? null : nextDirection.change(nextPoint);
            }
        }

        snake.setBody(body);
        return snake;
    }

    private static Direction findNearestBodyPart(Board board, Point head) {
        return Arrays.stream(directions)
            .filter(direction ->
                (direction == LEFT && board.isAt(direction.change(head), ENEMY_BODY_HORIZONTAL, ENEMY_BODY_RIGHT_DOWN, ENEMY_BODY_RIGHT_UP,ENEMY_TAIL_END_LEFT )) ||
                (direction == RIGHT && board.isAt(direction.change(head), ENEMY_BODY_HORIZONTAL, ENEMY_BODY_LEFT_DOWN, ENEMY_BODY_LEFT_UP,ENEMY_TAIL_END_RIGHT )) ||
                (direction == UP && board.isAt(direction.change(head), ENEMY_BODY_VERTICAL, ENEMY_BODY_LEFT_DOWN, ENEMY_BODY_RIGHT_DOWN, ENEMY_TAIL_END_UP)) ||
                (direction == DOWN && board.isAt(direction.change(head), ENEMY_BODY_VERTICAL, ENEMY_BODY_LEFT_UP, ENEMY_BODY_RIGHT_UP, ENEMY_TAIL_END_DOWN))
            )
            .findFirst()
            .orElse(null);
    }

    private static Direction getDirectionByEnemyHead(Elements head) {
        switch (head) {
            case ENEMY_HEAD_DOWN:
                return Direction.UP;
            case ENEMY_HEAD_LEFT:
                return RIGHT;
            case ENEMY_HEAD_RIGHT:
                return LEFT;
            case ENEMY_HEAD_UP:
                return DOWN;
            default:
                throw new IllegalArgumentException("Don't know how to handle " + head);
        }
    }

    private static Direction getDirectionByEnemyBody(Elements body, Direction toHead) {
        switch (body) {
            case ENEMY_BODY_HORIZONTAL:
                return toHead == LEFT ? RIGHT : LEFT;
            case ENEMY_BODY_VERTICAL:
                return toHead == UP ? DOWN : UP;
            case ENEMY_BODY_LEFT_DOWN:
                return toHead == LEFT ? DOWN : LEFT;
            case ENEMY_BODY_LEFT_UP:
                return toHead == LEFT ? UP : LEFT;
            case ENEMY_BODY_RIGHT_DOWN:
                return toHead == RIGHT ? DOWN : RIGHT;
            case ENEMY_BODY_RIGHT_UP:
                return toHead == RIGHT ? UP : RIGHT;
            default:
                return null;
        }
    }


    @Data
    @RequiredArgsConstructor
    @AllArgsConstructor
    public static class Node {
        private final Point point;
        private final int level;
        private int movesToDestination;
    }
}
