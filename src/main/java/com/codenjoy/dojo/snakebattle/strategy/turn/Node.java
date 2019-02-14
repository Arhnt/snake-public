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
import lombok.*;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;

@Log4j2
@Data
@EqualsAndHashCode(of = {"snakeLength", "furry", "flight", "point"})
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Node {

    private Point point;
    private int snakeLength;
    private int furry;
    private int flight;
    private double value;
    private Direction direction;
    private String board;

    private List<Node> leaves = new ArrayList<>();
    private List<Node> parents = new ArrayList<>();

    public void propagateValue(double reward, int step) {
        this.value = value + reward * Math.pow(0.95, step);
        for (Node parent : parents) {
            parent.propagateValue(reward, step + 1);
        }
    }
}
