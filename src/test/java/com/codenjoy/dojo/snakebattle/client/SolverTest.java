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


import lombok.extern.log4j.Log4j2;
import org.junit.Test;

import java.util.List;

import static com.codenjoy.dojo.snakebattle.client.TestUtil.readBoardsFromFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Log4j2
public class SolverTest {

    private YourSolver solver = new YourSolver();

    @Test
    public void testLoop() {
        List<Board> boards = readBoardsFromFile("snake-loops-in-corner.txt");
        assertEquals(20, boards.size());
        boards.forEach(solver.getBoards()::addFirst);
        assertTrue(solver.isMySnakeUseless());
    }

    @Test
    public void testSolver_CannotMove() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");
        assertEquals("LEFT", solver.get(boards.get(30)));
    }

    @Test
    public void testSolver_ActIfEnemyOnTheTail() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");
        assertEquals("DOWN,ACT", solver.get(boards.get(58)));
    }

    @Test
    public void shallAttackWithFurry() {
        List<Board> boards = readBoardsFromFile("snake-collisions.txt");
        solver.furry = 2;
        assertEquals("UP", solver.get(boards.get(10)));
    }

    @Test
    public void shallNotAttackWithFurry() {
        List<Board> boards = readBoardsFromFile("snake-collisions.txt");
        solver.furry = 2;
        assertEquals("LEFT", solver.get(boards.get(11)));
    }

    @Test
    public void shallAttackWithFurryBecauseLonger() {
        List<Board> boards = readBoardsFromFile("snake-collisions.txt");
        solver.furry = 2;
        assertEquals("UP", solver.get(boards.get(12)));
    }

    @Test
    public void shallAttackBecauseLonger() {
        List<Board> boards = readBoardsFromFile("snake-collisions.txt");
        solver.furry = 2;
        assertEquals("RIGHT", solver.get(boards.get(6)));
    }

    @Test
    public void shallEatAppleBecauseEnemyOnTheTail() {
        List<Board> boards = readBoardsFromFile("snake-collisions.txt");
        assertEquals("DOWN", solver.get(boards.get(13)));
    }

    @Test
    public void shallMakeTrap0() {
        List<Board> boards = readBoardsFromFile("snake-traps.txt");
        assertEquals("DOWN", solver.get(boards.get(0)));
    }

    @Test
    public void shallMakeTrap1() {
        List<Board> boards = readBoardsFromFile("snake-traps.txt");
        assertEquals("UP", solver.get(boards.get(1)));
    }

    @Test
    public void shallNotMakeTrap_Dangerous() {
        List<Board> boards = readBoardsFromFile("snake-traps.txt");
        assertEquals("LEFT", solver.get(boards.get(2)));
    }

    @Test
    public void shallNotGetInTrap() {
        List<Board> boards = readBoardsFromFile("snake-collisions.txt");
        assertEquals("DOWN", solver.get(boards.get(7)));
    }

    @Test
    public void shallCutOwnTail_InAdvance() {
        List<Board> boards = readBoardsFromFile("snake-collisions.txt");
        assertEquals("RIGHT", solver.get(boards.get(8)));
    }

    @Test
    public void shallCutOwnTail_InTime() {
        List<Board> boards = readBoardsFromFile("snake-collisions.txt");
        assertEquals("RIGHT", solver.get(boards.get(9)));
    }

    @Test
    public void shallNotCutSnakeBecauseWall() {
        List<Board> boards = readBoardsFromFile("snake-traps.txt");
        assertEquals("LEFT", solver.get(boards.get(3)));
    }

    @Test
    public void shallNotCutSnakeBecauseMoreProminentMoves() {
        List<Board> boards = readBoardsFromFile("snake-traps.txt");
        assertEquals("RIGHT", solver.get(boards.get(4)));
    }

    @Test
    public void testMove59() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");
        assertEquals("DOWN", solver.get(boards.get(59)));
    }

    @Test
    public void testMove61() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");
        assertEquals("DOWN", solver.get(boards.get(61)));
    }

    @Test
    public void testMove62() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");
        assertEquals("DOWN", solver.get(boards.get(62)));
    }

    @Test
    public void testMove63() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");
        assertEquals("UP", solver.get(boards.get(63)));
    }

    @Test
    public void testMove64() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");
        assertEquals("RIGHT", solver.get(boards.get(64)));
    }

    @Test
    public void testMove51_ShallAttackSnakeOnTop() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");
        solver.furry = 9;
        assertEquals("RIGHT,ACT", solver.get(boards.get(51)));
    }


    @Test
    public void testMove_ShallNotEatFurry() {
        List<Board> boards = readBoardsFromFile("snake-collisions.txt");
        solver.furry = 0;
        assertEquals("UP", solver.get(boards.get(14)));
    }

    @Test
    public void testMove22() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");
        assertEquals("DOWN", solver.get(boards.get(22)));
    }

    @Test
    public void testMove_ShallSuicideNoMoves() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");
        assertEquals("ACT(0)", solver.get(boards.get(65)));
    }

    @Test
    public void testMove_ShallSuicideKusKusFurry() {
        List<Board> boards = readBoardsFromFile("snake-collisions.txt");
        assertEquals("ACT(0)", solver.get(boards.get(15)));
    }

    @Test
    public void testMove_ShallSuicideKusKusLonger() {
        List<Board> boards = readBoardsFromFile("snake-collisions.txt");
        assertEquals("ACT(0)", solver.get(boards.get(16)));
    }

    @Test
    public void testMove_ShallNotSuicideShorter() {
        List<Board> boards = readBoardsFromFile("snake-collisions.txt");
        assertEquals("UP", solver.get(boards.get(17)));
    }

}
