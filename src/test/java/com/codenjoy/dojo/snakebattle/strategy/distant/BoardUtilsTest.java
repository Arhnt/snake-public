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
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.codenjoy.dojo.snakebattle.client.TestUtil.readBoardsFromFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class BoardUtilsTest {

    @Test
    public void testCalcDistanceMatrix() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");

        Map<Point, Integer> matrix0 = BoardUtils.calcDistanceMatrix(boards.get(0));
        assertEquals(8, matrix0.size());

        Map<Point, Integer> matrix1 = BoardUtils.calcDistanceMatrix(boards.get(1));
        assertEquals(25, matrix1.size());
    }

    @Test
    public void testFindMovesFromTo_Boards1() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");
        Set<Direction> directions0 = BoardUtils.findMovesFromTo(boards.get(1), boards.get(1).getMe(), new PointImpl(21, 13));
        assertEquals(Sets.newHashSet(Direction.RIGHT, Direction.UP), directions0);

        Set<Direction> directions1 = BoardUtils.findMovesFromTo(boards.get(1), boards.get(1).getMe(), new PointImpl(24, 13));
        assertEquals(Sets.newHashSet(Direction.RIGHT, Direction.UP), directions1);

        Set<Direction> directions2 = BoardUtils.findMovesFromTo(boards.get(1), boards.get(1).getMe(), new PointImpl(22, 12));
        assertEquals(Sets.newHashSet(Direction.RIGHT), directions2);

        Set<Direction> directions3 = BoardUtils.findMovesFromTo(boards.get(1), boards.get(1).getMe(), new PointImpl(17, 15));
        assertEquals(Sets.newHashSet(Direction.UP), directions3);
    }

    @Test
    public void testFindMovesFromTo_Long() {
        List<Board> boards = readBoardsFromFile("distant-strategy-moves.txt");
        Set<Direction> directions0 = BoardUtils.findMovesFromTo(boards.get(0), boards.get(0).getMe(), new PointImpl(2, 17));
        assertEquals(Sets.newHashSet(Direction.DOWN), directions0);

        Set<Direction> directions1 = BoardUtils.findMovesFromTo(boards.get(1), boards.get(1).getMe(), new PointImpl(3, 24));
        assertEquals(Sets.newHashSet(Direction.LEFT, Direction.UP), directions1);
    }

    @Test
    public void testCountMoves() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");
        int moves0 = BoardUtils.countMoves(boards.get(1), boards.get(1).getMe(), new PointImpl(21, 13));
        assertEquals(2, moves0);

        boards = readBoardsFromFile("distant-strategy-moves.txt");
        int moves1 = BoardUtils.countMoves(boards.get(1), boards.get(1).getMe(), new PointImpl(3, 24));
        assertEquals(BoardUtils.distance(boards.get(1).getMe(), new PointImpl(3, 24)), moves1);
    }
    @Test
    public void testFindMovesFromTo_Loop() {
        List<Board> boards = readBoardsFromFile("snake-loops-in-center.txt");
        Set<Direction> directions0 = BoardUtils.findMovesFromTo(boards.get(0), boards.get(0).getMe(), new PointImpl(3, 15));
        assertEquals(Sets.newHashSet(Direction.LEFT), directions0);
    }

    @Test
    public void testFindMovesFromTo_Stone() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");
        Set<Direction> directions0 = BoardUtils.findMovesFromTo(boards.get(23), boards.get(23).getMe(), new PointImpl(25, 20));
        assertEquals(Sets.newHashSet(Direction.RIGHT), directions0);

        Set<Direction> directions1 = BoardUtils.findMovesFromTo(boards.get(24), boards.get(24).getMe(), new PointImpl(15, 4));
        assertEquals(Sets.newHashSet(Direction.LEFT, Direction.DOWN), directions1);
    }


    @Test
    public void testCalcDistanceMatrix_IgnoreDeadEnds() {
        List<Board> boards = readBoardsFromFile("dead-ends.txt");

        Map<Point, Integer> matrix = BoardUtils.calcDistanceMatrix(boards.get(2));
        assertNull(matrix.get(new PointImpl(23, 10)));
        assertNull(matrix.get(new PointImpl(19, 10)));
        assertNull(matrix.get(new PointImpl(21, 10)));
        assertNull(matrix.get(new PointImpl(8, 20)));
        assertNull(matrix.get(new PointImpl(9, 20)));
        assertNull(matrix.get(new PointImpl(8, 22)));
        assertNull(matrix.get(new PointImpl(9, 22)));
    }

    @Test
    public void testFindEnemySnake() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");

        Snake snake1 = BoardUtils.findEnemySnake(boards.get(0), new PointImpl(22, 24));
        assertEquals(5, snake1.getBody().size());
        assertEquals(new PointImpl(22, 24), snake1.getBody().get(0));
        assertEquals(new PointImpl(18, 24), snake1.getBody().get(4));

        assertEquals(5, BoardUtils.findEnemySnake(boards.get(2), new PointImpl(16, 10)).getBody().size());
        assertEquals(10, BoardUtils.findEnemySnake(boards.get(6), new PointImpl(5, 13)).getBody().size());
        assertEquals(13, BoardUtils.findEnemySnake(boards.get(7), new PointImpl(7, 13)).getBody().size());

        assertEquals(5, BoardUtils.findEnemySnake(boards.get(8), new PointImpl(12, 24)).getBody().size());
        assertEquals(16, BoardUtils.findEnemySnake(boards.get(8), new PointImpl(25, 7)).getBody().size());
    }

    @Test
    public void findEnemySnakeFurry() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");
        Snake snake = BoardUtils.findEnemySnake(boards.get(3), new PointImpl(14, 23));
        assertEquals(8, snake.getBody().size());
        assertEquals( new PointImpl(14, 23), snake.getBody().get(0));
        assertEquals(boards.get(3).get(Elements.ENEMY_TAIL_END_DOWN).get(0), snake.getBody().get(7));
    }

    @Test
    public void testFindEnemies() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");
        List<Snake> enemies = BoardUtils.findEnemies(boards.get(22));
        assertEquals(3, enemies.size());
    }

    @Test
    public void findEnemySnakeFurry_Tail() {
        List<Board> boards = readBoardsFromFile("snake-collisions.txt");
        Snake snake = BoardUtils.findEnemySnake(boards.get(11), new PointImpl(24, 27));
        assertEquals(2, snake.getBody().size());
        assertEquals(new PointImpl(24, 27), snake.getBody().get(0));
        assertEquals(new PointImpl(23, 27), snake.getBody().get(1));
    }
}
