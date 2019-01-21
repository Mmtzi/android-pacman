package com.muntzinger.pacmandeluxe.sprites;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import javax.xml.soap.Text;

/**
 * Created by yurai on 31.01.2018.
 */

public class Dot extends MapObject {
    public Dot(Vector2 position, Texture texture) {
        this.texture = texture;
        this.position = position;
        this.rectangle = new Rectangle(position.x,position.y,this.texture.getWidth(),this.texture.getHeight());
    }
}
