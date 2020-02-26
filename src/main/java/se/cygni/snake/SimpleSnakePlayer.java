package se.cygni.snake;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.WebSocketSession;
import se.cygni.snake.api.event.*;
import se.cygni.snake.api.exception.InvalidPlayerName;
import se.cygni.snake.api.model.*;
import se.cygni.snake.api.response.PlayerRegistered;
import se.cygni.snake.api.util.GameSettingsUtils;
import se.cygni.snake.client.AnsiPrinter;
import se.cygni.snake.client.BaseSnakeClient;
import se.cygni.snake.client.MapCoordinate;
import se.cygni.snake.client.MapUtil;

import java.util.*;
import java.util.Map;

public class SimpleSnakePlayer extends BaseSnakeClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleSnakePlayer.class);

    // Set to false if you want to start the game from a GUI
    private static final boolean AUTO_START_GAME = true;

    // Personalise your game ...
    private static final String SERVER_NAME = "snake.cygni.se";
    private static final int SERVER_PORT = 80;

    private static final GameMode GAME_MODE = GameMode.TRAINING;
    private static final String SNAKE_NAME = "DepSnek";


    private static final int DEPTH = 30;
    private ArrayList<MapCoordinate> list = new ArrayList<>();

    private int currentDirection = -1;

    private static List<SnakeDirection> directions = new ArrayList<>();

    private Random random = new Random();


    // Set to false if you don't want the game world printed every game tick.
    private static final boolean ANSI_PRINTER_ACTIVE = false;
    private AnsiPrinter ansiPrinter = new AnsiPrinter(ANSI_PRINTER_ACTIVE, true);

    public static void main(String[] args) {
        SimpleSnakePlayer simpleSnakePlayer = new SimpleSnakePlayer();


        // Let's see in which directions I can move
        directions.add(SnakeDirection.UP);
        directions.add(SnakeDirection.RIGHT);
        directions.add(SnakeDirection.LEFT);
        directions.add(SnakeDirection.DOWN);

        try {
            ListenableFuture<WebSocketSession> connect = simpleSnakePlayer.connect();
            connect.get();
        } catch (Exception e) {
            LOGGER.error("Failed to connect to server", e);
            System.exit(1);
        }

        startTheSnake(simpleSnakePlayer);
    }

    /**
     * The Snake client will continue to run ...
     * : in TRAINING mode, until the single game ends.
     * : in TOURNAMENT mode, until the server tells us its all over.
     */
    private static void startTheSnake(final SimpleSnakePlayer simpleSnakePlayer) {

        Runnable task = () -> {
            do {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (simpleSnakePlayer.isPlaying());

            LOGGER.info("Shutting down");
        };

        Thread thread = new Thread(task);
        thread.start();
    }

    @Override
    public void onMapUpdate(MapUpdateEvent mapUpdateEvent) {
        ansiPrinter.printMap(mapUpdateEvent);

        // MapUtil contains lot's of useful methods for querying the map!
        MapUtil mapUtil = new MapUtil(mapUpdateEvent.getMap(), getPlayerId());

        if (currentDirection == -1) {
            getNewDirection();
        }

        // Register action here!
        registerMove(mapUpdateEvent.getGameTick(), depfist(mapUtil));
    }

    private SnakeDirection depfist(MapUtil mapUtil) {
        SnakeDirection dir;

        do {
            MapCoordinate currentPos = mapUtil.getMyPosition();
            generateStart(mapUtil);
            checkCurrentPath(mapUtil);
            dir = translateDirection(currentPos, list.get(0));
            list.remove(0);
        } while (!mapUtil.canIMoveInDirection(dir));

        //LOGGER.info("Done, giving direction: " + dir);
        return dir;
    }

    private void generateStart(MapUtil mapUtil) {

        while (list.isEmpty()) {
            getNewDirection();

            if (mapUtil.canIMoveInDirection(translateDirection(currentDirection))) {
                list.add(getNextCoordinates(currentDirection, mapUtil.getMyPosition()));
            }
        }
    }

    private void generateNewPath(MapUtil mapUtil) {

        MapCoordinate checkPos;
        int counter = 0;
        //LOGGER.info("CurrentPos: " + mapUtil.getMyPosition());

        while (list.size() < DEPTH) {

            if (list.isEmpty()) {
                generateStart(mapUtil);
            }
            checkPos = getNextCoordinates(currentDirection, list.get(list.size() - 1));

            //LOGGER.info("Checking pos: " + checkPos + " with tile: " + mapUtil.getTileAt(checkPos).getClass());
            if (mapUtil.isCoordinateOutOfBounds(checkPos) ||
                    mapUtil.getTileAt(checkPos) instanceof MapObstacle ||
                    mapUtil.getTileAt(checkPos) instanceof MapSnakeBody ||
                    mapUtil.getTileAt(checkPos) instanceof MapSnakeHead) {
                //!((mapUtil.getTileAt(checkPos) instanceof MapEmpty) || (mapUtil.getTileAt(checkPos) instanceof MapFood))){

                if (counter <= 5) {
                    getNewDirection();
                    counter++;
                } else {
                    counter = 0;
                    if (list.size() > 15)
                        list.subList(list.size() - 5, list.size()).clear();
                    else
                        list.clear();
                }
            } else {
                list.add(checkPos);
            }
        }

        //LOGGER.info(list.toString());

    }

    private void checkCurrentPath(MapUtil mapUtil) {

        generateNewPath(mapUtil);

        for (int i = 0; i < list.size(); i++) {

            MapCoordinate checkPos = list.get(i);

            if (mapUtil.isCoordinateOutOfBounds(checkPos) ||
                    mapUtil.getTileAt(checkPos) instanceof MapObstacle ||
                    mapUtil.getTileAt(checkPos) instanceof MapSnakeBody ||
                    mapUtil.getTileAt(checkPos) instanceof MapSnakeHead) {
                //!((mapUtil.getTileAt(checkPos) instanceof MapEmpty) || (mapUtil.getTileAt(checkPos) instanceof MapFood))){

                //LOGGER.info("Collision at: " + checkPos + " with " + mapUtil.getTileAt(checkPos));
                //LOGGER.info("My path is: " + list);

               /* if(list.size() > 5 && list.size() < 10)
                    list.subList(i - 5, list.size()).clear();
                else if(list.size() >= 10)
                    list.subList(i, list.size()).clear();
                else if(!list.isEmpty())
                    list.remove(list.size() - 1);
                else*/
                list.clear();
                list = new ArrayList<>();

                generateNewPath(mapUtil);
                //    LOGGER.info("New Path: " + list.toString());
                break;

            }
        }

    }


    private void getNewDirection() {
        currentDirection = random.nextInt(4);
        //LOGGER.info(String.valueOf(currentDirection));
    }

    private SnakeDirection translateDirection(int direction) {
        return directions.get(direction);
    }

    private SnakeDirection translateDirection(MapCoordinate position, MapCoordinate target) {

        SnakeDirection direction = null;
/*
         0 = UP
         1 = RIGHT
         2 = LEFT
         3 = DOWN
*/

        if (position.x == target.x) {
            if (position.y < target.y) {
                direction = directions.get(3);
            } else {
                direction = directions.get(0);
            }

        } else if (position.y == target.y) {
            if (position.x < target.x) {
                direction = directions.get(1);
            } else {
                direction = directions.get(2);
            }
        }

        //LOGGER.info("Going: " + direction);
        return direction;
    }

    private MapCoordinate getNextCoordinates(int direction, MapCoordinate pos) {
        MapCoordinate coords = new MapCoordinate(pos.x, pos.y);

        //LOGGER.info("Inital coords:" + coords.x + ":" + coords.y + "   - Direction: " + direction);

        switch (direction) {
            case 0:
                coords = new MapCoordinate(pos.x, pos.y + 1);

                break;
            case 1:
                coords = new MapCoordinate(pos.x + 1, pos.y);

                break;
            case 2:
                coords = new MapCoordinate(pos.x - 1, pos.y);

                break;
            case 3:
                coords = new MapCoordinate(pos.x, pos.y - 1);

                break;
        }

        //LOGGER.info("Post coords:" + coords.x + ":" + coords.y + "   - Direction: " + direction);
        return coords;

    }

    @Override
    public void onInvalidPlayerName(InvalidPlayerName invalidPlayerName) {
        LOGGER.debug("InvalidPlayerNameEvent: " + invalidPlayerName);
    }

    @Override
    public void onSnakeDead(SnakeDeadEvent snakeDeadEvent) {
        LOGGER.info("A snake {} died by {}",
                snakeDeadEvent.getPlayerId(),
                snakeDeadEvent.getDeathReason());
    }

    @Override
    public void onGameResult(GameResultEvent gameResultEvent) {
        LOGGER.info("Game result:");
        gameResultEvent.getPlayerRanks().forEach(playerRank -> LOGGER.info(playerRank.toString()));
    }

    @Override
    public void onGameEnded(GameEndedEvent gameEndedEvent) {
        LOGGER.debug("GameEndedEvent: " + gameEndedEvent);
    }

    @Override
    public void onGameStarting(GameStartingEvent gameStartingEvent) {
        LOGGER.debug("GameStartingEvent: " + gameStartingEvent);
    }

    @Override
    public void onPlayerRegistered(PlayerRegistered playerRegistered) {
        LOGGER.info("PlayerRegistered: " + playerRegistered);

        if (AUTO_START_GAME) {
            startGame();
        }
    }

    @Override
    public void onTournamentEnded(TournamentEndedEvent tournamentEndedEvent) {
        LOGGER.info("Tournament has ended, winner playerId: {}", tournamentEndedEvent.getPlayerWinnerId());
        int c = 1;
        for (PlayerPoints pp : tournamentEndedEvent.getGameResult()) {
            LOGGER.info("{}. {} - {} points", c++, pp.getName(), pp.getPoints());
        }
    }

    @Override
    public void onGameLink(GameLinkEvent gameLinkEvent) {
        LOGGER.info("The game can be viewed at: {}", gameLinkEvent.getUrl());
    }

    @Override
    public void onSessionClosed() {
        LOGGER.info("Session closed");
    }

    @Override
    public void onConnected() {
        LOGGER.info("Connected, registering for training...");
        GameSettings gameSettings = GameSettingsUtils.trainingWorld();
        registerForGame(gameSettings);
    }

    @Override
    public String getName() {
        return SNAKE_NAME;
    }

    @Override
    public String getServerHost() {
        return SERVER_NAME;
    }

    @Override
    public int getServerPort() {
        return SERVER_PORT;
    }

    @Override
    public GameMode getGameMode() {
        return GAME_MODE;
    }
}
