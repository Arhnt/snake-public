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

import com.codenjoy.dojo.client.Solver;
import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.snakebattle.model.Elements;
import com.codenjoy.dojo.snakebattle.strategy.distant.BoardUtils;
import com.codenjoy.dojo.snakebattle.strategy.distant.DistantStrategy;
import com.codenjoy.dojo.snakebattle.strategy.turn.DeepTurnBasedStrategy;
import com.google.common.collect.Sets;
import javafx.util.Pair;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.codenjoy.dojo.snakebattle.model.Elements.*;
import static com.codenjoy.dojo.snakebattle.strategy.distant.BoardUtils.distance;

@Log4j2
@Getter
public class YourSolver implements Solver<Board> {

    int furry = 0;
    private long lastServerResponse = System.currentTimeMillis();
    private boolean shouldReset = false;
    private int sleepingTurns = 0;
    private Direction lastAction;
    private Deque<Board> boards = new ArrayDeque<>();
    private DeepTurnBasedStrategy localStrategy = new DeepTurnBasedStrategy();
    private DistantStrategy distantStrategy = new DistantStrategy();
    private ExecutorService es = Executors.newFixedThreadPool(2);

    @Override
    @SneakyThrows
    public String get(Board board) {
        long start = System.currentTimeMillis();
        log.info(board);
        log.info("Starting business logic");

        lastServerResponse = start;
        board.setMySnakeBody(null);
        board.setMySnakeLength(null);
        board.setEnemies(BoardUtils.findEnemies(board));

        BoardUtils.deadEnds.forEach(point -> board.set(point, Elements.WALL));

        if (board.isGameOver()) return "";

        if (allSleeping(board)) {
            return Direction.RIGHT.toString();
        }

        // if others are moving but you're not - try new room
        if (isMySnakeSleepingWhileRestHaveFun(board)) {
            log.info("Sleeping, should I change room?");
            shouldReset = true;
        }

        if (isMySnakeUseless()) {
            log.info("Looping, should I change room?");
            shouldReset = true;
        }

        furry = furry - 1;
        furry = furry < 0 ? 0 : furry;
        if (boards.size() > 0 && boards.getFirst().isAt(board.getMe(), FURY_PILL)) {
            furry = furry + 9;
        }
        board.setMyFurry(furry);
        log.info("My furry {}", board.getMyFurry());

        boards.addFirst((Board) new Board().forString(board.boardAsString()));
        while (boards.size() > 31) {
            boards.removeLast();
        }

        Direction action = canAttack(board);
        boolean eatineApple = false;
        if (action == null) {
            log.info("Start local strategy");
            Map<Direction, Double> actions = localStrategy.nextAction(board);
            if (actions == null || actions.isEmpty()) {
                return "ACT(0)";
            }
            double topReward = Collections.max(actions.values());
            Set<Direction> strategicDirections = new HashSet<>();

            if (action == null) {
                log.info("Start aggressive strategy");
                if (furry > 0) {
                    Direction attackAction = canAttackWithFurry(board, furry);
                    if (attackAction != null && actions.get(attackAction) >= 0) {
                        action = attackAction;
                    } else {
                        log.info("Cannot attack, reward is negative {}", actions.get(attackAction));
                    }
                }
            }

            if (action == null) {
                log.info("Start suicide strategy");
                if (shouldSuicide(board)) {
                    log.info("Suicide");
                    return "ACT(0)";
                }
            }

            if (action == null) {
                log.info("Start cutting strategy");
                strategicDirections.addAll(canMakeCutForEnemy(board));
                log.info("Cut directions: {}", strategicDirections);
                action = strategicDirections.stream()
                    .filter(actions::containsKey)
                    .filter(d -> actions.get(d) >= 0)
                    .max(Comparator.comparingDouble(actions::get))
                    .orElse(null);
                if (action != null) {
                    if (Math.abs(actions.get(action) - topReward) > 0.5) {
                        log.info("Cannot apply cut, there is more prominent move");
                        action = null;
                    }
                }
                if (action != null) {
                    log.info("Cut direction is {}", action);
                }
            }
//
//            if (action == null) {
//                log.info("Start defending strategy");
//                Point myHead = board.getMe();
//                List<Snake> snakes = board.getEnemies()
//                    .stream()
//                    .filter(snake -> distance(snake.getHead(), myHead) <= 4)
//                    .filter(snake -> isSnakeDanger(snake, board))
//                    .collect(Collectors.toList());
//                if (!snakes.isEmpty()) {
//                    log.info("{} danger snakes.", snakes.size());
//                    Set<Direction> dangerDirections = new HashSet<>();
//                    for (Snake snake : snakes) {
//                        if (snake.getHead().getX() > myHead.getX()) {
//                            dangerDirections.add(Direction.RIGHT);
//                        } else if (snake.getHead().getX() < myHead.getX()) {
//                            dangerDirections.add(Direction.LEFT);
//                        }
//                        if (snake.getHead().getY() > myHead.getY()) {
//                            dangerDirections.add(Direction.UP);
//                        } else if (snake.getHead().getY() < myHead.getY()) {
//                            dangerDirections.add(Direction.DOWN);
//                        }
//                    }
//                    log.info("Danger directions are: {}", dangerDirections);
//                    action = dangerDirections.stream()
//                        .map(Direction::inverted)
//                        .filter(d -> actions.get(d) >= 0)
//                        .max(Comparator.comparingInt(d -> freeCells(board, d, myHead)))
//                        .orElse(null);
//                }
//            }

            if (action == null) {
                log.info("Start gathering strategy");
                strategicDirections.addAll(distantStrategy.nextActions(board));

                action = actions
                    .keySet()
                    .stream()
                    .filter(strategicDirections::contains)
                    .filter(d -> Math.abs(actions.get(d) - topReward) <= 0.2)
                    .max(Comparator.comparingDouble(actions::get))
                    .orElse(null);

                if (action == null) {
                    action = actions
                        .keySet()
                        .stream()
                        .max(Comparator.comparingDouble(actions::get))
                        .orElse(null);
                }

                // Go by stairs
                if (action == lastAction) {
                    // other options
                    Set<Direction> possibleActions = Sets.newHashSet(action.clockwise(), action.clockwise().inverted());
                    possibleActions.retainAll(strategicDirections);
                    possibleActions.retainAll(actions.keySet());
                    for (Direction possibleAction : possibleActions) {
                        if (Math.abs(actions.get(possibleAction) - actions.get(action)) <= 0.0001) {
                            log.info("Original action {} similar to new action {}, switching", action, possibleAction);
                            action = possibleAction;
                            break;
                        }
                    }
                    // if other options has same reward and matched by distant strategy - use them
                }

                Direction appleDirection = canEatAppleIfEnemyOnTail(board);
                if (appleDirection != null && actions.get(appleDirection) > 0) {
                    action = appleDirection;
                    eatineApple = true;
                    log.info("Eating apple to trap enemy");
                }
            }

        }

        String act = "";
        if ((board.getMyFurry() > 1 || board.isAt(action, FURY_PILL)) && (board.getMySnakeLength() <= 6)) {
            act = ",ACT";
        }
        if ("".equals(act) && isEnemyOnTheTail(board)) {
            act = ",ACT";
        }
        lastAction = action;
        log.info("Time taken: {}", (System.currentTimeMillis() - start));
        log.info("Action: {}", action);
        return action.toString() + (eatineApple ? "" : act);
    }

