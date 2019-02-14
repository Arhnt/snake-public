package com.codenjoy.dojo.snakebattle.client;

/*-
 * #%L
 * Codenjoy - it's a dojo-like platform from developers to developers.
 * %%
 * Copyright (C) 2018 Codenjoy
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


import com.codenjoy.dojo.client.AbstractBoard;
import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.snakebattle.model.Elements;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.util.*;

import static com.codenjoy.dojo.services.Direction.*;
import static com.codenjoy.dojo.snakebattle.model.Elements.*;
import static com.codenjoy.dojo.snakebattle.strategy.distant.BoardUtils.distance;

/**
 * Класс, обрабатывающий строковое представление доски.
 * Содержит ряд унаследованных методов {@see AbstractBoard},
 * но ты можешь добавить сюда любые свои методы на их основе.
 */
@Getter
@Setter
@Log4j2
public class Board extends AbstractBoard<Elements> {

    private static Elements[] mySnakeHead = {
        HEAD_DOWN, HEAD_LEFT, HEAD_RIGHT, HEAD_UP, HEAD_DEAD, HEAD_EVIL, HEAD_FLY, HEAD_SLEEP
    };

    private static Elements[] mySnakeTail = {
        TAIL_END_DOWN, TAIL_END_LEFT, TAIL_END_UP, TAIL_END_RIGHT, TAIL_INACTIVE
    };

    private static Elements[] mySnake = {
        HEAD_DOWN, HEAD_LEFT, HEAD_RIGHT, HEAD_UP, HEAD_DEAD, HEAD_EVIL, HEAD_FLY, HEAD_SLEEP,
        TAIL_END_DOWN, TAIL_END_LEFT, TAIL_END_UP, TAIL_END_RIGHT, TAIL_INACTIVE,
        BODY_HORIZONTAL, BODY_VERTICAL, BODY_LEFT_DOWN, BODY_LEFT_UP, BODY_RIGHT_DOWN, BODY_RIGHT_UP
    };

    private static Elements[] enemySnake = {
        ENEMY_HEAD_DOWN, ENEMY_HEAD_LEFT, ENEMY_HEAD_RIGHT, ENEMY_HEAD_UP, ENEMY_HEAD_EVIL,
        ENEMY_TAIL_END_DOWN, ENEMY_TAIL_END_LEFT, ENEMY_TAIL_END_UP, ENEMY_TAIL_END_RIGHT,
        ENEMY_BODY_HORIZONTAL, ENEMY_BODY_VERTICAL, ENEMY_BODY_LEFT_DOWN, ENEMY_BODY_LEFT_UP, ENEMY_BODY_RIGHT_DOWN, ENEMY_BODY_RIGHT_UP
    };

    public static final Elements[] ENEMY_HEAD = {ENEMY_HEAD_DOWN, ENEMY_HEAD_LEFT, ENEMY_HEAD_RIGHT, ENEMY_HEAD_UP};
    public static final Elements[] ENEMY_TAIL = {ENEMY_TAIL_END_DOWN, ENEMY_TAIL_END_LEFT, ENEMY_TAIL_END_UP, ENEMY_TAIL_END_RIGHT};
    public static final Elements[] ENEMY_BODY = {ENEMY_BODY_HORIZONTAL, ENEMY_BODY_VERTICAL, ENEMY_BODY_LEFT_DOWN, ENEMY_BODY_LEFT_UP, ENEMY_BODY_RIGHT_DOWN, ENEMY_BODY_RIGHT_UP};

    private static Map<Elements, List<Direction>> directionsByBodyPart = new HashMap<>();

