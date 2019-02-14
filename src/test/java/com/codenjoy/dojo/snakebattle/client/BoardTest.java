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
import com.codenjoy.dojo.services.PointImpl;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

import static com.codenjoy.dojo.snakebattle.client.TestUtil.readBoardsFromFile;
import static org.junit.Assert.*;

public class BoardTest {

    @Test
    public void testMySnakeBody() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");
        assertEquals(2, boards.get(0).getMySnakeBody().size());
        assertEquals(6, boards.get(1).getMySnakeBody().size());
        assertEquals(4, boards.get(2).getMySnakeBody().size());
        assertEquals(5, boards.get(3).getMySnakeBody().size());
        assertEquals(4, boards.get(4).getMySnakeBody().size());
        assertEquals(4, boards.get(5).getMySnakeBody().size());
    }

    @Test
    public void testGetDirectionFromTo() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");
        Board board = boards.get(0);
        assertEquals(Direction.RIGHT, board.getDirectionFromTo(board.getTail(), board.getMe()));
        assertEquals(Direction.LEFT, board.getDirectionFromTo(board.getMe(), board.getTail()));
    }

    @Test
    public void testMoveSnake() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");
        Board board = boards.get(1);
        board.moveSnake(Direction.UP, 0, board.getMySnakeBody());
        System.out.println(board);
    }

    @Test
    public void testMoveSnake_Loop() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");
        Board board = boards.get(5);
        assertEquals(
            Lists.newArrayList(
                new PointImpl(2, 1),
                new PointImpl(3, 1),
                new PointImpl(3, 2),
                new PointImpl(2, 2)
            ),
            board.moveSnake(Direction.LEFT, 1, board.getMySnakeBody())
        );
        board.moveSnake(Direction.UP, 1, board.getMySnakeBody());
        board.moveSnake(Direction.RIGHT, 1, board.getMySnakeBody());
        board.moveSnake(Direction.DOWN, 1, board.getMySnakeBody());
        System.out.println(board);
    }

    @Test
    public void testCanMove_CanMoveOwnBody() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");
        Board board = boards.get(16);

        assertTrue(board.canMove(Direction.LEFT));
        assertTrue(board.canMove(Direction.UP));
        assertTrue(board.canMove(Direction.RIGHT));
        assertFalse(board.canMove(Direction.DOWN));
    }

    @Test
    public void testMoveSnake_CanMoveOwnBody() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");
        Board board = boards.get(16);
        board.moveSnake(Direction.LEFT, 1, board.getMySnakeBody());
        System.out.println(board);
        assertEquals(4, board.getMySnakeLength());
    }

    @Test
    public void testMoveSnake_MoveDown() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");
        Board board = boards.get(31);
        board.moveSnake(Direction.DOWN, 1, board.getMySnakeBody());
        System.out.println(board);
        assertEquals(4, board.getMySnakeLength());
    }

    @Test
    public void testCanMoveToTailSnake() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");
        Board board = boards.get(56);
        assertTrue(board.canMove(Direction.LEFT));
    }

    @Test
    public void testCannotMoveToTailSnake() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");
        Board board = boards.get(57);
        assertFalse(board.canMove(Direction.LEFT));
    }

    @Test
    public void testActIsEnemyOnTheTail() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");
        Board board = boards.get(57);
        assertFalse(board.canMove(Direction.LEFT));
    }
}
