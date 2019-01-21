package com.muntzinger.pacmandeluxe.states;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.muntzinger.pacmandeluxe.PacManMain;

/**
 * Created by Marci on 20.12.2016.
 */

public abstract class State {
    private FitViewport viewPort;
    protected OrthographicCamera camera;
    protected GameStateManager manager;
    protected Vector3 mouse;

    public State(GameStateManager manager) {
        this.manager = manager;
        camera = new OrthographicCamera();
        mouse = new Vector3();
        this.viewPort = new FitViewport(PacManMain.WIDTH, PacManMain.HEIGHT, camera);
        camera.position.set(viewPort.getWorldWidth(), viewPort.getWorldHeight(), 0);
    }

    protected abstract void handleinput(float dt);
    public abstract void update (float dt);
    public abstract void render(SpriteBatch batch);
    public abstract void dispose();
}