    static {
        directionsByBodyPart.put(TAIL_INACTIVE, Collections.singletonList(RIGHT));
        directionsByBodyPart.put(TAIL_END_DOWN, Collections.singletonList(UP));
        directionsByBodyPart.put(TAIL_END_LEFT, Collections.singletonList(RIGHT));
        directionsByBodyPart.put(TAIL_END_RIGHT, Collections.singletonList(LEFT));
        directionsByBodyPart.put(TAIL_END_UP, Collections.singletonList(DOWN));
        directionsByBodyPart.put(BODY_HORIZONTAL, Lists.newArrayList(LEFT, RIGHT));
        directionsByBodyPart.put(BODY_LEFT_DOWN, Lists.newArrayList(LEFT, DOWN));
        directionsByBodyPart.put(BODY_LEFT_UP, Lists.newArrayList(LEFT, UP));
        directionsByBodyPart.put(BODY_RIGHT_DOWN, Lists.newArrayList(RIGHT, DOWN));
        directionsByBodyPart.put(BODY_RIGHT_UP, Lists.newArrayList(RIGHT, UP));
        directionsByBodyPart.put(BODY_VERTICAL, Lists.newArrayList(UP, DOWN));
    }

    private long timestamp = System.currentTimeMillis();

    private int myFurry;
    private int myFlight;
    private Integer mySnakeLength;
    private List<Point> mySnakeBody;
    private List<Snake> enemies;

    @Override
    public Elements valueOf(char ch) {
        return Elements.valueOf(ch);
    }

    public boolean isBarrierAt(int x, int y) {
        return isAt(x, y, WALL, START_FLOOR, ENEMY_HEAD_SLEEP, ENEMY_TAIL_INACTIVE, TAIL_INACTIVE);
    }

    @Override
    protected int inversionY(int y) {
        return size - 1 - y;
    }

    public Point getMe() {
        return getMyHead().get(0);
    }

    public boolean isGameOver() {
        return getMyHead().isEmpty();
    }

    private List<Point> getMyHead() {
        return get(HEAD_DOWN, HEAD_LEFT, HEAD_RIGHT, HEAD_UP, HEAD_SLEEP, HEAD_EVIL, HEAD_FLY);
    }

    public boolean isStoneAt(int x, int y) {
        return isAt(x, y, STONE);
    }

    public void set(Point point, Elements elem) {
        this.set(point.getX(), point.getY(), elem.ch());
    }

    public Direction getDirectionFromTo(Point from, Point to) {
        if (from.getX() == to.getX() && from.getY() == to.getY() - 1) {
            return UP;
        } else if (from.getX() == to.getX() && from.getY() == to.getY() + 1) {
            return DOWN;
        } else if (from.getX() == to.getX() + 1 && from.getY() == to.getY()) {
            return LEFT;
        } else if (from.getX() == to.getX() - 1 && from.getY() == to.getY()) {
            return RIGHT;
        }
        throw new IllegalStateException("Cannot find direction from " + from + " to " + to);
    }

    public boolean isAt(Direction direction, Elements... element) {
        return this.isAt(direction.change(getMe()), element);
    }

    public int getMySnakeLength() {
        if (mySnakeLength == null) {
            mySnakeLength = get(mySnake).size();
        }
        return mySnakeLength;
    }

    public boolean canMove(Direction direction) {
        // can move if None, Apple, Gold
        if (isAt(direction, NONE, APPLE, GOLD, FLYING_PILL, FURY_PILL)) {
            return true;
        }
        // can move to own tail, if it's not last element
        if ((getMySnakeLength() > 2) && isAt(direction, TAIL_END_DOWN, TAIL_END_LEFT, TAIL_END_UP, TAIL_END_RIGHT)) {
            return true;
        }

        // can move to own body, but not to first chunk
        if (isAt(direction, BODY_HORIZONTAL, BODY_VERTICAL, BODY_LEFT_DOWN, BODY_LEFT_UP, BODY_RIGHT_DOWN, BODY_RIGHT_UP) && !direction.change(getMe()).equals(getMySnakeBody().get(1))) {
            return true;
        }

        // can move if Stone && own length >= 5
        if (isAt(direction, STONE) && getMySnakeLength() >= 5) {
            return true;
        }
        // can move if flight && stone || other snake
        if (isAt(direction, STONE) && isAt(mySnakeBody.get(0), HEAD_FLY)) {
            return true;
        }
        if (isAt(getMySnakeBody().get(0), HEAD_FLY) && (isAt(direction, STONE) || isAt(direction, enemySnake))) {
            return true;
        }
        // can move if fury && stone || other snake
        if (getMyFurry() > 0 && (isAt(direction, STONE) || isAt(direction, enemySnake))) {
            return true;
        }

        // TODO: can move to other's tail only if there is no apple around
        if (isAt(direction, ENEMY_TAIL) && thereIsNoAppleForThatSnake(direction.change(getMe()))) {
            return true;
        }

        if (isHeadOfShortSnake(direction.change(getMe()))) {
            return true;
        }

        return false;
    }

