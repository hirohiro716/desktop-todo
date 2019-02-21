package com.hirohiro716.desktop.todo.javafx;

import javafx.application.Application;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * TODOアプリケーションの開始クラス.
 * @author hiro
 */
public class Run extends Application {
    
    /**
     * アプリケーションの開始.
     * @param args
     */
    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        if (Screen.getScreens().size() == 0) {
            return;
        }
        ToDoEditor editor = new ToDoEditor();
        editor.show();
    }

}
