package se.cygni.snake.brain;

import se.cygni.snake.api.model.SnakeDirection;
import se.cygni.snake.client.MapUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jonathan Brorsson on 2020-02-28.
 */
public abstract class Sense {
    public abstract Map<SnakeDirection, Double> getMovesRanked(MapUtil mapUtil, List<String> liveSnakes);

    Map<SnakeDirection, Double> getPrioTemplate(){
        Map<SnakeDirection, Double> template = new HashMap<>();
        template.put(SnakeDirection.UP, 1.0);
        template.put(SnakeDirection.DOWN, 1.0);
        template.put(SnakeDirection.LEFT, 1.0);
        template.put(SnakeDirection.RIGHT, 1.0);
        return template;
    }
}