    private boolean thereIsNoAppleForThatSnake(Point change) {
        Optional<Snake> snake = getEnemies().stream().filter(s -> change.equals(s.getTail())).findFirst();
        if (snake.isPresent()) {
            return onlyDirections().stream().noneMatch(
                direction -> isAt(direction.change(snake.get().getHead()), APPLE)
            );
        }
        return false;
    }

    public Point getTail() {
        return get(mySnakeTail).get(0);
    }

    public boolean isMySnakeSleeping() {
        return !get(HEAD_SLEEP).isEmpty();
    }

    public boolean areOtherSnakesNotSleeping() {
        return !get(ENEMY_HEAD_DOWN, ENEMY_HEAD_LEFT, ENEMY_HEAD_RIGHT, ENEMY_HEAD_UP, ENEMY_HEAD_EVIL, ENEMY_HEAD_FLY).isEmpty();
    }

    public List<Point> getMySnakeBody() {
        if (mySnakeBody == null) {
            List<Point> snake = new ArrayList<>();
            Point point = getTail();

            boolean isHeadFound = false;
            Direction nextDirection = null;
            while (!isHeadFound) {
                Elements elem = getAt(point);
                if (!isAt(point, mySnake)) {
                    throw new IllegalStateException("Cannot find my snake's body");
                }
                snake.add(point);

                final Direction prevDirection = nextDirection == null ? null : nextDirection.inverted();
                nextDirection = directionsByBodyPart.get(elem)
                    .stream()
                    .filter(d -> prevDirection == null || d != prevDirection)
                    .findFirst()
                    .orElse(null);

                if (nextDirection == null) {
                    throw new IllegalStateException("Cannot find next move for: " + elem);
                }
                point = nextDirection.change(point);
                isHeadFound = isAt(point, mySnakeHead);
            }

            snake.add(point);
            Collections.reverse(snake);
            mySnakeBody = snake;
        }
        return mySnakeBody;
    }

    public List<Point> moveSnake(Direction direction, int trimTail, List<Point> mySnake) {
        Point newHead = direction.change(getMe());

        int cutLength = calcCutLength(newHead, mySnake);
        int newLength = mySnake.size() - trimTail - cutLength;
        int currentLength = 1;
        Direction prev = direction;
        for (int i = 0; i < mySnake.size(); i++) {
            Point point = mySnake.get(i);
            if (currentLength < newLength) {
                Direction next = getDirectionFromTo(point, mySnake.get(i + 1));
                set(point, getBodyForDirection(prev, next));
                prev = next.inverted();
            } else if (currentLength == newLength) {
                set(point, getTailForDirection(prev));
            } else {
                set(point, NONE);
            }
            currentLength++;
        }

        set(newHead, getHeadForDirection(direction));
        List<Point> newSnake = new ArrayList<>();
        newSnake.add(newHead);
        newSnake.addAll(mySnake.subList(0, newLength));
        mySnakeBody = newSnake;
        mySnakeLength = newSnake.size();
        return newSnake;
    }

    private int calcCutLength(Point newHead, List<Point> mySnake) {
        if (isAt(newHead, BODY_HORIZONTAL, BODY_VERTICAL, BODY_LEFT_DOWN, BODY_LEFT_UP, BODY_RIGHT_DOWN, BODY_RIGHT_UP)) {
            for (int i = 0; i < mySnake.size(); i++) {
                if (newHead.equals(mySnake.get(i))) {
                    return mySnake.size() - i - 1;
                }
            }
        }
        return 0;
    }

