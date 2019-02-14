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
import com.codenjoy.dojo.services.PointImpl;
import com.codenjoy.dojo.snakebattle.client.Board;
import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.List;
import java.util.PriorityQueue;

import static com.codenjoy.dojo.snakebattle.client.TestUtil.readBoardsFromFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class DistantStrategyTest {
    DistantStrategy strategy = new DistantStrategy();

    @Test
    public void testDistant() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");
        strategy.nextActions(boards.get(1));
    }

    @Test
    public void testDistant_FewPoints() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");
        assertEquals(Sets.newHashSet(Direction.LEFT), strategy.nextActions(boards.get(3)));
    }

    @Test
    public void testDistant_MiddleOfTwoPoints() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");
        assertEquals(Sets.newHashSet(Direction.RIGHT, Direction.UP), strategy.nextActions(boards.get(19)));
    }

    @Test
    public void testDistant_ShouldEatOwnTail() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");
        assertEquals(Sets.newHashSet(Direction.RIGHT), strategy.nextActions(boards.get(20)));
    }

    @Test
    public void testDistant_ShallNotDieInStones() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");
        assertEquals(Sets.newHashSet(Direction.LEFT), strategy.nextActions(boards.get(3)));
    }

    @Test
    public void testComparator() {
        PriorityQueue<DistantStrategy.State> heap = new PriorityQueue<>(DistantStrategy.STATE_COMPARATOR);

        heap.add(DistantStrategy.State.builder().rewardPerMove(1).moves(4).build());
        heap.add(DistantStrategy.State.builder().rewardPerMove(2.5).moves(4).build());
        heap.add(DistantStrategy.State.builder().rewardPerMove(0.3).moves(5).build());
        heap.add(DistantStrategy.State.builder().rewardPerMove(2.5).moves(3).visited(Sets.newHashSet(new PointImpl(1, 2), new PointImpl(2, 3))).build());
        heap.add(DistantStrategy.State.builder().rewardPerMove(0.4).moves(1).build());
        heap.add(DistantStrategy.State.builder().rewardPerMove(2.5).moves(3).visited(Sets.newHashSet(new PointImpl(1, 2))).build());

        assertEquals(6, heap.size());

        assertEquals(2.5, heap.peek().getRewardPerMove(), 0.001);
        assertEquals(3, heap.peek().getMoves());
        assertEquals(1, heap.peek().getVisited().size());
        heap.poll();

        assertEquals(2.5, heap.peek().getRewardPerMove(), 0.001);
        assertEquals(3, heap.peek().getMoves());
        assertEquals(2, heap.peek().getVisited().size());
        heap.poll();

        assertEquals(DistantStrategy.State.builder().rewardPerMove(2.5).moves(4).build(), heap.poll());

        assertEquals(DistantStrategy.State.builder().rewardPerMove(1).moves(4).build(), heap.poll());
    }

    @Test
    public void testDistant_Looping() {
        List<Board> boards = readBoardsFromFile("snake-loops-in-center.txt");
        strategy.nextActions(boards.get(0));
    }

    @Test
    public void testDistant_ShallIgnoreDeadEnds() {
        List<Board> boards = readBoardsFromFile("dead-ends.txt");

        assertFalse(strategy.nextActions(boards.get(0)).contains(Direction.UP));
        assertFalse(strategy.nextActions(boards.get(1)).contains(Direction.UP));
    }

    @Test
    public void testDistant_ShallEatStoneWithFurry() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");
        Board board = boards.get(8);
        board.setMyFurry(8);
        assertEquals(Sets.newHashSet(Direction.RIGHT), strategy.nextActions(board));
    }

    @Test
    public void testDistant_ShallGoToGoldViaApples() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");
        Board board = boards.get(18);
        assertEquals(Sets.newHashSet(Direction.LEFT), strategy.nextActions(board));
    }

    @Test
    public void testDistant_ShallEatStone() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");

        // assertEquals(Sets.newHashSet(Direction.LEFT, Direction.DOWN), strategy.nextActions(boards.get(24)));
        assertEquals(Sets.newHashSet(Direction.RIGHT, Direction.DOWN), strategy.nextActions(boards.get(26)));
    }

    @Test
    public void testDistant_IndexOutOfBound() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");
        assertEquals(Sets.newHashSet(Direction.RIGHT), strategy.nextActions(boards.get(33)));
    }

    @Test
    public void testDistant_ShallEatGoldEagerly() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");
        boards.get(37).setEnemies(BoardUtils.findEnemies(boards.get(37)));
        assertEquals(Sets.newHashSet(Direction.RIGHT, Direction.DOWN), strategy.nextActions(boards.get(37)));
    }

    @Test
    public void testDistant_ShallEatGoldEagerly_Purified() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");
        boards.get(38).setEnemies(BoardUtils.findEnemies(boards.get(38)));
        assertEquals(Sets.newHashSet(Direction.RIGHT, Direction.DOWN), strategy.nextActions(boards.get(38)));
    }

    @Test
    public void testDistant_ShouldNotGoForApple() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");
        boards.get(39).setEnemies(BoardUtils.findEnemies(boards.get(39)));
        assertEquals(Sets.newHashSet(Direction.UP), strategy.nextActions(boards.get(39)));
    }

    @Test
    public void testDistant_IndexOutOfBound2() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");
        boards.get(40).setEnemies(BoardUtils.findEnemies(boards.get(40)));
        assertEquals(Sets.newHashSet(Direction.DOWN), strategy.nextActions(boards.get(40)));
    }

    @Test
    public void testDistant_ShallNoGoDown() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");
        boards.get(41).setEnemies(BoardUtils.findEnemies(boards.get(41)));
        assertEquals(Sets.newHashSet(Direction.LEFT), strategy.nextActions(boards.get(41)));
    }

    @Test
    public void testDistant53() {
        List<Board> boards = readBoardsFromFile("snake-positions.txt");
        boards.get(53).setEnemies(BoardUtils.findEnemies(boards.get(53)));
        assertEquals(Sets.newHashSet(Direction.DOWN), strategy.nextActions(boards.get(53)));
    }

}

