package com.muntzinger.pacmandeluxe.sprites;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by yurai on 06.02.2018.
 */

public class Banana extends MapObject {
    public Banana(Vector2 position, Texture texture) {
        this.texture = texture;
        this.position = position;
        this.rectangle = new Rectangle(position.x, position.y, this.texture.getWidth(), this.texture.getHeight());
    }
}
