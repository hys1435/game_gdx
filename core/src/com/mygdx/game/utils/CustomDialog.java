package com.mygdx.game.utils;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.data.MultipleChoice;
import com.mygdx.game.entities.Enemy;
import com.mygdx.game.screens.Play;

/**
 * Created by Shuni on 3/24/17.
 *
 * CustomDialog is an abstract class and will never be initiated.
 */

public abstract class CustomDialog extends Dialog {

    public static final int LABEL_WIDTH = 400;

    private InputProcessor ip;
    private Play playScreen;
    private CustomDialog responseDialog;

    public CustomDialog(String title, Skin skin, CustomDialog responseDialog) {
        super(title, skin);
        this.ip = Gdx.input.getInputProcessor();
        this.responseDialog = responseDialog;

        //Shuni: To get the current screen, used to get enemies and freeze them
        //Can be used to freeze time/clock in the future
        //TODO: write interface freezable, enemy/player implements freezable
        Screen curScreen = ((Game) Gdx.app.getApplicationListener()).getScreen();
        if (curScreen instanceof Play) {
            this.playScreen = (Play) curScreen;
        }
    }

    /**
     * updates the content of this dialog. will eb called in the result method to
     * result content for response dialog
     * @param object
     */
    public abstract void renderContent(Object object);

    public CustomDialog getResponseDialog() {
        return responseDialog;
    }

    public void setResponseDialog(CustomDialog responseDialog) {
        this.responseDialog = responseDialog;
    }

    @Override public Dialog show(Stage stage) {
        // Set input processor to dialog
        playScreen.getPlayer().resetDirection();
        Gdx.input.setInputProcessor(stage);
        setTimeFreeze(true);
        return super.show(stage);
    }

    @Override protected void result(Object object) {
        // Reset input processor to screen
        Gdx.input.setInputProcessor(this.ip);
        setTimeFreeze(false);
        if (this.responseDialog != null) {
            if (object instanceof MultipleChoice) {
                responseDialog.renderContent(object);
                responseDialog.show(getStage());
            }
            // if the object is passed from a TextDialog
            if (object instanceof String[]) {
                String[] strs = (String[]) object;
                if (!(strs.length == 0)) {
                    responseDialog.renderContent(strs);
                    responseDialog.show(getStage());
                }
            }
        }
        remove();
    }

    public abstract Object getContent();

    protected void addLabel(String str, Skin skin) {
        Label label = new Label(str, skin);
        label.setWrap(true); // wrapping text to multiple lines
        label.setAlignment(Align.center);
        getContentTable().clearChildren();
        getContentTable().add(label).prefWidth(LABEL_WIDTH); // prefWidth is width of the actual text box
        getButtonTable().clearChildren();
    }

    private boolean checkScreen() {
        return this.playScreen != null;
    }

    private void setTimeFreeze(boolean freeze) {
        if (!checkScreen()) return;
        Array<Enemy> enemies = this.playScreen.getEnemies();
        for (Enemy e : enemies) {
            e.setFreeze(freeze);
        }
        this.playScreen.getGameStatsGroup().setFrozen(freeze);
    }

    @Override
    public Dialog button (Button button, Object object) {
        Dialog result = super.button(button, object);
        getButtonTable().row();
        return result;
    }
}

