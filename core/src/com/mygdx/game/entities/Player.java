package com.mygdx.game.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.data.Direction;
import com.mygdx.game.data.MultipleChoice;
import com.mygdx.game.data.QuestionText;
import com.mygdx.game.screens.Play;
import com.mygdx.game.utils.CustomDialog;
import com.mygdx.game.utils.QuestionDialog;
import com.mygdx.game.utils.TextDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Shuni on 2/25/17.
 *
 * TODO: game stops when message pops up
 */
public class Player extends Sprite {

    public static final float SPEED = 10f;
    public static int health = 3;
    public static final int TOTALHEALTH = 3;

    private List<Direction> movingDirs;
    //private Direction currentDir;

    private Stage stage;//?
    private Array<Rectangle> doorRects;
    private Array<CustomDialog> questions;
    private Array<Rectangle> collisionRects;
    private Map<Rectangle, CustomDialog> spots;


    public ArrayList<Explosion> explosions;

    public Player(Texture texture, Stage stage) {
        super(texture);
        this.movingDirs = new ArrayList<>();
        //this.currentDir = Direction.IDLE;
        this.stage = stage;
        //TODO: Refactor!
        explosions = new ArrayList<>();
    }

    public void addNewDirection(Direction dir) {
        this.movingDirs.add(dir);
        //this.currentDir = dir;
    }

    public void removeDirection(Direction dir) {

        this.movingDirs.remove(dir);

//        if (this.movingDirs.isEmpty()) {
//            this.currentDir = Direction.IDLE;
//        } else {
//            this.currentDir = this.movingDirs.get(this.movingDirs.size() - 1);
//        }
    }

    private void resetDirection() {
        this.movingDirs.clear();
        //this.currentDir = Direction.IDLE;
    }



    //TODO: refactor
    public void makePlayerMove(){

        float oldXPos = getX();
        float oldYPos = getY();

        for (Direction dirInCurDir : movingDirs) {
            makeMove(dirInCurDir, 1.0f);
        }

        if (isOverlappedArray(collisionRects)) {
            popUpMessage();
            setPosition(oldXPos, oldYPos);
        }
    }

    public void makeMove(Direction direction, float scale) {
        switch (direction) {
            case UP:
                translateY(SPEED * scale);
                break;
            case DOWN:
                translateY(-SPEED * scale);
                break;
            case LEFT:
                translateX(-SPEED * scale);
                break;
            case RIGHT:
                translateX(SPEED * scale);
                break;
        }
    }


    public boolean isOverlappedArray(Array<Rectangle> rects) {
        for (Rectangle rect : rects) {
            if (isOverlapped(rect)){
                return true;
            }
        }
        return false;
    }

    public boolean isOverlapped(Rectangle rec2) {
        float p1x = getX();
        float p1y = getY() + getHeight();
        float p2x = getX() + getWidth();
        float p2y = getY();

        float p3x = rec2.getX();
        float p3y = rec2.getY() + rec2.getHeight();
        float p4x = rec2.getX() + rec2.getWidth();
        float p4y = rec2.getY();
        return (!((p2x < p3x) || (p1y < p4y) || (p1x > p4x) || (p2y > p3y)));
    }

    private Array<Rectangle> getExistingDoors() {
        Array<Rectangle> existingDoors = new Array<>();
        for (CustomDialog questionDialog : questions) {
            if (questionDialog instanceof QuestionDialog) {
                QuestionDialog question = (QuestionDialog) questionDialog;
                if (question.isAnswered()) {
                    existingDoors.add(Chapter.getKeyByValue(spots, question));
                }
            }
        }
        return existingDoors;
    }

    private Rectangle nextDoor(Array<Rectangle> existingDoors) {
        if (isFinished(existingDoors)) {
            return doorRects.get(existingDoors.size - 1);
        }
        return (doorRects.get(existingDoors.size));
    }

    private boolean isFinished(Array<Rectangle> existingDoors) {
        return (existingDoors.size == doorRects.size);
    }

    // Called when hitting on the wall
    private void popUpMessage() {
        if (doorRects.random() == null || questions.random() == null) { // check if the array is empty
            return;
        }
        Array<Rectangle> existingDoors = getExistingDoors();
        Rectangle newDoor = nextDoor(existingDoors);

        // Visiting answered questions
        for (Rectangle rect : existingDoors) {
            if (isOverlapped(rect)) {
                resetDirection();
                CustomDialog dialogBox = spots.get(rect);
                dialogBox.getResponseDialog().show(this.stage);
            }
        }

        // Visiting a new spot
        if (isOverlapped(newDoor)) {
            resetDirection();
            CustomDialog dialogBox = spots.get(newDoor);
            if (isFinished(existingDoors)) {
                dialogBox.getResponseDialog().show(this.stage);
            }
            else {
                dialogBox.show(this.stage);
            }
        }
    }

//
//    private boolean checkAnswer(QuestionDialog dialog) {
//        if (dialog.isError()) {
//            dialog.setError(false);
//            return true;
//        }
//        return false;
//    }

    public Array<CustomDialog> generateQuestions(Skin skin) {

        Array<CustomDialog> questions = new Array<>();

        QuestionText qt = new QuestionText();
        qt.initQuestions();

        for (int i = 0; i < qt.getNumQuestions(); i++){
            CustomDialog td = new TextDialog("TEXT", skin, null);
            CustomDialog responseDialog = new TextDialog("ANSWER", skin, null);
            CustomDialog qd = new QuestionDialog("CLUE", skin, responseDialog);
            if (qt.getNthQuestion(i) instanceof MultipleChoice) {
                qd.renderContent(qt.getNthQuestion(i));
                questions.add(qd);
            }
            else if (qt.getNthQuestion(i) instanceof String) {
                td.renderContent(qt.getNthQuestion(i));
                questions.add(td);
            }
        }
        return questions;
    }

    public void hitEnemy(Array<Enemy> enemies) {
        for (Enemy enemy : enemies) {
            if(isOverlapped(enemy.getBoundingRectangle())) {
                health--;
                enemies.removeValue(enemy, true);
                explosions.add(new Explosion(enemy.getX(), enemy.getY()));
                Play.punch.play();
            }
        }
    }

    public boolean isAlive(int health) {
        return (health > 0);
    }

    public void setCollisionRects(Array<Rectangle> collisionRects) {
        this.collisionRects = collisionRects;
    }

    public void setDoorRects(Array<Rectangle> doorRects) {
        this.doorRects = doorRects;
    }

    public void setQuestions(Array<CustomDialog> questions) {
        this.questions = questions;
    }

    public void setSpots(Map<Rectangle, CustomDialog> spots) {
        this.spots = spots;
    }
}