    private int freeCells(Board board, Direction d, Point head) {
        Point point = head;
        int distance = 0;
        while (!board.isBarrierAt(point.getX(), point.getY())) {
            distance++;
            point = d.change(point);
        }
        log.debug("To {} cells {}", d, distance);
        return distance;
    }

    private boolean isSnakeDanger(Snake snake, Board board) {
        if (snake.getFurry() <= 0 && board.getMyFurry() > 0) {
            return false;
        }
        if (snake.getFurry() > 0 && board.getMyFurry() <= 0) {
            return true;
        }
        return snake.getLength() >= board.getMySnakeLength() + 2;
    }

    private boolean shouldSuicide(Board board) {
        Point myHead = board.getMe();
        boolean furryAround = Direction.onlyDirections().stream().map(d -> d.change(myHead)).anyMatch(point -> board.isAt(point, ENEMY_HEAD_EVIL));
        if (board.getMyFurry() <= 0 && furryAround) {
            return true;
        } else if (board.getMyFurry() > 0 && furryAround) {
            return Direction
                .onlyDirections()
                .stream()
                .map(d -> d.change(myHead))
                .filter(point -> board.isAt(point, ENEMY_HEAD_EVIL))
                .map(point -> board.getEnemies().stream().filter(snake -> point.equals(snake.getHead())).findFirst().orElse(null))
                .filter(Objects::nonNull)
                .anyMatch(snake -> snake.getLength() >= board.getMySnakeLength() + 2);
        }
        return Direction
            .onlyDirections()
            .stream()
            .map(d -> d.change(myHead))
            .filter(point -> board.isAt(point, Board.ENEMY_HEAD))
            .map(point -> board.getEnemies().stream().filter(snake -> point.equals(snake.getHead())).findFirst().orElse(null))
            .filter(Objects::nonNull)
            .anyMatch(snake -> snake.getLength() >= board.getMySnakeLength() + 2);
    }

