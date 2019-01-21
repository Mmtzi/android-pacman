package com.muntzinger.pacmandeluxe.sprites;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.collision.BoundingBox;

/**
 * Created by yurai on 31.01.2018.
 */

public abstract class MapObject {
    protected Vector2 position;
    protected Texture texture;
    protected Rectangle rectangle;

    public Texture getTexture(){return texture;}
    public Vector2 getPosition(){return position;}
    public boolean catchingMapObject(Rectangle player) {
        return player.overlaps(rectangle);
    }
    public void dispose(){
        texture.dispose();
    }
}
