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

import com.codenjoy.dojo.snakebattle.strategy.distant.BoardUtils;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TestUtil {

    @SneakyThrows
    public static List<Board> readBoardsFromFile(String file) {
        List<Board> boards = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(ClassLoader.getSystemResource("replies/" + file).toURI()));
        String board = null;
        for (String line : lines) {
            if (line.startsWith("//")) {
                continue;
            }
            if (board == null) {
                board = line;
            } else {
                board = board + line;
                if (line.equals("☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼☼")) {
                    boards.add(board(board));
                    board = null;
                }
            }
        }
        return boards;
    }

    private static Board board(String boardString) {
        Board board = (Board) new Board().forString(boardString);
        board.setEnemies(BoardUtils.findEnemies(board));
        return board;
    }

}