    private Direction canAttackWithFurry(Board board, int furry) {
        Map<Direction, Integer> candidates = new HashMap<>();
        for (Snake snake : board.getEnemies()) {
            if (snake.getFurry() > 0) {
                continue;
            }
            for (int i = 0; i < snake.getLength(); i++) {
                boolean canReachWithFurry = distance(board.getMe(), snake.getBody().get(i)) <= furry;
                boolean willStayThere = i + distance(board.getMe(), snake.getBody().get(i)) <= snake.getLength();
                if (canReachWithFurry && willStayThere) {
                    Pair<Integer, Set<Direction>> moves = BoardUtils.findMovesFromToAndStartDirection(board, board.getMe(), snake.getBody().get(i));
                    if (moves.getKey() <= furry && i + moves.getKey() <= snake.getLength()) {
                        moves.getValue().forEach(d -> {
                            int reward = snake.getLength() - moves.getKey();
                            candidates.putIfAbsent(d, reward);
                            if (candidates.get(d) < reward) {
                                candidates.put(d, reward);
                            }
                        });
                    }
                }
            }
        }
        if (!candidates.isEmpty()) {
            log.info("Furry attack results: {}", candidates);
        }
        return candidates.entrySet()
            .stream()
            .max(Comparator.comparingInt(Map.Entry::getValue))
            .map(Map.Entry::getKey)
            .orElse(null);
    }

    private Set<Direction> canMakeCutForEnemy(Board board) {
        Point myHead = board.getMySnakeBody().get(0);
        Point candidate = board.getEnemies()
            .stream()
            .filter(snake -> snake.getFurry() <= 0)
            .filter(snake -> distance(snake.getHead(), myHead) <= 4)
            // snake must be shifted by 1 pixel
            .filter(snake -> snake.getHead().getX() != myHead.getX())
            .filter(snake -> snake.getHead().getY() != myHead.getY())
            .filter(snake -> Math.abs(snake.getHead().getY() - myHead.getY()) == 1 || Math.abs(snake.getHead().getX() - myHead.getX()) == 1)
            .findFirst()
            .map(Snake::getHead)
            .orElse(null);

        if (candidate == null) {
            return Collections.emptySet();
        }

        Set<Direction> cutDirections = new HashSet<>();
        if (candidate.getX() == myHead.getX() - 1) {
            cutDirections.add(Direction.LEFT);
        } else if (candidate.getX() == myHead.getX() + 1) {
            cutDirections.add(Direction.RIGHT);
        }
        if (candidate.getY() == myHead.getY() - 1) {
            cutDirections.add(Direction.DOWN);
        } else if (candidate.getY() == myHead.getY() + 1) {
            cutDirections.add(Direction.UP);
        }

        return cutDirections.stream().filter(board::canMove).collect(Collectors.toSet());
    }

    private Direction canEatAppleIfEnemyOnTail(Board board) {
        if (isEnemyOnTheTail(board)) {
            for (Direction direction : Direction.onlyDirections()) {
                if (board.isAt(direction, APPLE)) {
                    return direction;
                }
            }
        }
        return null;
    }

