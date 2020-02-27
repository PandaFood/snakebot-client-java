package se.cygni.snake.brain;

import se.cygni.snake.api.model.SnakeDirection;
import se.cygni.snake.client.MapUtil;

import java.util.List;
import java.util.Map;

/**
 * Created by Jonathan Brorsson on 2020-02-28.
 */
public class Obvious extends Sense {

    private Double reductionFactor = 0.0;

    @Override
    public Map<SnakeDirection, Double> getMovesRanked(MapUtil mapUtil, List<String> liveSnakes) {
        Map<SnakeDirection, Double> movableDirections = getPrioTemplate();

        if (!mapUtil.canIMoveInDirection(SnakeDirection.UP)) {
            movableDirections.put(SnakeDirection.UP, reductionFactor);
        }
        if (!mapUtil.canIMoveInDirection(SnakeDirection.DOWN)) {
            movableDirections.put(SnakeDirection.DOWN, reductionFactor);
        }
        if (!mapUtil.canIMoveInDirection(SnakeDirection.RIGHT)) {
            movableDirections.put(SnakeDirection.RIGHT, reductionFactor);
        }
        if (!mapUtil.canIMoveInDirection(SnakeDirection.LEFT)) {
            movableDirections.put(SnakeDirection.LEFT, reductionFactor);
        }
        return movableDirections;
    }
}
