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
import com.codenjoy.dojo.snakebattle.client.Board;
import com.codenjoy.dojo.snakebattle.client.Snake;
import com.codenjoy.dojo.snakebattle.model.Elements;
import com.google.common.collect.Sets;
import lombok.*;
import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

// TODO: regard others positions
@Log4j2
public class DistantStrategy {

    private static int MAX_MOVES = 30;
    public static Comparator<State> STATE_COMPARATOR =
        Comparator.comparingInt(State::getBucket)
            .thenComparing(Comparator.comparingDouble(State::getRewardPerMove).reversed());
    private Map<Snake, Map<Point, Integer>> enemyDistances;
//        Comparator
//        .comparingDouble(State::getRewardPerMove)
//        .reversed()
//        .thenComparing(State::getMoves)
//        .thenComparing(s -> s.getVisited().size());

    @SneakyThrows
    public Set<Direction> nextActions(Board board) {
        long start = System.currentTimeMillis();

        enemyDistances = board.getEnemies().stream()
            .collect(toMap(Function.identity(), snake -> BoardUtils.calcDistanceMatrix(board, snake.getHead())));

        // iterating heap
        PriorityBlockingQueue<State> heap = new PriorityBlockingQueue<>(300, STATE_COMPARATOR);
        heap.add(buildInitialState(board)); // initialize heap

        int counter = 0;
        Map<Integer, State> winners = new HashMap<>();
        while (System.currentTimeMillis() - start < 200 && !heap.isEmpty()) {
            // pick node
            State state = heap.poll();
            Map<Point, Integer> childs = BoardUtils.calcDistanceMatrix(board, state.point, state.getVisited());
            if (childs.isEmpty()) {
                continue;
            }
            counter = counter + childs.size();
            childs.entrySet()
                .stream()
                .parallel()
                .map(poi -> calculateReward(board, poi.getKey(), state, poi.getValue()))
                .filter(Objects::nonNull)
                .filter(newState -> newState.getMoves() <= MAX_MOVES)
                .peek(newState -> {
                    if ((winners.get(newState.getBucket()) == null) || (winners.get(newState.getBucket()).getRewardPerMove() < newState.getRewardPerMove())) {
                        winners.put(newState.getBucket(), newState);
                    }
                })
                .forEach(heap::add);
        }

        State winner = null;
        if (winners.size() == 0) {
            log.warn("Cannot find any route for:\n{}", board.boardAsString());
            return Sets.newHashSet();
        } else {
            Integer topKey = Collections.max(winners.keySet());
            winner = winners.get(topKey);
        }

        Point firstMove = winner.getVisited().iterator().next();
        log.info("Best strategy to move to {} with rewards {} per move, calculation took {}ms, iterated {} nodes, depth {} moves",
            firstMove, winner.rewardPerMove, (System.currentTimeMillis() - start), counter, winner.getVisited().size());
        log.info("Points: {}", winner.getVisited());
        Set<Direction> directions = BoardUtils.findMovesFromTo(board, board.getMe(), firstMove);
        log.info("Can move via: {}", directions);
        return directions;
    }

    private State calculateReward(Board board, Point point, State state, int movesToPoint) {
        int moves = state.getMoves() + movesToPoint;
        int length = state.getLength();
        int furry = state.getFurry() - movesToPoint;
        furry = furry < 0 ? 0 : furry;

        double reward = 0;
        if (board.isAt(point, Elements.APPLE)) {
            length = length + 1;
            reward = 1;
        } else if (board.isAt(point, Elements.GOLD)) {
            reward = 10;
        } else if (board.isAt(point, Elements.FURY_PILL)) {
            furry = furry + 9;
            reward = 5;
        } else if (board.isAt(point, Elements.STONE) && (length > 5 || furry > 0)) {
            if (furry > 0) {
                reward = 5;
            } else if (board.getLongestEnemy() + 5 < state.length) {
                reward = 3;
            } else {
                reward = -1;
                length = length - 3;
            }
        } else if (board.isAt(point, Elements.STONE)) {
            return null;
        } else {
            reward = 0;
        }

        // distance multiplies
        reward = reward * Math.pow(0.95, moves);
        // enemy multiplies
        for (Map.Entry<Snake, Map<Point, Integer>> enemy : enemyDistances.entrySet()) {
            Integer enemyMoves = enemy.getValue().get(point);
            if (enemyMoves != null && moves - enemyMoves > 0) {
                double multiplier = 0.85;
                if (board.isAt(point, Elements.FURY_PILL)) {
                    multiplier = 0.1;
                }
                reward = reward * Math.pow(multiplier, (moves - enemyMoves) / multiplier(board, enemy.getValue(), moves));
            }
        }

        State newState = State.builder()
            .point(point)
            .reward(state.getReward() + reward)
            .length(length)
            .moves(moves)
            .furry(furry)
            .visited(new LinkedHashSet<>())
            .build();
        newState.getVisited().addAll(state.getVisited());
        newState.getVisited().add(point);
        newState.updateRewardPerMove();
        return newState;
    }

    private double multiplier(Board board, Map<Point, Integer> matrix, int moves) {
        long possibilites = matrix.entrySet()
            .stream()
            .filter(distance -> distance.getValue() <= moves)
            .filter(distance -> !board.isAt(distance.getKey(), Elements.STONE))
            .count();
        return 1 - (1d / (double) possibilites) * 0.95;

    }

    private State buildInitialState(Board board) {
        return State.builder()
            .point(board.getMe())
            .moves(0)
            .furry(board.getMyFurry())
            .length(board.getMySnakeLength())
            .visited(new LinkedHashSet<>())
            .build();
    }

    @Data
    @Builder
    @RequiredArgsConstructor
    @AllArgsConstructor
    public static class State {
        final int length;
        final int furry;
        final int moves;
        Point point;
        double reward;
        double rewardPerMove;
        Set<Point> visited = new LinkedHashSet<>();

        public void updateRewardPerMove() {
            this.rewardPerMove = moves > 0 ? reward / moves : 0;
        }

        public int getBucket() {
            return (int) Math.ceil(moves / 10d) * 10;
        }
    }

}
