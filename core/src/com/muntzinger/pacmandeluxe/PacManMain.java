package com.muntzinger.pacmandeluxe;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.muntzinger.pacmandeluxe.states.GameStateManager;
import com.muntzinger.pacmandeluxe.states.MenuState;

public class PacManMain extends Game {
	public static final Integer WIDTH = 540;
	public static final Integer HEIGHT = 960;
	//server ip, change if necesarry
    public static final String SERVERIP = "http://192.168.1.103:8080";
    public static Preferences prefs;
	private GameStateManager manager;
	private SpriteBatch batch;
	public static Music music;

	@Override
	public void create () {
		prefs = Gdx.app.getPreferences("PacManDeluxeHS");
		batch = new SpriteBatch();
		music = Gdx.audio.newMusic(Gdx.files.internal("sound/throughspace.ogg"));
		music.setLooping(true);
		music.setVolume(0.1f);
		music.play();
		manager = new GameStateManager();
		manager.push(new MenuState(manager, prefs.getInteger("highscore",0)));
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		manager.update(Gdx.graphics.getDeltaTime());
		manager.render(batch);

	}
	
	@Override
	public void dispose () {
		batch.dispose();
	}
}
