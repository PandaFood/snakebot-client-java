package se.cygni.snake.brain;

import se.cygni.snake.api.model.SnakeDirection;
import se.cygni.snake.client.MapUtil;

import java.util.List;
import java.util.Map;

/**
 * Created by Jonathan Brorsson on 2020-02-28.
 */
public class Centre extends Sense {

    private Double prefered;
    private Double second;
    private Double third;
    private Double forth;

    public Centre(Double prefered, Double second, Double third, Double forth) {
        this.prefered = prefered;
        this.second = second;
        this.third = third;
        this.forth = forth;
    }


    @Override
    public Map<SnakeDirection, Double> getMovesRanked(MapUtil mapUtil, List<String> liveSnakes) {
        return null;
    }
}
