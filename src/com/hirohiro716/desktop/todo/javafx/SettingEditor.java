package com.hirohiro716.desktop.todo.javafx;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import com.hirohiro716.InterfaceKeyInputRobotJapanese.ImeMode;
import com.hirohiro716.RegexHelper.RegexPattern;
import com.hirohiro716.RudeArray;
import com.hirohiro716.database.ValidationException;
import com.hirohiro716.database.sqlite.SQLite.IsolationLevel;
import com.hirohiro716.desktop.todo.Database;
import com.hirohiro716.desktop.todo.Setting;
import com.hirohiro716.desktop.todo.Setting.Property;
import com.hirohiro716.file.FileHelper;
import com.hirohiro716.javafx.ImeHelper;
import com.hirohiro716.javafx.PaneNodeFinder;
import com.hirohiro716.javafx.control.LimitTextField;
import com.hirohiro716.javafx.data.AbstractEditor;
import com.hirohiro716.javafx.dialog.DialogResult;
import com.hirohiro716.javafx.dialog.InterfaceDialog.CloseEventHandler;
import com.hirohiro716.javafx.dialog.alert.AlertPane;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
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
    protected void editDataController() throws SQLException {
        Database database = new Database();
        database.connect();
        Setting setting = new Setting(database);
        setting.edit();
        this.setDataController(setting);
        this.allSettings = setting.getEditingAllSettings();
    }

    @Override
    protected void beforeShowDoPreparation() throws IOException {
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
        SettingEditor editor = this;
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
        } catch (SQLException exception) {
            AlertPane.show(ERROR_DIALOG_TITLE_SAVE, exception.getMessage(), editor.paneRoot, new CloseEventHandler<DialogResult>() {
                @Override
                public void handle(DialogResult resultValue) {
                    try {
                        editor.editDataController();
                    } catch (SQLException exception) {
                        editor.close();
                    }
                }
            });
        } catch (ValidationException exception) {
            AlertPane.show(ERROR_DIALOG_TITLE_VALIDATION, exception.getMessage(), editor.paneRoot, new CloseEventHandler<DialogResult>() {
                @Override
                public void handle(DialogResult resultValue) {
                    Node errorNode = editor.paneRoot.lookup("#" + exception.getCauseColumn().getPhysicalName());
                    if (errorNode != null) {
                        errorNode.requestFocus();
                    }
                    try {
                        editor.getDataController().getDatabase().rollback();
                    } catch (SQLException exception) {
                        editor.close();
                    }
                }
            });
        }
    }

    @Override
    protected void beforeCloseDoPreparation() {
        this.getDataController().getDatabase().close();
    }

}
