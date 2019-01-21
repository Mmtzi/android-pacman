package com.muntzinger.pacmandeluxe.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.muntzinger.pacmandeluxe.PacManMain;
import com.muntzinger.pacmandeluxe.sprites.Pacman;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by yurai on 02.02.2018.
 */

public class LobbyState extends State {
    private Socket socket;
    private Stage stage;

    private final Texture map1BtnTexture;
    private final TextureRegion map1BtnRegion;
    private final TextureRegionDrawable map1BtnRegionDrawable;
    private final ImageButton map1Btn;

    private final Texture map2BtnTexture;
    private final TextureRegion map2BtnRegion;
    private final TextureRegionDrawable map2BtnRegionDrawable;
    private final ImageButton map2Btn;

    private SpriteBatch batch;
    private Texture bg;
    private Texture p1;
    private Texture p2;
    private Texture p3;
    private Texture p4;
    private Texture p1r;
    private Texture p2r;
    private Texture p3r;
    private Texture p4r;
    private Texture pacMan1;
    private Texture pacMan2;
    private Texture pacMan3;
    private Texture pacMan4;
    private Texture ghost1;
    private Texture ghost2;
    private Texture ghost3;
    private Texture ghost4;
    private Array<Texture> textureArray;
    private BitmapFont title;
    private BitmapFont status;
    private boolean isSpectator;
    private boolean isHost;
    private Array<Boolean> playersInLobby;
    private Integer myPlayerId;
    private Integer countOfPlayers;
    private Boolean serverResponded;
    private Sound select;
    private String warningMessage;

    public LobbyState(GameStateManager manager, boolean isHostp, boolean isSpectatorp) {
        super(manager);
        select = Gdx.audio.newSound(Gdx.files.internal("sound/select.ogg"));
        isSpectator = isSpectatorp;
        isHost = isHostp;
        playersInLobby = new Array<Boolean>();
        textureArray = new Array<Texture>();
        batch = new SpriteBatch();
        serverResponded = false;
        title = new BitmapFont();
        status = new BitmapFont();
        countOfPlayers =0;
        warningMessage = "";       //load all Textures used in the Game before connecting to the socket.
        loadAllTextures();

        map1BtnTexture = new Texture("menu/map1.png");
        map1BtnRegion = new TextureRegion(map1BtnTexture);
        map1BtnRegionDrawable = new TextureRegionDrawable(map1BtnRegion);
        map1Btn = new ImageButton(map1BtnRegionDrawable); //Set the button up
        map1Btn.setPosition(PacManMain.WIDTH / 4 - map1Btn.getWidth() / 2, PacManMain.HEIGHT - 700);

        map2BtnTexture = new Texture("menu/map2.png");
        map2BtnRegion = new TextureRegion(map2BtnTexture);
        map2BtnRegionDrawable = new TextureRegionDrawable(map2BtnRegion);
        map2Btn = new ImageButton(map2BtnRegionDrawable); //Set the button up
        map2Btn.setPosition(PacManMain.WIDTH * 3 / 4 - map2Btn.getWidth() / 2, PacManMain.HEIGHT - 700);
        //add start map buttons only for the host
        if(isHost) {
            stage = new Stage(new ScreenViewport());
            stage.addActor(map1Btn);
            stage.addActor(map2Btn);
        }
        //connect to the socket
        connectSocket();
        configSocketEvents();
        //watches for other players and syncronizes the ui
        setUpConnections();

        Gdx.input.setInputProcessor(stage);
    }

    private void setUpConnections() {
        if (!isHost) {
            //only players who join need to check in which position they start (p2/p3 or p4)
            getConnectedPlayers();
        } else {
            serverResponded=true;
            //host is always player 1
            myPlayerId = 1;
        }
        while (true){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //the separate threats (socket connection and game thread) could cause "lost update problems"
            //the boolean serverResponded makes sure that no lost update occurs.
            if(!isSpectator && serverResponded) {
                System.out.println("Count of Players: "+countOfPlayers);
                //if there are already 4 players go pack to menu or you are without host in the lobby
                if(countOfPlayers >3 || (countOfPlayers < 1 && !isHost)) {
                    socket.disconnect();
                    warningMessage = "You have disconnected from the server, please retry...";
                    manager.push(new MenuState(manager,0));
                    System.out.println("Back to Menu failed?");
                }
                //add a new player
                playersInLobby.add(true);
                countOfPlayers++;
                updatePlayerListonServer();
                serverResponded = false;
                break;
            } else if (isSpectator) {
                break;
            }
        }
    }

