package com.hirohiro716.desktop.todo.javafx;

import com.hirohiro716.javafx.GenerationalRunLater;

import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * フォームのstatic関数.
 * @author hiro
 */
public class FormHelper {
    
    /**
     * Stageに対して規定のアイコンを設定する.
     * @param stage
     */
    public static void applyIcon(Stage stage) {
        stage.getIcons().add(new Image(FormHelper.class.getResourceAsStream("icon128.png")));
        stage.getIcons().add(new Image(FormHelper.class.getResourceAsStream("icon64.png")));
        stage.getIcons().add(new Image(FormHelper.class.getResourceAsStream("icon48.png")));
        stage.getIcons().add(new Image(FormHelper.class.getResourceAsStream("icon32.png")));
        stage.getIcons().add(new Image(FormHelper.class.getResourceAsStream("icon16.png")));
    }
    
    /**
     * Stageに対してJavaFX既知のバグ対策をする.
     * @param stage
     */
    public static void applyBugFix(Stage stage) {
        GenerationalRunLater.runLater(500, new Runnable() {
            @Override
            public void run() {
                stage.requestFocus();
            }
        });
    }
    
}
