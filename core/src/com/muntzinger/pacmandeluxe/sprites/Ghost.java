package com.muntzinger.pacmandeluxe.sprites;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by yurai on 03.02.2018.
 */

public class Ghost {
    private Texture texture;
    private Vector2 actualPosition;
    private Rectangle rectangle;
    private int ghostNumber;
    private int previousDirection;



    public Ghost(int id, Texture ptexture, int xstart, int ystart){
        ghostNumber = id;
        texture = ptexture;
        actualPosition = new Vector2(xstart,ystart);
        rectangle = new Rectangle(actualPosition.x, actualPosition.y, texture.getWidth(),texture.getHeight());
        previousDirection = 0;
    }
    public int getPreviousDirection() {
        return previousDirection;
    }

    public void setPreviousDirection(int previousDirection) {
        this.previousDirection = previousDirection;
    }
    public int getGhostNumber() {
        return ghostNumber;
    }

    public Rectangle getRectangle() {
        return rectangle;
    }

    public void setRectangle(Rectangle rectangle) {
        this.rectangle = rectangle;
    }

    public Texture getTexture() {
        return texture;
    }

    public Vector2 getActualPosition() {
        return actualPosition;
    }

    public void setActualPosition(Vector2 actualPosition) {
        this.actualPosition = actualPosition;
    }

}