    private Direction canAttack(Board board) {
        if (board.getMyFurry() > 0) {
            // if there is some one with furry who is shorter than I
            for (Direction direction : Direction.onlyDirections()) {
                Point point = direction.change(board.getMe());
                boolean enemyAtPoint = board.isAt(point, ENEMY_HEAD_EVIL);
                if (enemyAtPoint) {
                    Optional<Snake> enemy = board.getEnemies().stream().filter(snake -> snake.getBody().contains(point)).findAny();
                    if (enemy.isPresent() && enemy.get().getLength() <= board.getMySnakeLength() - 2) {
                        log.info("Attacking furry enemy who is shorter");
                        return direction;
                    }
                }
            }

            for (Direction direction : Direction.onlyDirections()) {
                Point point = direction.change(board.getMe());
                boolean enemyAtPoint = board.isAt(point, ENEMY_HEAD_DOWN, ENEMY_HEAD_LEFT, ENEMY_HEAD_RIGHT, ENEMY_HEAD_UP, ENEMY_BODY_HORIZONTAL, ENEMY_BODY_VERTICAL, ENEMY_BODY_LEFT_DOWN, ENEMY_BODY_LEFT_UP, ENEMY_BODY_RIGHT_DOWN, ENEMY_BODY_RIGHT_UP);
                if (enemyAtPoint) {
                    Optional<Snake> enemy = board.getEnemies().stream().filter(snake -> snake.getBody().contains(point)).findAny();
                    if (enemy.isPresent() && enemy.get().getFurry() <= 0) {
                        log.info("Attacking plain enemy with furry");
                        return direction;
                    }
                }
            }
        }

        for (Direction direction : Direction.onlyDirections()) {
            Point point = direction.change(board.getMe());
            boolean enemyAtPoint = board.isAt(point, ENEMY_HEAD_DOWN, ENEMY_HEAD_LEFT, ENEMY_HEAD_RIGHT, ENEMY_HEAD_UP);
            if (enemyAtPoint) {
                Optional<Snake> enemy = board.getEnemies().stream().filter(snake -> snake.getBody().contains(point)).findAny();
                if (enemy.isPresent() && enemy.get().getLength() + 2 <= board.getMySnakeLength()) {
                    log.info("Attacking shorter enemy");
                    return direction;
                }
            }
        }

        return null;
    }

    private boolean isEnemyOnTheTail(Board board) {
        Point tail = board.getMySnakeBody().get(board.getMySnakeBody().size() - 1);
        return Direction.onlyDirections()
            .stream()
            .map(direction -> direction.change(tail))
            .anyMatch(p -> board.isAt(p, Board.ENEMY_HEAD));
    }

    private boolean allSleeping(Board board) {
        if (board.isMySnakeSleeping() && !board.areOtherSnakesNotSleeping()) {
            return true;
        }
        return false;
    }

    public boolean isMySnakeUseless() {
        if (boards.size() < 8) {
            return false;
        }
        int useLessMoves = 0;
        Iterator<Board> iterator = boards.iterator();
        Point lastPoint = null;
        List<Point> history = new ArrayList<>(8);
        while (iterator.hasNext()) {
            Board b = iterator.next();

            if (lastPoint != null) {
                if (b.isAt(lastPoint, NONE, TAIL_END_DOWN, TAIL_END_LEFT, TAIL_END_UP, TAIL_END_RIGHT)) {
                    useLessMoves++;
                } else {
                    return false;
                }
            }

            lastPoint = b.getMe();
            history.add(lastPoint);

            if (isLooping(history)) {
                return true;
            }
        }

        return useLessMoves >= 30;
    }

    private boolean isMySnakeSleepingWhileRestHaveFun(Board board) {
        if (board.isMySnakeSleeping() && board.areOtherSnakesNotSleeping()) {
            if (sleepingTurns > 1) {
                return true;
            }
            sleepingTurns++;
        }
        return false;
    }

    public boolean isLooping(List<Point> history) {
        return (history.size() >= 8)
            && history.get(0).equals(history.get(4))
            && history.get(1).equals(history.get(5))
            && history.get(2).equals(history.get(6))
            && history.get(3).equals(history.get(7));
    }

}
