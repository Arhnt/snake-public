package com.codenjoy.dojo.snakebattle.client;

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
import lombok.Data;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import static com.codenjoy.dojo.snakebattle.model.Elements.*;

@Log4j2
public class TurnBasedStrategy {

    private static final int MAX_MOVES = 7;
    private Direction[] directions = {Direction.UP, Direction.LEFT, Direction.RIGHT, Direction.DOWN};

    public Map<Direction, Double> nextAction(Board board) {
        long start = System.currentTimeMillis();

        // iterating heap
        PriorityQueue<State> heap = new PriorityQueue<>();
        int counter = 0;
        while (System.currentTimeMillis() - start < 500 && !heap.isEmpty()) {
            State state = heap.poll();
            for (Direction direction : directions) {
                if (!state.directioons.isEmpty() && direction == state.directioons.get(state.directioons.size() - 1).inverted()) {
                    // cannot move back
                    continue;
                }
                counter++;
                State newState = calculateReward(state, direction);
                if (newState != null) {
                    heap.add(newState);
                }
            }
        }


//        log.debug("Direction rewards: {}, took {}ms", rewardsByDirection, System.currentTimeMillis() - start);

        return null;
    }

    // calculate max reward for given board
    private State calculateReward(State state, Direction direction) {
//        System.out.println("===================================================");
//        System.out.println("Reward calculation for direction " + direction + ", depth: " + moves);
        Board currentState = (Board) new Board().forString(state.board);
//        System.out.println(currentState);

        // TODO: do not check same cell multiple times, unless fury or flight pill consumed
//        if (currentState.isVisited(direction)) {
//            return reward;
//        }

        if (!currentState.canMove(direction)) {
//            System.out.println("Cannot move");
            return null;
        }

        double reward = 0;
        int trimTail = 1;
        Point currentHead = currentState.getMe();
        Point newHead = direction.change(currentHead);

        if (currentState.isAt(newHead, APPLE)) {
            trimTail = 0;
            reward = 1d;
        } else if (currentState.isAt(newHead, GOLD)) {
            reward = 10d;
        } else if (currentState.isAt(newHead, STONE)) {

            reward = 5d;
        } else if (currentState.isAt(newHead, FLYING_PILL)) {
            reward = 0d;
        } else if (currentState.isAt(newHead, FURY_PILL)) {
            reward = 10d;
        }

        // move snake head to that direction
        Board newState = (Board) new Board().forString(currentState.boardAsString());
        newState.set(newHead, HEAD_UP);

        if (newState.getMySnakeLength() < 2) {
            throw new IllegalStateException("Smth went wrong, snake length < 2");
        } else if (newState.getMySnakeLength() > 2) {
            newState.set(currentHead.getX(), currentHead.getY(), BODY_HORIZONTAL.ch());
        } else {
            newState.set(currentHead.getX(), currentHead.getY(), TAIL_END_DOWN.ch());
        }


        if (currentState.isAt(newHead, APPLE)) {
            // tail unchanged
        } else if (currentState.isAt(newHead, GOLD)) {
//            newState.moveSnake(1);
        } else if (currentState.isAt(newHead, STONE)) {
//            newState.trimTail(4);
        } else if (currentState.isAt(newHead, FLYING_PILL)) {
            // reduce tail
            // set flying head
            newState.set(newHead, HEAD_FLY);
            //           newState.trimTail(1);
        } else if (currentState.isAt(newHead, FURY_PILL)) {
            newState.set(newHead, HEAD_EVIL);
            //           newState.trimTail(1);
        } else {
//            newState.trimTail(1);
        }

//        System.out.println(newState);

//        System.out.println("Reward " + reward);
        return null;
    }

    @Data
    public static class State {
        final int length;
        final int furry;
        Point point;
        private String board;
        private double reward;
        private double rewardPerMove;
        private int moves;
        private List<Direction> directioons;

        public void updateRewardPerMove() {
            this.rewardPerMove = moves > 0 ? reward / moves : 0;
        }
    }

}
