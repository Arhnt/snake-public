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
import com.codenjoy.dojo.snakebattle.client.Board;
import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static com.codenjoy.dojo.services.Direction.LEFT;
import static com.codenjoy.dojo.services.Direction.RIGHT;
import static com.codenjoy.dojo.services.Direction.UP;
import static com.codenjoy.dojo.snakebattle.client.TestUtil.readBoardsFromFile;
import static org.junit.Assert.assertEquals;

public class TurnBasedStrategyTest {

    @Test
    public void test() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");
        Board board = boards.get(0);

        DeepTurnBasedStrategy strategy = new DeepTurnBasedStrategy();
//        assertEquals(Collections.singletonList(Direction.RIGHT), strategy.nextAction(board));
    }

    @Test
    public void testShouldNotDie() {
        List<Board> boards = readBoardsFromFile("eat-stone-and-die.txt");
        Board board = boards.get(0);

        board.getMe();
        DeepTurnBasedStrategy strategy = new DeepTurnBasedStrategy();
//        assertEquals(Collections.singletonList(Direction.LEFT), strategy.nextAction(board));
    }

    @Test
    public void testMove_Loop() {
        List<Board> boards = readBoardsFromFile("snake-loops-in-center.txt");
        Board board = boards.get(0);

        DeepTurnBasedStrategy strategy = new DeepTurnBasedStrategy();
        assertEquals(Collections.singletonList(Direction.RIGHT), strategy.nextAction(board));
    }

    @Test
    public void testMove_ShallGoToGoldViaApples() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");
        Board board = boards.get(18);

        DeepTurnBasedStrategy strategy = new DeepTurnBasedStrategy();
        assertEquals(LEFT, strategy.nextAction(board));
    }

    @Test
    public void testMove_PointInTheMiddle() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");
        Board board = boards.get(19);

        DeepTurnBasedStrategy strategy = new DeepTurnBasedStrategy();
        assertEquals(RIGHT, strategy.nextAction(board));
    }

    @Test
    public void testMove_CannotFindException() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");

        DeepTurnBasedStrategy strategy = new DeepTurnBasedStrategy();
        assertEquals(LEFT, strategy.nextAction(boards.get(28)));
        assertEquals(LEFT, strategy.nextAction(boards.get(29)));
    }


    @Test
    public void testMove_Collisions_ShallAttackWithFurry() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");
        DeepTurnBasedStrategy strategy = new DeepTurnBasedStrategy();

        boards.get(9).setMyFurry(1);
        assertEquals(RIGHT, strategy.nextAction(boards.get(9)));

        boards.get(10).setMyFurry(1);
        assertEquals(RIGHT, strategy.nextAction(boards.get(10)));
    }

    @Test
    public void testMove_Collisions_ShallMoveToAttack() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");
        DeepTurnBasedStrategy strategy = new DeepTurnBasedStrategy();

        boards.get(34).setMyFurry(2);
        assertEquals(LEFT, strategy.nextAction(boards.get(34)));
        boards.get(35).setMyFurry(2);
        assertEquals(LEFT, strategy.nextAction(boards.get(35)));
        boards.get(36).setMyFurry(3);
        assertEquals(LEFT, strategy.nextAction(boards.get(36)));
    }

    @Test
    public void testMove_Collisions_ShallNotAttack_NotEnoughtFurry() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");
        DeepTurnBasedStrategy strategy = new DeepTurnBasedStrategy();

        boards.get(34).setMyFurry(1);
        assertEquals(RIGHT, strategy.nextAction(boards.get(34)));
    }

    @Test
    public void testMove_Collisions_ShouldAttackShorterSnake() {
        List<Board> boards = readBoardsFromFile("snake-collisions.txt");
        DeepTurnBasedStrategy strategy = new DeepTurnBasedStrategy();
        assertEquals(UP, strategy.nextAction(boards.get(2)));
    }

    @Test
    public void testMove_Collisions_ShouldAttackShorterSnake6() {
        List<Board> boards = readBoardsFromFile("snake-collisions.txt");
        DeepTurnBasedStrategy strategy = new DeepTurnBasedStrategy();
        assertEquals(UP, strategy.nextAction(boards.get(6)));
    }

    @Test
    public void testMove_Collisions_ShouldAvoidLongerSnake() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");
        DeepTurnBasedStrategy strategy = new DeepTurnBasedStrategy();
        assertEquals(UP, strategy.nextAction(boards.get(53)));
    }

    @Test
    public void testMove_NPE() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");
        DeepTurnBasedStrategy strategy = new DeepTurnBasedStrategy();
        assertEquals(UP, strategy.nextAction(boards.get(54)));
    }


    @Test
    public void testMove_55() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");
        DeepTurnBasedStrategy strategy = new DeepTurnBasedStrategy();
        boards.get(55).setMyFurry(3);
        assertEquals(LEFT, strategy.nextAction(boards.get(55)));
    }
}
