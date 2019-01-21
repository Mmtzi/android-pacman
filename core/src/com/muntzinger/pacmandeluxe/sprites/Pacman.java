package com.muntzinger.pacmandeluxe.sprites;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by yurai on 31.01.2018.
 */

public class Pacman {
    private Texture texture;
    private Vector2 previousPosition;
    private Vector2 actualPosition;
    private Rectangle rectangle;
    private int points;
    private int myPlayerId;
    private int deaths;
    private float invTimer;
    private int score;


    public Pacman(int myPlayerIdp, Texture texturep){
        myPlayerId = myPlayerIdp;
        texture = texturep;
        previousPosition = new Vector2(210+(myPlayerId-1)*30,840);
        actualPosition = new Vector2(210+(myPlayerId-1)*30,840);
        rectangle = new Rectangle(actualPosition.x, actualPosition.y, texture.getWidth(),texture.getHeight());
        points = 0;
        deaths = 0;
        invTimer =0;
        score = 0;
    }

    public boolean hasMoved(){
        if (previousPosition.x != actualPosition.x || previousPosition.y != actualPosition.y){
            previousPosition.x = actualPosition.x;
            previousPosition.y = actualPosition.y;
            rectangle.setPosition(actualPosition.x,actualPosition.y);
            return true;
        }
        return false;
    }

    public int getMyPlayerId() {
        return myPlayerId;
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

    public Vector2 getPreviousPosition() {
        return previousPosition;
    }

    public void setPreviousPosition(Vector2 previousPosition) {
        this.previousPosition = previousPosition;
    }

    public Vector2 getActualPosition() {
        return actualPosition;
    }

    public void setActualPosition(Vector2 actualPosition) {
        this.actualPosition = actualPosition;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }


    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }
    public int getDeaths() {
        return deaths;
    }

    public float getInvTimer() {
        return invTimer;
    }

    public void setInvTimer(float invTimer) {
        this.invTimer = invTimer;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void calcScore(Float timer, Integer countOfPlayers) {
        setScore((int) (Math.round(points-deaths*5-Math.round(Math.round(timer)/5))*(0.25+0.75*countOfPlayers)));
        System.out.println(myPlayerId+" points: "+ points +" deaths: "+deaths+ " score: "+score + "Multiplikator");
    }
}
