package com.muntzinger.pacmandeluxe.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.muntzinger.pacmandeluxe.PacManMain;

import static com.muntzinger.pacmandeluxe.PacManMain.prefs;

/**
 * Created by yurai on 23.01.2018.
 */

public class MenuState extends State {
    //Menu Buttons
    private final Texture hostGameBtnTexture;
    private final TextureRegion hostGameBtnRegion;
    private final TextureRegionDrawable hostGameBtnRegionDrawable;
    private final ImageButton hostGameBtn;

    private final Texture joinGameBtnTexture;
    private final TextureRegion joinGameBtnRegion;
    private final TextureRegionDrawable joinGameBtnRegionDrawable;
    private final ImageButton joinGameBtn;

    private final Texture spectateGameBtnTexture;
    private final TextureRegion spectateGameBtnRegion;
    private final TextureRegionDrawable spectateGameBtnRegionDrawable;
    private final ImageButton spectateGameBtn;


    private Texture title;

    private static int highscore;

    private Texture bg;
    private BitmapFont font;
    private Stage stage;

    private final Sound select;

    public MenuState(GameStateManager manager, int highscore) {
        super(manager);
        select = Gdx.audio.newSound(Gdx.files.internal("sound/select.ogg"));
        //safe highscores local in preferencses
        if (getHighscore() <= highscore) {
            setHighscore(highscore);
            prefs.putInteger("highscore", highscore);
            prefs.flush();
        }

        bg = new Texture("menu/menubg.png");
        title = new Texture("menu/title.png");
        font = new BitmapFont();

        camera.setToOrtho(false, PacManMain.WIDTH, PacManMain.HEIGHT);
        hostGameBtnTexture = new Texture("menu/hostbtn.png");
        hostGameBtnRegion = new TextureRegion(hostGameBtnTexture);
        hostGameBtnRegionDrawable = new TextureRegionDrawable(hostGameBtnRegion);
        hostGameBtn = new ImageButton(hostGameBtnRegionDrawable);
        hostGameBtn.setPosition(PacManMain.WIDTH / 2 - hostGameBtn.getWidth() / 2 - 100, PacManMain.HEIGHT - 370);

        joinGameBtnTexture = new Texture("menu/joinbtn.png");
        joinGameBtnRegion = new TextureRegion(joinGameBtnTexture);
        joinGameBtnRegionDrawable = new TextureRegionDrawable(joinGameBtnRegion);
        joinGameBtn = new ImageButton(joinGameBtnRegionDrawable);
        joinGameBtn.setPosition(PacManMain.WIDTH / 2 - joinGameBtn.getWidth() / 2 - 100, PacManMain.HEIGHT - 495);


        spectateGameBtnTexture = new Texture("menu/spectatebtn.png");
        spectateGameBtnRegion = new TextureRegion(spectateGameBtnTexture);
        spectateGameBtnRegionDrawable = new TextureRegionDrawable(spectateGameBtnRegion);
        spectateGameBtn = new ImageButton(spectateGameBtnRegionDrawable);
        spectateGameBtn.setPosition(PacManMain.WIDTH / 2 - joinGameBtn.getWidth() / 2 - 100, PacManMain.HEIGHT - 620);

        stage = new Stage(new ScreenViewport());
        stage.addActor(joinGameBtn); //Add buttons to stage
        stage.addActor(hostGameBtn);
        stage.addActor(spectateGameBtn);
        Gdx.input.setInputProcessor(stage); //Start taking input from the ui
    }

    @Override
    public void handleinput(float dt) {
        if (joinGameBtn.isPressed()) {
            System.out.print("Join Game Button pressed");
            manager.set(new LobbyState(manager, false, false));
            select.play(0.1f);
        }
        if (hostGameBtn.isPressed()) {
            System.out.print("Create Game Button pressed");
            manager.set(new LobbyState(manager, true, false));
            select.play(0.1f);
        }
        if (spectateGameBtn.isPressed()) {
            System.out.print("Spectate Game Button pressed");
            manager.set(new LobbyState(manager, false, true));
            select.play(0.1f);
        }
    }

    @Override
    public void update(float dt) {
        handleinput(dt);

    }
    private void setHighscore(int highscore) {
        MenuState.highscore = highscore;
    }


    @Override
    public void render(SpriteBatch batch) {

        batch.begin();
        batch.setProjectionMatrix(camera.combined);
        batch.draw(title, 20, 680);
        font.setColor(Color.YELLOW);
        font.draw(batch, "Highscore: " + highscore, 40, 40);
        batch.end();
        stage.act(Gdx.graphics.getDeltaTime()); //Perform ui logic
        stage.draw(); //Draw the ui
    }

    private int getHighscore() {
        return highscore;
    }


    @Override
    public void dispose() {
        bg.dispose();
        joinGameBtnTexture.dispose();
        hostGameBtnTexture.dispose();
        spectateGameBtnTexture.dispose();
    }
}
