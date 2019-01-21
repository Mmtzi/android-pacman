package com.muntzinger.pacmandeluxe.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.muntzinger.pacmandeluxe.sprites.Banana;
import com.muntzinger.pacmandeluxe.sprites.Border;
import com.muntzinger.pacmandeluxe.sprites.Dot;
import com.muntzinger.pacmandeluxe.sprites.Ghost;
import com.muntzinger.pacmandeluxe.sprites.MapObject;
import com.muntzinger.pacmandeluxe.sprites.Pacman;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by yurai on 13.01.2018.
 */

public class PlayState extends State {

    private final float GHOSTDIRECTION = 0.85f;
    private final Integer GHOSTVELOCITY = 200;
    private final Integer PACMANVELOCITY = 150;

    protected SpriteBatch batch;
    private float timer;
    private Socket socket;
    private Pacman player;

    private Array<Pacman> allPacmans;
    private final float UPDATETIME = 1/60f;
    private OrthographicCamera gameCam;
    private Boolean isSpectator;
    private Boolean isHost;
    private Integer myPlayerId;
    private Integer countOfPlayers;
    private Texture tempTexture;
    private Array<Texture> textureArray;
    private static Integer[][] map;
    private Array<Border> borderList;
    private Array<MapObject> consumableList;
    private Array<Ghost> ghostList;
    private BitmapFont font;
    private Boolean serverResponded;
    private int textureDirection;
    private int mapID;
    private boolean gameIsFinished;
    private Sound deathsound;


    public PlayState(GameStateManager manager, Integer mapIDp, Boolean isSpectatorp, Boolean isHostp, Integer countOfPlayersp, Socket socketp, Integer myPlayerIdp, Array<Texture> textureArrayp, SpriteBatch batchp, BitmapFont fontp) {
        super(manager);
        deathsound = Gdx.audio.newSound(Gdx.files.internal("sound/death.ogg"));
        textureArray = textureArrayp;
        gameCam = new OrthographicCamera();
        mapID = mapIDp;
        isSpectator = isSpectatorp;
        isHost = isHostp;
        socket= socketp;
        myPlayerId = myPlayerIdp;
        countOfPlayers = countOfPlayersp;
        batch = batchp;
        font = fontp;
        gameIsFinished= false;
        serverResponded = false;
        borderList = new Array<Border>();
        consumableList = new Array<MapObject>();
        ghostList = new Array<Ghost>();
        allPacmans = new Array<Pacman>();
        font.setColor(Color.BLACK);
        System.out.println("Accelormeter available: "+Gdx.input.isPeripheralAvailable(Input.Peripheral.Accelerometer));
        configSocketEvents();
        //short sleep duration for notHosts so that the host can set the game up (load the map etc..)
        if(!isHost) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (!isSpectator) {
            createPlayer();
        }
        //everybody needs the map locally
        createMap(mapID);
    }

    private void createPlayer() {
        //the personal id describes the color
        if (myPlayerId == 1) {
            tempTexture = textureArray.get(8);
        }
        else if (myPlayerId == 2) {
            tempTexture = textureArray.get(12);
        }
        else if (myPlayerId == 3) {
            tempTexture = textureArray.get(16);
        }
        else if (myPlayerId == 4) {
            tempTexture = textureArray.get(20);
        }
        System.out.println(myPlayerId);
        player = new Pacman(myPlayerId, tempTexture);
        allPacmans.add(player);
        Gdx.app.log("SocketIO","created Player with Id: "+myPlayerId);
        socket.emit("createPlayer",myPlayerId);
    }