    private Elements getHeadForDirection(Direction direction) {
        switch (direction) {
            case LEFT:
                return HEAD_LEFT;
            case RIGHT:
                return HEAD_RIGHT;
            case UP:
                return HEAD_UP;
            case DOWN:
                return HEAD_DOWN;
            default:
                throw new IllegalStateException("Cannot find head for " + direction);
        }
    }

    private Elements getTailForDirection(Direction direction) {
        switch (direction) {
            case LEFT:
                return TAIL_END_RIGHT;
            case RIGHT:
                return TAIL_END_LEFT;
            case UP:
                return TAIL_END_DOWN;
            case DOWN:
                return TAIL_END_UP;
            default:
                throw new IllegalStateException("Cannot find head for " + direction);
        }
    }

    private Elements getBodyForDirection(Direction left, Direction right) {
        switch (left) {
            case LEFT:
                switch (right) {
                    case LEFT:
                        return BODY_HORIZONTAL;
                    case RIGHT:
                        return BODY_HORIZONTAL;
                    case UP:
                        return BODY_LEFT_UP;
                    case DOWN:
                        return BODY_LEFT_DOWN;
                    default:
                        throw new IllegalStateException("Cannot find head for " + right);
                }
            case RIGHT:
                switch (right) {
                    case LEFT:
                        return BODY_HORIZONTAL;
                    case RIGHT:
                        return BODY_HORIZONTAL;
                    case UP:
                        return BODY_RIGHT_UP;
                    case DOWN:
                        return BODY_RIGHT_DOWN;
                    default:
                        throw new IllegalStateException("Cannot find head for " + right);
                }
            case UP:
                switch (right) {
                    case LEFT:
                        return BODY_LEFT_UP;
                    case RIGHT:
                        return BODY_RIGHT_UP;
                    case UP:
                        return BODY_VERTICAL;
                    case DOWN:
                        return BODY_VERTICAL;
                    default:
                        throw new IllegalStateException("Cannot find head for " + right);
                }
            case DOWN:
                switch (right) {
                    case LEFT:
                        return BODY_LEFT_DOWN;
                    case RIGHT:
                        return BODY_RIGHT_DOWN;
                    case UP:
                        return BODY_VERTICAL;
                    case DOWN:
                        return BODY_VERTICAL;
                    default:
                        throw new IllegalStateException("Cannot find head for " + right);
                }
            default:
                throw new IllegalStateException("Cannot find head for " + left);
        }
    }


    public int getLongestEnemy() {
        return enemies.stream()
            .map(Snake::getBody)
            .filter(Objects::nonNull)
            .mapToInt(List::size)
            .max()
            .orElse(0);
    }

    public boolean isHeadOfShortSnake(Point newHead) {
        if (isAt(newHead, ENEMY_HEAD)) {
            Snake enemy = getEnemy(newHead);
            if (enemy == null) {
                log.warn("Cannot find enemy at {}", newHead);
                return false;
            }
            return enemy.getBody().size() <= getMySnakeLength() - 2;
        }
        return false;
    }

    private Snake getEnemy(Point newHead) {
        return enemies.stream().filter(snake -> snake.getBody().contains(newHead)).findFirst().orElse(null);
    }

    public boolean isCloseToLongerSnake(Point newHead, int moves) {
        return getEnemies()
            .stream()
            .filter(snake -> snake.getFurry() > 0 || snake.getBody().size() > getMySnakeLength())
            .anyMatch(snake -> distance(newHead, snake.getBody().get(0)) <= moves);
    }

    public boolean isCloseToShortSnake(Point newHead, int moves) {
        return getEnemies()
            .stream()
            .filter(snake -> snake.getBody().size() <= getMySnakeLength() - 2)
            .filter(snake -> distance(newHead, snake.getBody().get(0)) <= moves)
            .filter(snake -> snake.getBody().size() > 1)
            .anyMatch(snake -> distance(newHead, snake.getBody().get(1)) >= distance(newHead, snake.getBody().get(0))); // ensure that tail is not closer than head
    }
}