    private void loadAllTextures() {

        bg = new Texture("menu/menubg.png");
        p1 = new Texture("menu/player1.png");
        p2 = new Texture("menu/player2.png");
        p3 = new Texture("menu/player3.png");
        p4 = new Texture("menu/player4.png");
        p1r = new Texture("menu/checkp1.png");
        p2r = new Texture("menu/checkp2.png");
        p3r = new Texture("menu/checkp3.png");
        p4r = new Texture("menu/checkp4.png");
        //mapobjects
        textureArray.add(new Texture("objects/border.png"));//0
        textureArray.add(new Texture("objects/dot.png"));
        textureArray.add(new Texture("objects/cherry.png"));
        textureArray.add(new Texture("objects/banana.png"));
        //ghosts
        textureArray.add(new Texture("objects/ghost1.png"));//4
        textureArray.add(new Texture("objects/ghost2.png"));
        textureArray.add(new Texture("objects/ghost3.png"));
        textureArray.add(new Texture("objects/ghost4.png"));
        //players
        //p1
        textureArray.add(new Texture("objects/pac1r.png"));//8
        textureArray.add(new Texture("objects/pac1d.png"));
        textureArray.add(new Texture("objects/pac1l.png"));
        textureArray.add(new Texture("objects/pac1u.png"));
        //p2
        textureArray.add(new Texture("objects/pac2r.png"));//12
        textureArray.add(new Texture("objects/pac2d.png"));
        textureArray.add(new Texture("objects/pac2l.png"));
        textureArray.add(new Texture("objects/pac2u.png"));
        //p3
        textureArray.add(new Texture("objects/pac3r.png"));//16
        textureArray.add(new Texture("objects/pac3d.png"));
        textureArray.add(new Texture("objects/pac3l.png"));
        textureArray.add(new Texture("objects/pac3u.png"));
        //p4
        textureArray.add(new Texture("objects/pac4r.png"));//20
        textureArray.add(new Texture("objects/pac4d.png"));
        textureArray.add(new Texture("objects/pac4l.png"));
        textureArray.add(new Texture("objects/pac4u.png"));

        textureArray.add(new Texture("objects/banana.png"));//24

    }

    private void updatePlayerListonServer() {
        socket.emit("createPlayerInLobby");
        Gdx.app.log("SocketIO","created Player " +(countOfPlayers));
    }

    private void getConnectedPlayers() {
        socket.emit("getPlayerCountLobby");
    }

