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
import com.codenjoy.dojo.snakebattle.client.Snake;
import com.codenjoy.dojo.snakebattle.strategy.distant.BoardUtils;
import com.google.common.collect.Sets;
import javafx.util.Pair;
import lombok.Builder;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static com.codenjoy.dojo.snakebattle.model.Elements.*;
import static com.codenjoy.dojo.snakebattle.strategy.distant.BoardUtils.distance;
import static java.util.stream.Collectors.toMap;

@Log4j2
public class DeepTurnBasedStrategy {

    public static Comparator<State> STATE_COMPARATOR = Comparator
        .comparing(State::getMoves)
        .thenComparing(Comparator.comparingDouble(State::getRewardPerMove).reversed());

    private Direction[] directions = {Direction.UP, Direction.LEFT, Direction.RIGHT, Direction.DOWN};

    public Map<Direction, Double> nextAction(Board board) {
        long start = System.currentTimeMillis();

        // iterating heap
        PriorityBlockingQueue<State> heap = new PriorityBlockingQueue<>(300, STATE_COMPARATOR);
        State initialState = buildInitialState(board);
        Sets.newHashSet(directions)
            .stream()
            .map(direction -> calculateReward(initialState, direction))
            .filter(Objects::nonNull)
            .peek(State::updateRewardPerMove)
            .forEach(heap::add);

        AtomicInteger counter = new AtomicInteger(0);
        Map<Integer, Map<Direction, State>> winners = new HashMap<>();
        while (System.currentTimeMillis() - start < 300 && !heap.isEmpty()) {
            State state = heap.poll();
            Arrays.stream(directions)
//                .parallel()
                .filter(direction -> state.directions.isEmpty() || direction != state.directions.get(state.directions.size() - 1).inverted())
                .peek(d -> counter.incrementAndGet())
                .map(direction -> calculateReward(state, direction))
                .filter(Objects::nonNull)
                .peek(State::updateRewardPerMove)
                .peek(newState -> suggestWinner(winners, newState))
                .forEach(heap::add);
        }

        Map<Direction, Double> winner = null;
        if (winners.size() == 0) {
            log.warn("Cannot find any route for:\n{}", board.boardAsString());
            return null;
        } else if (winners.size() == 1) {
            winner = winners.values().iterator().next().entrySet().stream().collect(toMap(Map.Entry::getKey, e -> e.getValue().getRewardPerMove()));
        } else {
            Integer maxStep = Collections.max(winners.keySet());
            winner = winners.get(maxStep - 1).entrySet().stream().collect(toMap(Map.Entry::getKey, e -> e.getValue().getRewardPerMove()));
        }

        log.debug("Winner directions {} took {}ms, parsed {} moves, max depth {}", winner, System.currentTimeMillis() - start, counter, Collections.max(winners.keySet()));
        return winner;
    }

    private void suggestWinner(Map<Integer, Map<Direction, State>> winners, State newState) {
        Direction direction = newState.getDirections().get(0);
        winners.putIfAbsent(newState.getMoves(), new HashMap<>());
        winners.get(newState.getMoves()).putIfAbsent(direction, newState);
        if (winners.get(newState.getMoves()).get(direction).getRewardPerMove() < newState.getRewardPerMove()) {
            winners.get(newState.getMoves()).put(direction, newState);
        }
    }

    private State buildInitialState(Board board) {
        return State.builder()
            .board(board.boardAsString())
            .furry(board.getMyFurry())
            .directions(new ArrayList<>())
            .mySnake(board.getMySnakeBody())
            .build();
    }

