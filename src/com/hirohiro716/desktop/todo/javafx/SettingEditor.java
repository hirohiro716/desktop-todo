package com.hirohiro716.desktop.todo.javafx;

import java.io.File;

import com.hirohiro716.ExceptionHelper;
import com.hirohiro716.InterfaceKeyInputRobotJapanese.ImeMode;
import com.hirohiro716.RegexHelper.RegexPattern;
import com.hirohiro716.RudeArray;
import com.hirohiro716.database.sqlite.SQLite.IsolationLevel;
import com.hirohiro716.desktop.todo.Database;
import com.hirohiro716.desktop.todo.Setting;
import com.hirohiro716.desktop.todo.Setting.Property;
import com.hirohiro716.file.FileHelper;
import com.hirohiro716.javafx.ImeHelper;
import com.hirohiro716.javafx.PaneNodeFinder;
import com.hirohiro716.javafx.control.LimitTextField;
import com.hirohiro716.javafx.data.AbstractEditor;
import com.hirohiro716.javafx.dialog.alert.InstantAlert;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;

/**
 * 設定を編集する画面クラス.
 * @author hiro
 */
public class SettingEditor extends AbstractEditor<Setting> {
    
    @FXML
    private AnchorPane paneRoot;
    
    @FXML
    private Button buttonFiler;
    
    @FXML
    private Button buttonSave;
    
    private RudeArray allSettings;

    @Override
    protected void editDataController() throws Exception {
        Database database = new Database();
        database.connect();
        Setting setting = new Setting(database);
        setting.edit();
        this.setDataController(setting);
        this.allSettings = setting.getEditingAllSettings();
    }

    @Override
    protected void beforeShowDoPreparation() throws Exception {
        SettingEditor editor = this;
        this.setFxml(this.getClass().getResource(this.getClass().getSimpleName() + ".fxml"));
        this.getStage().setResizable(false);
        this.getStage().setTitle(this.getDataController().getDescription());
        FormHelper.applyIcon(this.getStage());
        FormHelper.applyBugFix(this.getStage());
        // コントロールの初期化
        PaneNodeFinder finder = new PaneNodeFinder(this.paneRoot);
        for (Property property: Property.values()) {
            LimitTextField limitTextField;
            ComboBox<String> comboBox;
            switch (property) {
            case FILER:
                limitTextField = finder.findLimitTextField("#" + property.getPhysicalName());
                ImeHelper.apply(limitTextField, ImeMode.OFF);
                this.buttonFiler.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        FileChooser chooser = new FileChooser();
                        chooser.setTitle("ファイラーの指定");
                        File file = chooser.showOpenDialog(editor.getStage());
                        if (file != null) {
                            limitTextField.setText(FileHelper.generateOptimizedPathFromURI(file.toURI()));
                        }
                    }
                });
                limitTextField.setText(this.allSettings.getString(property.getPhysicalName()));
                break;
            case FONT_STYLE:
                comboBox = finder.findComboBox("#" + property.getPhysicalName());
                comboBox.getItems().addAll(Font.getFamilies());
                comboBox.setValue(this.allSettings.getString(property.getPhysicalName()));
                break;
            case FONT_SIZE:
                limitTextField = finder.findLimitTextField("#" + property.getPhysicalName());
                limitTextField.addPermitRegex(RegexPattern.INTEGER_NARROW_ONLY.getPattern(), false);
                ImeHelper.apply(limitTextField, ImeMode.OFF);
                limitTextField.setText(this.allSettings.getString(property.getPhysicalName()));
                break;
            case BACKGROUND:
            case TEXT_FILL:
            case SELECTED_ROW_BACKGROUND:
                limitTextField = finder.findLimitTextField("#" + property.getPhysicalName());
                ImeHelper.apply(limitTextField, ImeMode.OFF);
                limitTextField.setText(this.allSettings.getString(property.getPhysicalName()));
                break;
            case COLUMN_WIDTH_RATE_ITEM_COUNT:
            case COLUMN_WIDTH_RATE_DIRECTORY:
            case COLUMN_WIDTH_RATE_DESCRIPTION:
            case COLUMN_WIDTH_RATE_DELETE:
                limitTextField = finder.findLimitTextField("#" + property.getPhysicalName());
                limitTextField.addPermitRegex(RegexPattern.DECIMAL.getPattern(), false);
                ImeHelper.apply(limitTextField, ImeMode.OFF);
                limitTextField.setText(this.allSettings.getString(property.getPhysicalName()));
                break;
            }
        }
        // 保存
        this.buttonSave.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                editor.importDataFromForm();
            }
        });
    }
    
    @Override
    protected void importDataFromForm() {
        PaneNodeFinder finder = new PaneNodeFinder(this.paneRoot);
        for (Property property: Property.values()) {
            LimitTextField limitTextField;
            ComboBox<String> comboBox;
            switch (property) {
            case FILER:
            case FONT_SIZE:
            case BACKGROUND:
            case TEXT_FILL:
            case SELECTED_ROW_BACKGROUND:
            case COLUMN_WIDTH_RATE_ITEM_COUNT:
            case COLUMN_WIDTH_RATE_DIRECTORY:
            case COLUMN_WIDTH_RATE_DESCRIPTION:
            case COLUMN_WIDTH_RATE_DELETE:
                limitTextField = finder.findLimitTextField("#" + property.getPhysicalName());
                this.allSettings.put(property.getPhysicalName(), limitTextField.getText());
                break;
            case FONT_STYLE:
                comboBox = finder.findComboBox("#" + property.getPhysicalName());
                this.allSettings.put(property.getPhysicalName(), comboBox.getValue());
                break;
            }
        }
        this.getDataController().setEditingAllSettings(this.allSettings);
        try {
            this.getDataController().getDatabase().begin(IsolationLevel.EXCLUSIVE);
            this.getDataController().validate();
            this.getDataController().normalize();
            this.getDataController().update();
            this.getDataController().getDatabase().commit();
            this.close();
        } catch (Exception exception) {
            InstantAlert.show(this.paneRoot, ExceptionHelper.createDetailMessage("情報の保存に失敗しました。", exception), Pos.CENTER, 3000);
        }
    }

    @Override
    protected void beforeCloseDoPreparation() throws Exception {
    }

}
