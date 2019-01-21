package com.muntzinger.pacmandeluxe.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.muntzinger.pacmandeluxe.PacManMain;
import com.muntzinger.pacmandeluxe.sprites.Pacman;

import javax.xml.soap.Text;

/**
 * Created by yurai on 05.02.2018.
 */

public class PostGameState extends State {

    private Texture bg;
    private Stage stage;
    private Texture p1;
    private Texture p2;
    private Texture p3;
    private Texture p4;
    private Texture title;
    private Texture backBtnTexture;
    private TextureRegion backBtnRegion;
    private TextureRegionDrawable backBtnRegionDrawable;
    private ImageButton backBtn;
    private BitmapFont score;
    private Array<Pacman> allPacmans;
    private Float timer;
    private SpriteBatch batch;
    private double endTime;
    private int highscore;
    private Sound victory;


    public PostGameState(GameStateManager manager, Array<Pacman> allPacmansp, float timerp) {
        super(manager);
        victory = Gdx.audio.newSound(Gdx.files.internal("sound/victory.ogg"));
        batch = new SpriteBatch();
        timer = timerp;
        allPacmans = allPacmansp;
        endTime = (double)Math.round(100*(timer/60))/100;
        bg = new Texture("menu/menubg.png");
        p1 = new Texture("menu/player1.png");
        p2 = new Texture("menu/player2.png");
        p3 = new Texture("menu/player3.png");
        p4 = new Texture("menu/player4.png");
        title = new Texture("menu/title.png");
        score = new BitmapFont();
        backBtnTexture = new Texture("menu/tomenu.png");
        backBtnRegion = new TextureRegion(backBtnTexture);
        backBtnRegionDrawable = new TextureRegionDrawable(backBtnRegion);
        backBtn = new ImageButton(backBtnRegionDrawable); //Set the button up
        backBtn.setPosition(PacManMain.WIDTH / 4 - backBtn.getWidth() / 2, PacManMain.HEIGHT - 900);
        //because everybody got disconnected from the server after finishing the playstate everybody has to calculate the socres of each other
        calcScore();
        PacManMain.music.pause();
        victory.play(0.2f);
        stage = new Stage(new ScreenViewport());
        stage.addActor(backBtn);
        Gdx.input.setInputProcessor(stage);
    }

    private void calcScore() {
        for(int i=0; i<allPacmans.size; i++) {
            allPacmans.get(i).calcScore(timer, allPacmans.size);
            highscore = allPacmans.get(i).getScore();
            if (allPacmans.size > i+1) {
                highscore = Math.max(allPacmans.get(i).getScore(), allPacmans.get(i + 1).getScore());
            }
        }
    }

    @Override
    protected void handleinput(float dt) {
        if(backBtn.isPressed()){
            System.out.print("Back to Menu Button pressed");
            PacManMain.music.play();
            //if a new highscore was achieved the local highscore in the menu state will be updated
            manager.set(new MenuState(manager, highscore));
        }
    }

    @Override
    public void update(float dt) {
        handleinput(dt);
    }

    @Override
    public void render(SpriteBatch batch) {
        batch.begin();
        batch.draw(title, 20, 680);
        score.getData().setScale(2);
        score.setColor(Color.WHITE);
        score.draw(batch, "Game finished in "+ endTime +" min", PacManMain.WIDTH / 2 - 150, PacManMain.HEIGHT - 300);
        //print Scores
        for (int i = 0; i < allPacmans.size; i++) {
            if (allPacmans.get(i).getMyPlayerId()==1) {
                batch.draw(p1, PacManMain.WIDTH / 2 - 200, PacManMain.HEIGHT - 400);
                score.draw(batch, ""+ allPacmans.get(i).getScore() , PacManMain.WIDTH / 2 +100, PacManMain.HEIGHT - (350));
            }
            if (allPacmans.get(i).getMyPlayerId()==2) {
                batch.draw(p2, PacManMain.WIDTH / 2 - 200, PacManMain.HEIGHT - 500);
                score.draw(batch, ""+ allPacmans.get(i).getScore() , PacManMain.WIDTH / 2 +100, PacManMain.HEIGHT - (450));
            }
            if (allPacmans.get(i).getMyPlayerId()==3) {
                batch.draw(p3, PacManMain.WIDTH / 2 - 200, PacManMain.HEIGHT - 600);
                score.draw(batch, ""+ allPacmans.get(i).getScore() , PacManMain.WIDTH / 2 +100, PacManMain.HEIGHT - (550));
            }
            if (allPacmans.get(i).getMyPlayerId()==4) {
                batch.draw(p4, PacManMain.WIDTH / 2 - 200, PacManMain.HEIGHT - 700);
                score.draw(batch, ""+ allPacmans.get(i).getScore() , PacManMain.WIDTH / 2 +100, PacManMain.HEIGHT - (650));
            }
        }
        batch.end();
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    @Override
    public void dispose() {
        bg.dispose();
        p1.dispose();
        p2.dispose();
        p3.dispose();
        p4.dispose();
        title.dispose();
    }
}