    private State calculateReward(State state, Direction direction) {
        Board board = (Board) new Board().forString(state.board);
        board.setMySnakeLength(state.mySnake.size());
        board.setMySnakeBody(state.mySnake);
        board.setMyFurry(state.furry);
        board.setEnemies(BoardUtils.findEnemies(board));
        Pair<Double, State> rewardPair = calculateReward(board, direction, state);
        if (rewardPair == null) {
            return null;
        }

        double reward = rewardPair.getKey();
        reward = reward * Math.pow(0.99, state.moves);

        List<Direction> directions = new ArrayList<>(state.directions);
        directions.add(direction);

        State newState = rewardPair.getValue();
        newState.setReward(state.reward + reward);
        newState.setDirections(directions);

        return newState;
    }
// todo ignore snake's behind the body
    private Pair<Double, State> calculateReward(Board board, Direction direction, State state) {
        if (!board.canMove(direction)) {
            return null;
        }
        int furry = state.furry;
        double reward = 0;
        Point newHead = direction.change(board.getMe());
        List<Point> newSnake;
        // if 2 moves to longer snake close to longer snake than -10
        if (board.isAt(newHead, FURY_PILL) && !enemyHeadWithin(board, newHead, state.getMoves())) {
            reward = 10d;
            furry = furry + 9;
            newSnake = board.moveSnake(direction, 1, state.mySnake);
            board.set(newHead, HEAD_EVIL);
        } else if (board.isAt(newHead, Board.ENEMY_BODY) && state.furry > 0) {
            reward = 10d;
            newSnake = board.moveSnake(direction, 1, state.mySnake);
        } else if (board.isAt(newHead, Board.ENEMY_HEAD) && state.furry > 0) {
            reward = 20d;
            newSnake = board.moveSnake(direction, 1, state.mySnake);
        } else if (board.isCloseToLongerSnake(newHead, 2)) {
            reward = -10d;
            newSnake = board.moveSnake(direction, 1, state.mySnake);
        } else if (board.isCloseToShortSnake(newHead, 1) && !furryPillWithin(board, newHead, state.getMoves())) {
            reward = 10d;
            newSnake = board.moveSnake(direction, 1, state.mySnake);
        } else if (board.isAt(newHead, APPLE)) {
            reward = 1d;
            newSnake = board.moveSnake(direction, 0, state.mySnake);
        } else if (board.isAt(newHead, GOLD)) {
            reward = 10d;
            newSnake = board.moveSnake(direction, 1, state.mySnake);
        } else if (board.isAt(newHead, STONE) && board.getMyFurry() > 0) {
            reward = 5d;
            newSnake = board.moveSnake(direction, 1, state.mySnake);
        } else if (board.isAt(newHead, STONE) && (board.getLongestEnemy() + 5 < board.getMySnakeLength())) {
            reward = 5d;
            newSnake = board.moveSnake(direction, 4, state.mySnake);
        } else if (board.isAt(newHead, STONE)) {
            reward = -1d;
            newSnake = board.moveSnake(direction, 4, state.mySnake);
        } else if (board.isAt(newHead, FLYING_PILL)) {
            reward = 0d;
            newSnake = board.moveSnake(direction, 1, state.mySnake);
            board.set(newHead, HEAD_FLY);
        } else if (board.isAt(newHead, BODY_HORIZONTAL, BODY_VERTICAL, BODY_LEFT_DOWN, BODY_LEFT_UP, BODY_RIGHT_DOWN, BODY_RIGHT_UP)) {
            reward = -5d;
            newSnake = board.moveSnake(direction, 1, state.mySnake);
        } else {
            newSnake = board.moveSnake(direction, 1, state.mySnake);
        }

        State newState = State.builder()
            .furry(furry - 1 < 0 ? 0 : furry - 1)
            .board(board.boardAsString())
            .moves(state.moves + 1)
            .mySnake(newSnake)
            .build();

        return new Pair<>(reward, newState);
    }

    private boolean furryPillWithin(Board board, Point newHead, int moves) {
        return board.get(FURY_PILL).stream().anyMatch(point -> distance(point, newHead) <= moves);
    }

    private boolean enemyHeadWithin(Board board, Point newHead, int moves) {
        return board.getEnemies().stream()
            .map(Snake::getHead)
            .anyMatch(point -> distance(point, newHead) <= moves);
    }

    @Data
    @Builder
    public static class State {
        final int furry;
        private String board;
        private double reward;
        private double rewardPerMove;
        private int moves;
        private List<Direction> directions;
        private List<Point> mySnake;

        public void updateRewardPerMove() {
            this.rewardPerMove = moves > 0 ? reward / moves : 0;
        }
    }

}