    private void createMap(Integer mapID) {
        //map 30x30 px per tile => map of 18x32
        if (mapID==1) {
            map = new Integer[][]{
                    {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                    {1, 3, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 3, 1},
                    {1, 0, 1, 1, 1, 0, 1, 0, 1, 1, 0, 1, 0, 1, 1, 1, 0, 1},
                    {1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1},
                    {1, 0, 1, 0, 1, 0, 1, 1, 1, 1, 0, 1, 0, 1, 0, 1, 0, 1},
                    {1, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1},
                    {1, 1, 1, 0, 1, 0, 1, 0, 1, 1, 0, 1, 0, 1, 0, 1, 1, 1},
                    {1, 0, 0, 0, 1, 0, 1, 0, 1, 1, 0, 1, 0, 1, 0, 0, 0, 1},
                    {1, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 1},
                    {1, 0, 1, 0, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1, 0, 1, 0, 1},
                    {1, 0, 1, 0, 0, 0, 0, 1, 2, 2, 1, 0, 0, 0, 0, 1, 0, 1},
                    {1, 0, 1, 0, 1, 0, 1, 1, 2, 2, 1, 1, 0, 1, 0, 1, 0, 1},
                    {1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 1},
                    {1, 1, 1, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 1, 1, 1},
                    {1, 3, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 3, 1},
                    {1, 0, 1, 0, 1, 0, 1, 1, 1, 1, 1, 1, 0, 1, 0, 1, 0, 1},
                    {1, 0, 1, 0, 1, 0, 1, 0, 0, 0, 0, 1, 0, 1, 0, 1, 0, 1},
                    {1, 0, 1, 0, 1, 0, 1, 0, 1, 1, 0, 1, 0, 1, 0, 1, 0, 1},
                    {1, 0, 1, 0, 1, 0, 1, 0, 1, 1, 0, 1, 0, 1, 0, 1, 0, 1},
                    {1, 0, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 1, 0, 1},
                    {1, 0, 1, 0, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 0, 1, 0, 1},
                    {1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1},
                    {1, 0, 1, 0, 1, 0, 1, 0, 1, 1, 0, 1, 0, 1, 0, 1, 0, 1},
                    {1, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 1},
                    {1, 0, 1, 0, 1, 0, 1, 1, 1, 1, 1, 1, 0, 1, 0, 1, 0, 1},
                    {1, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 1},
                    {1, 0, 1, 0, 1, 1, 1, 2, 2, 2, 2, 1, 1, 1, 0, 1, 0, 1},
                    {1, 0, 1, 0, 1, 0, 0, 2, 2, 2, 2, 0, 0, 1, 0, 1, 0, 1},
                    {1, 3, 0, 0, 0, 0, 1, 2, 2, 2, 2, 1, 0, 0, 0, 0, 3, 1},
                    {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                    {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                    {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}};
        } else {
            map = new Integer[][]{
                    {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                    {1, 0, 0, 0, 1, 3, 1, 0, 0, 0, 0, 1, 3, 1, 0, 0, 0, 1},
                    {1, 0, 0, 0, 1, 0, 1, 0, 1, 1, 0, 1, 0, 1, 0, 0, 0, 1},
                    {1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1},
                    {1, 1, 1, 0, 1, 0, 1, 1, 1, 1, 1, 1, 0, 1, 0, 1, 1, 1},
                    {1, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1},
                    {1, 0, 1, 0, 1, 0, 1, 0, 1, 1, 0, 1, 0, 1, 0, 1, 0, 1},
                    {1, 0, 1, 0, 1, 0, 1, 0, 1, 1, 0, 1, 0, 1, 0, 0, 0, 1},
                    {1, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 1},
                    {1, 0, 1, 0, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1, 0, 1, 0, 1},
                    {1, 0, 1, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 1, 0, 1},
                    {1, 0, 1, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 1, 0, 1},
                    {1, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 1},
                    {1, 0, 1, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 1, 0, 1},
                    {1, 0, 1, 0, 1, 0, 3, 1, 0, 0, 1, 3, 0, 1, 0, 1, 0, 1},
                    {1, 0, 1, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 1, 0, 1},
                    {1, 3, 1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 1, 3, 1},
                    {1, 0, 1, 0, 0, 0, 1, 0, 1, 1, 0, 1, 0, 0, 0, 1, 0, 1},
                    {1, 0, 1, 0, 1, 0, 1, 0, 1, 1, 0, 1, 0, 1, 0, 1, 0, 1},
                    {1, 0, 1, 0, 1, 0, 0, 0, 1, 1, 0, 0, 0, 1, 0, 1, 0, 1},
                    {1, 0, 1, 0, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 0, 1, 0, 1},
                    {1, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 1},
                    {1, 0, 1, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 1, 0, 1},
                    {1, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 1},
                    {1, 0, 0, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 0, 0, 1},
                    {1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1},
                    {1, 0, 0, 0, 1, 0, 1, 2, 2, 2, 2, 1, 0, 1, 0, 0, 0, 1},
                    {1, 0, 0, 0, 1, 0, 1, 2, 2, 2, 2, 1, 0, 1, 0, 0, 0, 1},
                    {1, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 0, 0, 0, 0, 0, 0, 1},
                    {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                    {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                    {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}};
        }
        for (int i =0; i<map[0].length; i++){
            for (int j = 0; j <map.length; j++) {
                if(map[j][i]==1) {
                    borderList.add(new Border(new Vector2(30*i,30*j), textureArray.get(0)));
                } else if (map[j][i]==0){
                    consumableList.add(new Dot(new Vector2(30*i,30*j), textureArray.get(1)));
                } else if (map[j][i] == 2) {
                } else if (map[j][i] == 3 ) {
                    consumableList.add(new Banana(new Vector2(30*i,30*j), textureArray.get(24)));
                }
            }
        }
        ghostList.add(new Ghost(1,textureArray.get(4),31,31));
        ghostList.add(new Ghost(2,textureArray.get(5), 481,31));
        ghostList.add(new Ghost(3,textureArray.get(6), 31, 841));
        ghostList.add(new Ghost(4,textureArray.get(7),481, 841));
        JSONObject serverInteractiveMapObjectList = new JSONObject();
        try {
            serverInteractiveMapObjectList.put("consumableList", consumableList);
            serverInteractiveMapObjectList.put("ghostList", ghostList);
            socket.emit("InteractiveMapObjects", serverInteractiveMapObjectList);
            System.out.println("sending InteractiveMapObjects Data");
        } catch(JSONException e) {
            Gdx.app.log("SOCKET.IO", "Error sending InteractiveMapObjects data");
        }

    }

    private void configSocketEvents() {
        socket.on("newPlayer", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Integer playerID = (Integer) args[0];
                try {
                    if (playerID == 1) {
                        tempTexture = textureArray.get(8); //pac1r
                    }
                    else if (playerID == 2) {
                        tempTexture = textureArray.get(12); //pac2r
                    }
                    else if (playerID == 3) {
                        tempTexture = textureArray.get(16); //pac3r
                    }
                    else if (playerID == 4) {
                        tempTexture = textureArray.get(20); //pac4r
                    }
                    Gdx.app.log("SocketIO", "New Player Connect: " + playerID);
                    allPacmans.add(new Pacman(playerID, tempTexture));
                } catch (Exception e) {
                    Gdx.app.log("SocketIO", "Error getting New Player ID");
                }
            }
        }).on("playerDisconnected", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Integer id = (Integer) args[0];
                try {
                    allPacmans.removeIndex(id-1);
                    Gdx.app.log("SocketIO" , "player with ID: "+id);
                } catch (Exception e) {
                    Gdx.app.log("SocketIO", "Error getting disconnected Player ID");
                }
            }
        }).on("playerMoved", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                try {
                    Integer playerID = data.getInt("id");
                    Double x = data.getDouble("x");
                    Double y = data.getDouble("y");
                    Integer p = data.getInt("p");
                    Integer dir = data.getInt("dir");
                    Integer deaths = data.getInt("deaths");
                    Vector2 newPosition = new Vector2(x.floatValue(),y.floatValue());
                    for (Pacman each : allPacmans) {
                        if (each.getMyPlayerId()==playerID) {
                            each.setActualPosition(newPosition);
                            each.getRectangle().setPosition(newPosition.x,newPosition.y);
                            each.setPoints(p);
                            each.setTexture(textureArray.get(4+dir-1+4*playerID));
                            each.setDeaths(deaths);
                        }
                    }
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error updating Player Positions");
                }
            }
        }).on("ghostMoved", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                try {
                    Integer ghostID = data.getInt("id");
                    Double x = data.getDouble("x");
                    Double y = data.getDouble("y");
                    Vector2 newPosition = new Vector2(x.floatValue(),y.floatValue());
                    if (ghostList.get(ghostID-1)!= null) {
                        ghostList.get(ghostID-1).setActualPosition(newPosition);
                        ghostList.get(ghostID-1).getRectangle().setPosition(newPosition);
                    }
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error updating Ghost Position");
                }
            }
        }).on("updateConsumables", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                try {
                    Integer updatedConsumable = data.getInt("index");
                    consumableList.removeIndex(updatedConsumable);
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error updating Consumable");
                }
            }
        }).on("endGame", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                gameIsFinished = true;
            }
        });
    }

    protected void updateServer(float deltaTime) {
        timer += deltaTime;
        if(timer >= UPDATETIME && player != null){
            JSONObject data = new JSONObject();
            try {
                data.put("id", myPlayerId);
                data.put("x", player.getActualPosition().x);
                data.put("y", player.getActualPosition().y);
                data.put("p", player.getPoints());
                data.put("dir",textureDirection);
                data.put("deaths", player.getDeaths());
                socket.emit("playerMoved", data);
            } catch(JSONException e) {
                Gdx.app.log("SOCKET.IO", "Error sending update data");
            }
        }

    }
    @Override
    protected void handleinput(float dt) {
        if(player != null){
            float accelX =0;
            float accelY =0;
            //steering with Accelerometer for non desktop users
            if(Gdx.input.isPeripheralAvailable(Input.Peripheral.Accelerometer)) {
                accelX = Gdx.input.getAccelerometerX();
                accelY = Gdx.input.getAccelerometerY();
            }
            Vector2 direction = new Vector2(player.getActualPosition());
            //left
            if(Gdx.input.isKeyPressed(Input.Keys.LEFT) || (accelX*-1 <0 && Math.abs(accelX) > Math.abs(accelY))){
                //texture direction to send to other clients so that every pacman shows in the direction he is trying to walk
                textureDirection = 3;
                //update facing texture local
                player.setTexture(textureArray.get(4+4*myPlayerId+textureDirection-1));
                Boolean collision = false;
                //put rectangle of pacman the movement in front and check if it hits an obstacle
                player.setRectangle(new Rectangle(player.getActualPosition().x- PACMANVELOCITY *dt, player.getActualPosition().y, player.getRectangle().getWidth(), player.getRectangle().getHeight()));
                for (int i=0; i <borderList.size; i++) { //check if pac hits a border, if so put the rectangle back and deny the real movement
                    if (borderList.get(i).catchingMapObject(player.getRectangle())) {
                        collision = true;
                        player.setRectangle(new Rectangle(player.getActualPosition().x,player.getActualPosition().y, player.getRectangle().getWidth(), player.getRectangle().getHeight()));
                        break;
                    }
                }
                for (int i=0; i<allPacmans.size; i++) { //same for other players
                    if(allPacmans.get(i).getMyPlayerId()!= myPlayerId){
                        if (allPacmans.get(i).getRectangle().overlaps(player.getRectangle())){
                            collision = true;
                            player.setRectangle(new Rectangle(player.getActualPosition().x,player.getActualPosition().y, player.getRectangle().getWidth(), player.getRectangle().getHeight()));
                            break;
                        }
                    }
                }
                if (!collision) { // if no collision pac goes his way
                    direction = new Vector2(player.getActualPosition().x - PACMANVELOCITY * dt, player.getActualPosition().y);
                }
                //right
            } else if(Gdx.input.isKeyPressed(Input.Keys.RIGHT) || (accelX <0 && Math.abs(accelX) > Math.abs(accelY))){
                textureDirection = 1;
                player.setTexture(textureArray.get(4+4*myPlayerId+textureDirection-1));
                Boolean collision = false;
                player.setRectangle(new Rectangle(player.getActualPosition().x+ PACMANVELOCITY *dt, player.getActualPosition().y, player.getRectangle().getWidth(), player.getRectangle().getHeight()));
                for (int i=0; i <borderList.size; i++) {
                    if (borderList.get(i).catchingMapObject(player.getRectangle())) {
                        collision = true;
                        player.setRectangle(new Rectangle(player.getActualPosition().x,player.getActualPosition().y, player.getRectangle().getWidth(), player.getRectangle().getHeight()));
                        break;
                    }
                }
                for (int i=0; i<allPacmans.size; i++) {
                    if(allPacmans.get(i).getMyPlayerId()!= myPlayerId){
                        if (allPacmans.get(i).getRectangle().overlaps(player.getRectangle())){
                            collision = true;
                            player.setRectangle(new Rectangle(player.getActualPosition().x,player.getActualPosition().y, player.getRectangle().getWidth(), player.getRectangle().getHeight()));
                            break;
                        }
                    }
                }
                if (!collision) {
                    direction = new Vector2(player.getActualPosition().x+ PACMANVELOCITY *dt, player.getActualPosition().y);
                }
                //up
            } else if(Gdx.input.isKeyPressed(Input.Keys.UP) || (accelY <0 && Math.abs(accelY) > Math.abs(accelX))){
                textureDirection =4;
                player.setTexture(textureArray.get(4+4*myPlayerId+textureDirection-1));
                Boolean collision = false;
                player.setRectangle(new Rectangle(player.getActualPosition().x, player.getActualPosition().y+ PACMANVELOCITY *dt, player.getRectangle().getWidth(), player.getRectangle().getHeight()));
                for (int i=0; i <borderList.size; i++) {
                    if (borderList.get(i).catchingMapObject(player.getRectangle())) {
                        collision = true;
                        player.setRectangle(new Rectangle(player.getActualPosition().x,player.getActualPosition().y, player.getRectangle().getWidth(), player.getRectangle().getHeight()));
                        break;
                    }
                }
                for (int i=0; i<allPacmans.size; i++) {
                    if(allPacmans.get(i).getMyPlayerId()!= myPlayerId){
                        if (allPacmans.get(i).getRectangle().overlaps(player.getRectangle())){
                            collision = true;
                            player.setRectangle(new Rectangle(player.getActualPosition().x,player.getActualPosition().y, player.getRectangle().getWidth(), player.getRectangle().getHeight()));
                            break;
                        }
                    }
                }
                if (!collision) {
                    direction = new Vector2(player.getActualPosition().x, player.getActualPosition().y+ PACMANVELOCITY *dt);
                }
                //down
            } else if(Gdx.input.isKeyPressed(Input.Keys.DOWN)|| (accelY*-1 <0 && Math.abs(accelY) > Math.abs(accelX))){
                textureDirection=2;
                player.setTexture(textureArray.get(4+4*myPlayerId+textureDirection-1));
                Boolean collision = false;
                player.setRectangle(new Rectangle(player.getActualPosition().x, player.getActualPosition().y- PACMANVELOCITY *dt, player.getRectangle().getWidth(), player.getRectangle().getHeight()));
                for (int i=0; i <borderList.size; i++) {
                    if (borderList.get(i).catchingMapObject(player.getRectangle())) {
                        collision = true;
                        player.setRectangle(new Rectangle(player.getActualPosition().x,player.getActualPosition().y, player.getRectangle().getWidth(), player.getRectangle().getHeight()));
                        break;
                    }
                }
                for (int i=0; i<allPacmans.size; i++) {
                    if(allPacmans.get(i).getMyPlayerId()!= myPlayerId){
                        if (allPacmans.get(i).getRectangle().overlaps(player.getRectangle())){
                            collision = true;
                            player.setRectangle(new Rectangle(player.getActualPosition().x,player.getActualPosition().y, player.getRectangle().getWidth(), player.getRectangle().getHeight()));
                            break;
                        }
                    }
                }
                if (!collision) {
                    direction = new Vector2(player.getActualPosition().x, player.getActualPosition().y- PACMANVELOCITY *dt);
                }
            } //finally set new position
            player.setActualPosition(direction);
        }

    }

    @Override
    public void update(float dt) {
        if(gameIsFinished) {
            socket.disconnect();
            manager.set(new PostGameState(manager, allPacmans, timer));
        }
        if(isHost) {
            checkIfFinished();
            updateGhosts(dt);
        }
        if (!isSpectator) {
            checkConsumableState();
            checkCollisionWithGhosts(dt);
            handleinput(dt);
        }
        updateServer(dt);
    }

    private void checkIfFinished() {
        //if all consumables were collected...
        if(consumableList.size==0) {
            socket.emit("endGame");
            try {
                //sleep for host again, he should be the last one to leave the game
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            socket.disconnect();
            manager.set(new PostGameState(manager, allPacmans, timer));
        }
    }

    private void checkCollisionWithGhosts(float dt) {
        //handle invulnerability
        if (player.getInvTimer()>0) {
            player.setInvTimer(player.getInvTimer() - dt);
            if (player.getInvTimer() <0) {
                player.setInvTimer(0);
            }
        }//check for hit with ghosts
        for (int i =0; i<ghostList.size; i++){
            Ghost ghost = ghostList.get(i);
            if (ghost.getRectangle().overlaps(player.getRectangle()) && player.getInvTimer() == 0.0) {
                player.setDeaths(player.getDeaths()+1);
                //invulnerability of one sec if hit by a ghost
                player.setInvTimer(3);
                deathsound.play(0.1f);
            }
        }
    }

    private void updateGhosts(float dt) {
        for(Ghost ghost : ghostList) {
            Vector2 newGhostPos = ghost.getActualPosition();
            //with the probability GHOSTDIRECTION the ghost will walk in the direction he walked before
            if(ghost.getPreviousDirection()==0 && Math.random()< GHOSTDIRECTION) {
                newGhostPos = moveUp(dt, ghost,newGhostPos);
            } else if(ghost.getPreviousDirection()==1  && Math.random()< GHOSTDIRECTION) {
                newGhostPos = moveRight(dt, ghost,newGhostPos);
            } else if(ghost.getPreviousDirection()==2  && Math.random()< GHOSTDIRECTION) {
                newGhostPos = moveDown(dt, ghost,newGhostPos);
            } else if(ghost.getPreviousDirection()==3  && Math.random()< GHOSTDIRECTION) {
                newGhostPos = moveLeft(dt, ghost,newGhostPos);
            } else {
                Vector2 targetPosition = player.getActualPosition();
                //range between 0.5 and 1.5, so that vectors can stretch and contract
                //we need a bit of randomness to give the pacs a chance to survive
                float xtarget = (float) (Math.random()+0.5);
                float xnew = (float) (Math.random()+0.5);
                float ytarget = (float) (Math.random()+0.5);
                float ynew = (float) (Math.random()+0.5);
                Vector2 directionVectorTemp;
                float directionVectorTempAbs;
                //every ghost will get an own direction he wants to walk (to the closest pacman in beeline)
                //first target position is the position of the local pac
                //so first direction vector is ghost 1 to pac 1 with a little bias because of the randomness
                Vector2 directionVector = new Vector2(targetPosition.x * xtarget - xnew * newGhostPos.x, targetPosition.y * ytarget - ynew * newGhostPos.y);
                //we need the length to check which player is the closest to hunt down.
                float directionVectorAbs = (float) Math.sqrt(Math.pow(directionVector.x,2) + Math.pow(directionVector.y,2));
                //for second pac
                if (allPacmans.size>1) {
                    targetPosition = allPacmans.get(1).getActualPosition();
                    directionVectorTemp = new Vector2(targetPosition.x * xtarget - xnew * newGhostPos.x, targetPosition.y * ytarget - ynew * newGhostPos.y);
                    directionVectorTempAbs = (float) Math.sqrt(Math.pow(directionVectorTemp.x,2) + Math.pow(directionVectorTemp.y,2));
                    if (directionVectorAbs > directionVectorTempAbs) {
                        directionVector = directionVectorTemp;
                    }
                    //for third pac
                } if (allPacmans.size>2) {
                    targetPosition = allPacmans.get(2).getActualPosition();
                    directionVectorTemp = new Vector2(targetPosition.x * xtarget - xnew * newGhostPos.x, targetPosition.y * ytarget - ynew * newGhostPos.y);
                    directionVectorTempAbs = (float) Math.sqrt(Math.pow(directionVectorTemp.x,2) + Math.pow(directionVectorTemp.y,2));
                    if (directionVectorAbs > directionVectorTempAbs) {
                        directionVector = directionVectorTemp;
                    }
                    //for fourth pac
                } if (allPacmans.size>3) {
                    targetPosition = allPacmans.get(3).getActualPosition();
                    directionVectorTemp = new Vector2(targetPosition.x * xtarget - xnew * newGhostPos.x, targetPosition.y * ytarget - ynew * newGhostPos.y);
                    directionVectorTempAbs = (float) Math.sqrt(Math.pow(directionVectorTemp.x,2) + Math.pow(directionVectorTemp.y,2));
                    if (directionVectorAbs > directionVectorTempAbs) {
                        directionVector = directionVectorTemp;
                    }
                }//check for every ghost which pac is the closest and go the direction..
                if (directionVector.x < 0 && Math.abs(directionVector.x) > Math.abs(directionVector.y)) {
                    newGhostPos = moveLeft(dt, ghost, newGhostPos);
                } else if (directionVector.x > 0 && Math.abs(directionVector.x) > Math.abs(directionVector.y)) {
                    newGhostPos = moveRight(dt, ghost, newGhostPos);
                } else if (directionVector.y < 0 && Math.abs(directionVector.y) > Math.abs(directionVector.x)) {
                    newGhostPos = moveDown(dt, ghost, newGhostPos);
                } else {
                    newGhostPos = moveUp(dt, ghost, newGhostPos);
                }
            }
            ghost.setActualPosition(newGhostPos);
            JSONObject data = new JSONObject();
            try {
                //send new ghostposition to the other players
                data.put("id", ghost.getGhostNumber());
                data.put("x", ghost.getActualPosition().x);
                data.put("y", ghost.getActualPosition().y);
                socket.emit("ghostMoved", data);
            } catch (JSONException e) {
                Gdx.app.log("SOCKET.IO", "Error sending ghost update data");
            }
        }
    }
    //check collision for the choosen movement
    private Vector2 moveUp(float dt, Ghost ghost, Vector2 newPosition) {
        Boolean collision = false;
        ghost.setRectangle(new Rectangle(ghost.getActualPosition().x, ghost.getActualPosition().y+ GHOSTVELOCITY * dt, ghost.getRectangle().getWidth(), ghost.getRectangle().getHeight()));
        for (int i = 0; i < borderList.size; i++) {
            if (borderList.get(i).catchingMapObject(ghost.getRectangle())) {
                collision = true;
                ghost.setRectangle(new Rectangle(ghost.getActualPosition().x, ghost.getActualPosition().y, ghost.getRectangle().getWidth(), ghost.getRectangle().getHeight()));
                break;
            }
        }
        if (!collision) {
            newPosition = new Vector2(ghost.getActualPosition().x , ghost.getActualPosition().y+ GHOSTVELOCITY * dt);
            ghost.setPreviousDirection(0);
        } else {
            ghost.setPreviousDirection(4);
        }
        return newPosition;
    }

    private Vector2 moveDown(float dt, Ghost ghost, Vector2 newPosition) {
        Boolean collision = false;
        ghost.setRectangle(new Rectangle(ghost.getActualPosition().x , ghost.getActualPosition().y- GHOSTVELOCITY * dt, ghost.getRectangle().getWidth(), ghost.getRectangle().getHeight()));
        for (int i = 0; i < borderList.size; i++) {
            if (borderList.get(i).catchingMapObject(ghost.getRectangle())) {
                collision = true;
                ghost.setRectangle(new Rectangle(ghost.getActualPosition().x, ghost.getActualPosition().y, ghost.getRectangle().getWidth(), ghost.getRectangle().getHeight()));
                break;
            }
        }
        if (!collision) {
            newPosition = new Vector2(ghost.getActualPosition().x , ghost.getActualPosition().y- GHOSTVELOCITY * dt);
            ghost.setPreviousDirection(2);
        } else {
            ghost.setPreviousDirection(4);
        }
        return newPosition;
    }

    private Vector2 moveRight(float dt, Ghost ghost, Vector2 newPosition) {
        Boolean collision = false;
        ghost.setRectangle(new Rectangle(ghost.getActualPosition().x + GHOSTVELOCITY * dt, ghost.getActualPosition().y, ghost.getRectangle().getWidth(), ghost.getRectangle().getHeight()));
        for (int i = 0; i < borderList.size; i++) {
            if (borderList.get(i).catchingMapObject(ghost.getRectangle())) {
                collision = true;
                ghost.setRectangle(new Rectangle(ghost.getActualPosition().x, ghost.getActualPosition().y, ghost.getRectangle().getWidth(), ghost.getRectangle().getHeight()));
                break;
            }
        }
        if (!collision) {
            newPosition = new Vector2(ghost.getActualPosition().x + GHOSTVELOCITY * dt, ghost.getActualPosition().y);
            ghost.setPreviousDirection(1);
        } else {
            ghost.setPreviousDirection(4);
        }
        return newPosition;
    }

    private Vector2 moveLeft(float dt, Ghost ghost, Vector2 newPosition) {
        Boolean collision = false;
        ghost.setRectangle(new Rectangle(ghost.getActualPosition().x - GHOSTVELOCITY * dt, ghost.getActualPosition().y, ghost.getRectangle().getWidth(), ghost.getRectangle().getHeight()));
        for (int i = 0; i < borderList.size; i++) {
            if (borderList.get(i).catchingMapObject(ghost.getRectangle())) {
                collision = true;
                ghost.setRectangle(new Rectangle(ghost.getActualPosition().x, ghost.getActualPosition().y, ghost.getRectangle().getWidth(), ghost.getRectangle().getHeight()));
                break;
            }
        }
        if (!collision) {
            newPosition = new Vector2(ghost.getActualPosition().x - GHOSTVELOCITY * dt, ghost.getActualPosition().y);
            ghost.setPreviousDirection(3);
        } else {
            ghost.setPreviousDirection(4);
        }
        return newPosition;
    }
    //update the status of the consumables and send an update to the other players...
    private void checkConsumableState() {
        for (int i = 0; i< consumableList.size; i++){
            MapObject consumable = consumableList.get(i);
            if (player != null) {
                if (consumable.catchingMapObject(player.getRectangle())) {
                    if (consumable instanceof Banana) {
                        player.setPoints(player.getPoints()+ 10);
                    } else if (consumable instanceof Dot) {
                        player.setPoints(player.getPoints() + 1);
                    }
                    consumableList.removeIndex(i);
                    JSONObject data = new JSONObject();
                    try {
                        data.put("index", i);
                        socket.emit("updateConsumables", data);
                    } catch(JSONException e) {
                        Gdx.app.log("SOCKET.IO", "Error sending update Consumable data");
                    }
                }
            }
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        for (MapObject border : borderList) {
            batch.draw(border.getTexture(),border.getPosition().x, border.getPosition().y);
        }
        for (MapObject consumable : consumableList) {
            batch.draw(consumable.getTexture(),consumable.getPosition().x, consumable.getPosition().y);
        }
        //self created hud...
        if(!isSpectator) {
            font.draw(batch,"Player: " + myPlayerId, 50 + (myPlayerId-1) * 100, 940);
            font.draw(batch,"Points: " + player.getPoints(), 50 + (myPlayerId-1) * 100, 920);
            font.draw(batch, "Deaths: "+ player.getDeaths(), 50 + (myPlayerId-1) * 100, 900);
        }
        //draw pacs and points of the other players
        for (int i =0; i<allPacmans.size; i++) {
            batch.draw(allPacmans.get(i).getTexture(), allPacmans.get(i).getActualPosition().x, allPacmans.get(i).getActualPosition().y);
            font.draw(batch, "Player: "+ allPacmans.get(i).getMyPlayerId(), 50+(allPacmans.get(i).getMyPlayerId()-1)*100, 940);
            font.draw(batch, "Points: " + allPacmans.get(i).getPoints(), 50+(allPacmans.get(i).getMyPlayerId()-1)*100, 920);
            font.draw(batch, "Deaths: "+ allPacmans.get(i).getDeaths(), 50 +(allPacmans.get(i).getMyPlayerId()-1)* 100, 900);
        }
        //draw ghost
        for (Ghost ghost : ghostList){
            batch.draw(ghost.getTexture(),ghost.getActualPosition().x,ghost.getActualPosition().y);
        }

        batch.end();
    }

    @Override
    public void dispose() {
        for (Texture each : textureArray
             ) {
            each.dispose();
        }

    }
}