    private void configSocketEvents() {
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if(!isSpectator) {
                    Gdx.app.log("SocketIO", "Connected as Player");
                } else {
                    Gdx.app.log("SocketIO", "Connected as Spectator");
                }
            }
        }).on("socketID", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                try {
                    String id = data.getString("id");
                    Gdx.app.log("SocketIO", "MyID:" + id);
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error getting ID");
                }
            }
        }).on("getPlayersLobby", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONArray objects = (JSONArray) args[0];
                Gdx.app.log("TAG", objects.toString());
                try {
                    for (int i = 0; i < objects.length(); i++) {
                        playersInLobby.add(true);
                        countOfPlayers++;
                        Gdx.app.log("SocketIO:","added player local: "+ (i+1) );
                    }
                    if(!isSpectator) {
                        myPlayerId = countOfPlayers  + 1;
                    }
                    serverResponded = true;
                } catch(Exception e) {
                    Gdx.app.log("SocketIO", "Error getting Players Array");
                }
            }
        }).on("newPlayerLobby", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                try {
                    String playerID = data.getString("id");
                    playersInLobby.add(true);
                    countOfPlayers++;
                    select.play(0.2f);
                    Gdx.app.log("SocketIO", "New Player Connect: " + playerID);
                    System.out.println(countOfPlayers);
                    serverResponded=true;
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error getting New Player ID");
                }
            }
        }).on("startGame", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Integer map = (Integer) args[0];
                try {
                    Gdx.app.log("SocketIO", "Start Game with Map: " + map);
                    manager.set(new PlayState(manager, map, isSpectator, false, countOfPlayers , socket, myPlayerId, textureArray, batch, status));
                } catch (Exception e) {
                    Gdx.app.log("SocketIO", "Error Starting Game with Map:" +map);
                }

            }
        });
    }
    private void connectSocket() {
        try{
            socket = IO.socket(PacManMain.SERVERIP);
            socket.connect();
        } catch(Exception e){
            System.out.print(e);
        }

    }

    @Override
    protected void handleinput(float dt) {
        if (map1Btn.isPressed()) {
            select.play(0.2f);
            System.out.println("start game with map 1");
            JSONObject data = new JSONObject();
            try {
                data.put("map", 1);
                data.put("id", myPlayerId);
                socket.emit("startGame", data);
            } catch(JSONException e) {
                Gdx.app.log("SOCKET.IO", "Error sending start map data");
            }
            try {
                Gdx.app.log("SocketIO", "Start Game with Map: " + 1);
                manager.set(new PlayState(manager, 1, isSpectator, true, countOfPlayers , socket, myPlayerId, textureArray, batch, status));
            } catch (Exception e) {
                Gdx.app.log("SocketIO", "Error Starting Game with Map:" +1);
            }

        } else if (map2Btn.isPressed()) {
            select.play(0.2f);
            System.out.println("start game with map 2");
            JSONObject data = new JSONObject();
            try {
                data.put("map", 2);
                data.put("id", myPlayerId);
                socket.emit("startGame", data);
            } catch(JSONException e) {
                Gdx.app.log("SOCKET.IO", "Error sending start map data");
            }
            try {
                Gdx.app.log("SocketIO", "Start Game with Map: " + 2);
                manager.set(new PlayState(manager, 2, isSpectator, true, countOfPlayers , socket, myPlayerId, textureArray, batch, status));
            } catch (Exception e) {
                Gdx.app.log("SocketIO", "Error Starting Game with Map:" +2);
            }

        }

    }

    @Override
    public void update(float dt) {
        handleinput(dt);
    }

    @Override
    public void render(SpriteBatch batch) {
        batch.begin();
        title.getData().setScale(2);
        status.draw(batch, warningMessage, 50, 940);
        title.setColor(Color.RED);
        title.draw(batch, "Create Game", PacManMain.WIDTH/2-200, PacManMain.HEIGHT-50);
        if (countOfPlayers<4) {
            status.draw(batch, "...waiting for players to join..", PacManMain.WIDTH / 2 - 150, PacManMain.HEIGHT - 90);
            if(isSpectator) {
                status.draw(batch, "You are Spectator", PacManMain.WIDTH / 2 - 150, PacManMain.HEIGHT - 120);
            }
            else {
                status.draw(batch, "You are Player "+ myPlayerId,PacManMain.WIDTH / 2 - 150, PacManMain.HEIGHT - 120);
            }
        } else if (isHost){
            status.draw(batch, "Game full! Choose Map to start the Game!", PacManMain.WIDTH / 2 - 150, PacManMain.HEIGHT - 100);
        } else {
            status.draw(batch, "Game full! Host has to start the Game!", PacManMain.WIDTH / 2 - 150, PacManMain.HEIGHT - 100);
        }
        if(isHost) {
            status.draw(batch, "Choose the level and start the Game!", PacManMain.WIDTH / 2 - 150, PacManMain.HEIGHT - 600);
        }

        status.draw(batch, "How to Highscore:", PacManMain.WIDTH / 2+80 , PacManMain.HEIGHT - 80);
        status.draw(batch, "Dot : 1p", PacManMain.WIDTH / 2+80 , PacManMain.HEIGHT - 100);
        status.draw(batch, "Banana : 10p", PacManMain.WIDTH / 2+80 , PacManMain.HEIGHT - 120);
        status.draw(batch, "Death : -5p", PacManMain.WIDTH / 2+80 , PacManMain.HEIGHT - 140);
        status.draw(batch, "Time : -0.2p/s", PacManMain.WIDTH / 2+80 , PacManMain.HEIGHT - 160);
        status.draw(batch,"Multiplayer Multiplikator:",PacManMain.WIDTH / 2+80 , PacManMain.HEIGHT - 180);
        status.draw(batch,"1P: 1.0x    2P: 1.75x",PacManMain.WIDTH / 2+80 , PacManMain.HEIGHT - 200);
        status.draw(batch,"3P: 2.5x    4P: 3.25x ",PacManMain.WIDTH / 2+80 , PacManMain.HEIGHT - 230);

        batch.draw(p1,PacManMain.WIDTH/2-200, PacManMain.HEIGHT-200);
        batch.draw(p2,PacManMain.WIDTH/2-200, PacManMain.HEIGHT-300);
        batch.draw(p3,PacManMain.WIDTH/2-200, PacManMain.HEIGHT-400);
        batch.draw(p4,PacManMain.WIDTH/2-200, PacManMain.HEIGHT-500);

        //draw checkmark for each player who connected
        if (countOfPlayers >0 && playersInLobby.get(0)) {
            batch.draw(p1r, PacManMain.WIDTH / 2 - 200 + p1.getWidth(), PacManMain.HEIGHT - 200);
        }
        if (countOfPlayers  >1 && playersInLobby.get(1)) {
            batch.draw(p2r, PacManMain.WIDTH / 2 - 200 + p2.getWidth(), PacManMain.HEIGHT - 300);
        }
        if (countOfPlayers  >2 && playersInLobby.get(2)) {
            batch.draw(p3r, PacManMain.WIDTH / 2 - 200 + p3.getWidth(), PacManMain.HEIGHT - 400);
        }
        if (countOfPlayers  >3 && playersInLobby.get(3)) {
            batch.draw(p4r, PacManMain.WIDTH / 2 - 200 + p4.getWidth(), PacManMain.HEIGHT - 500);
        }
        batch.end();
        //only host can start the game
        if (isHost) {
            stage.act(Gdx.graphics.getDeltaTime());
            stage.draw();
        }
    }

    @Override
    public void dispose() {

    }
}
