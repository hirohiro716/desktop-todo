package com.hirohiro716.desktop.todo.javafx;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;

import com.hirohiro716.ExceptionHelper;
import com.hirohiro716.RudeArray;
import com.hirohiro716.StringConverter;
import com.hirohiro716.database.sqlite.SQLite.IsolationLevel;
import com.hirohiro716.desktop.todo.Database;
import com.hirohiro716.desktop.todo.Setting;
import com.hirohiro716.desktop.todo.Setting.Property;
import com.hirohiro716.desktop.todo.ToDo;
import com.hirohiro716.desktop.todo.ToDo.Column;
import com.hirohiro716.file.FileHelper;
import com.hirohiro716.javafx.CSSHelper;
import com.hirohiro716.javafx.control.ImeOffButton;
import com.hirohiro716.javafx.control.table.EditableTable;
import com.hirohiro716.javafx.control.table.EditableTable.FixControlFactory;
import com.hirohiro716.javafx.control.table.RudeArrayTable;
import com.hirohiro716.javafx.data.AbstractEditor;
import com.hirohiro716.javafx.dialog.DialogResult;
import com.hirohiro716.javafx.dialog.InterfaceDialog.CloseEventHandler;
import com.hirohiro716.javafx.dialog.alert.InstantAlert;
import com.hirohiro716.javafx.dialog.confirm.ConfirmPane;
import com.hirohiro716.javafx.dialog.sort.SortPaneDialog;
import com.hirohiro716.javafx.dialog.text.LimitTextAreaPaneDialog;
import com.hirohiro716.validate.StringValidator;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Screen;
import javafx.stage.StageStyle;

/**
 * TODOを編集する画面クラス.
 * @author hiro
 */
public class ToDoEditor extends AbstractEditor<ToDo> {
    
    @FXML
    private AnchorPane paneRoot;
    
    @FXML
    private VBox vboxRoot;
    
    @FXML
    private RudeArrayTable rudeArrayTable;
    
    @FXML
    private Button buttonAdd;
    
    @FXML
    private Button buttonSort;
    
    @FXML
    private Button buttonSetting;
    
    @FXML
    private Button buttonClose;
    
    @Override
    protected void editDataController() throws Exception {
        Database database = new Database();
        database.connect();
        ToDo todo = new ToDo(database);
        todo.edit();
        this.setDataController(todo);
    }

    /**
     * 最新の設定に更新する.
     * @throws SQLException 
     */
    private void updateContentFromSetting() throws SQLException {
        Screen screen = Screen.getPrimary();
        double tableWidth = screen.getVisualBounds().getWidth() - 200;
        try (Database database = new Database()) {
            database.connect();
            try (Setting setting = new Setting(database)) {
                RudeArray allSettings = setting.fetchAllSettings();
                // ファイラー
                this.filer = allSettings.getString(Property.FILER.getPhysicalName());
                // 背景
                String css = this.paneRoot.getStyle();
                css = CSSHelper.updateStyleValue(css, "-fx-background-color", allSettings.getString(Property.BACKGROUND.getPhysicalName()));
                this.paneRoot.setStyle(css);
                // フォント
                css = this.vboxRoot.getStyle();
                css = CSSHelper.updateStyleValue(css, "-fx-font-size", allSettings.getString(Property.FONT_SIZE.getPhysicalName()));
                css = CSSHelper.updateStyleValue(css, "-fx-font-family", allSettings.getString(Property.FONT_STYLE.getPhysicalName()));
                this.vboxRoot.setStyle(css);
                // テキスト色
                String textFill = allSettings.getString(Property.TEXT_FILL.getPhysicalName());
                for (Node node: this.paneRoot.lookupAll("Label")) {
                    node.setStyle(CSSHelper.updateStyleValue(node.getStyle(), "-fx-text-fill", textFill));
                }
                // 選択行の背景色
                this.rudeArrayTable.setSelectedRowColor(allSettings.getString(Property.SELECTED_ROW_BACKGROUND.getPhysicalName()));
                // テーブル
                double directoryWidth = tableWidth * StringConverter.stringToDouble(allSettings.getString(Property.COLUMN_WIDTH_RATE_DIRECTORY.getPhysicalName()));
                double itemCountWidth = tableWidth * StringConverter.stringToDouble(allSettings.getString(Property.COLUMN_WIDTH_RATE_ITEM_COUNT.getPhysicalName()));
                double descriptionWidth = tableWidth * StringConverter.stringToDouble(allSettings.getString(Property.COLUMN_WIDTH_RATE_DESCRIPTION.getPhysicalName()));
                double deleteWidth = tableWidth * StringConverter.stringToDouble(allSettings.getString(Property.COLUMN_WIDTH_RATE_DELETE.getPhysicalName()));
                this.rudeArrayTable.getHeaderLabel(Column.DIRECTORY.getPhysicalName()).setPrefWidth(directoryWidth);
                this.rudeArrayTable.getHeaderLabel(COLUMN_ID_OF_ITEM_COUNT).setPrefWidth(itemCountWidth);
                this.rudeArrayTable.getHeaderLabel(Column.DESCRIPTION.getPhysicalName()).setPrefWidth(descriptionWidth);
                this.rudeArrayTable.getHeaderLabel(COLUMN_ID_OF_DELETE).setPrefWidth(deleteWidth);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }
    
    private String filer = null;
    
    @Override
    protected void beforeShowDoPreparation() throws Exception {
        ToDoEditor editor = this;
        this.setFxml(this.getClass().getResource(this.getClass().getSimpleName() + ".fxml"));
        this.getStage().initStyle(StageStyle.TRANSPARENT);
        this.getStage().setResizable(false);
        this.getStage().setTitle(this.getDataController().getDescription());
        Screen screen = Screen.getPrimary();
        this.getStage().setWidth(screen.getVisualBounds().getWidth());
        this.getStage().setHeight(screen.getVisualBounds().getHeight());
        this.vboxRoot.setMaxSize(screen.getVisualBounds().getWidth() - 200, screen.getVisualBounds().getHeight() - 200);
        this.getStage().getScene().setFill(null);
        FormHelper.applyIcon(this.getStage());
        FormHelper.applyBugFix(this.getStage());
        // フォーカス喪失時に透過する＋フォーカス取得時に最新に更新
        AnchorPane pane = (AnchorPane) this.getStage().getScene().getRoot();
        this.getStage().focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    pane.setOpacity(0.95);
                    try {
                        editor.updateRudeArrayTable();
                    } catch (SQLException exception) {
                        InstantAlert.show(editor.paneRoot, ExceptionHelper.createDetailMessage("情報の取得に失敗しました。", exception), Pos.CENTER, 3000);
                    }
                } else {
                    pane.setOpacity(0.6);
                }
            }
        });
        // RudeArrayTableを初期化する
        this.preparationToDoTable(screen.getBounds().getWidth());
        // 閉じる系の動作設定
        this.setCloseConfirmShowing(false);
        this.buttonClose.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                editor.close();
            }
        });
        // 追加
        this.buttonAdd.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                RudeArray row = editor.getDataController().createDefaultRow();
                try {
                    editor.rudeArrayTable.addRow(row);
                    editor.rudeArrayTable.loadMoreRows();
                    editor.updateContentFromSetting();
                } catch (SQLException exception) {
                    InstantAlert.show(editor.paneRoot, ExceptionHelper.createDetailMessage(exception), Pos.CENTER, 3000);
                }
            }
        });
        // 並び替え
        this.buttonSort.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (editor.rudeArrayTable.getItems().size() < 2) {
                    return;
                }
                LinkedHashMap<RudeArray, String> hashMap = new LinkedHashMap<>();
                for (RudeArray row: editor.rudeArrayTable.getItems()) {
                    hashMap.put(row, row.getString(Column.DESCRIPTION.getPhysicalName()));
                }
                SortPaneDialog<RudeArray> dialog = new SortPaneDialog<>(hashMap, editor.paneRoot);
                dialog.setTitle("並び替え");
                dialog.setMessage("マウスで並び替えを行ってOKを押してください。");
                dialog.setCloseEvent(new CloseEventHandler<LinkedHashMap<RudeArray,String>>() {
                    @Override
                    public void handle(LinkedHashMap<RudeArray, String> resultValue) {
                        if (resultValue == null) {
                            return;
                        }
                        try {
                            editor.rudeArrayTable.clearRows();
                            for (RudeArray row: resultValue.keySet()) {
                                editor.rudeArrayTable.addRow(row);
                            }
                            editor.rudeArrayTable.loadMoreRows();
                            editor.rudeArrayTable.setSelectedItem(null);;
                            editor.importDataFromForm();
                            editor.updateContentFromSetting();
                        } catch (SQLException exception) {
                            InstantAlert.show(editor.paneRoot, ExceptionHelper.createDetailMessage(exception), Pos.CENTER, 3000);
                        }
                    }
                });
                dialog.show();
            }
        });
        // 設定
        this.buttonSetting.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    SettingEditor settingEditor = new SettingEditor();
                    settingEditor.show();
                } catch (Exception exception) {
                    InstantAlert.show(editor.paneRoot, ExceptionHelper.createDetailMessage(exception), Pos.CENTER, 3000);
                }
            }
        });
        // データ入力
        this.updateRudeArrayTable();
    }
    
    private static String COLUMN_ID_OF_ITEM_COUNT = "item_count";
    
    private static String COLUMN_ID_OF_DELETE = "delete";
    
    /**
     * RudeArrayTableを初期化する.
     * @param screenWidth
     * @throws SQLException 
     */
    private void preparationToDoTable(double screenWidth) throws SQLException {
        ToDoEditor editor = this;
        double tableWidth = screenWidth - 200;
        // 関連ディレクトリ
        this.rudeArrayTable.addColumnHyperlink(Column.DIRECTORY.getPhysicalName(), Column.DIRECTORY.getLogicalName(), new EditableTable.ReadOnlyControlFactory<RudeArray, Hyperlink>() {
            @Override
            public Hyperlink newInstance(RudeArray item) {
                Hyperlink hyperlink = new Hyperlink();
                Runnable chooseDirectoryRunnable = new Runnable() {
                    @Override
                    public void run() {
                        DirectoryChooser chooser = new DirectoryChooser();
                        File file = chooser.showDialog(editor.getStage());
                        if (file != null) {
                            item.put(Column.DIRECTORY.getPhysicalName(), FileHelper.generateOptimizedPathFromURI(file.toURI()));
                            editor.rudeArrayTable.updateRow(item);
                            editor.importDataFromForm();
                        }
                    }
                };
                hyperlink.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        String directory = StringConverter.nullReplace(item.getString(Column.DIRECTORY.getPhysicalName()), "");
                        if (FileHelper.isExistsDirectory(directory)) {
                            try {
                                Runtime.getRuntime().exec(StringConverter.join(editor.filer, " ", directory));
                            } catch (Exception exception) {
                                InstantAlert.show(editor.paneRoot, ExceptionHelper.createDetailMessage(exception), Pos.CENTER, 3000);
                            }
                        } else {
                            chooseDirectoryRunnable.run();
                        }
                    }
                });
                hyperlink.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        if (event.getButton() == MouseButton.SECONDARY) {
                            chooseDirectoryRunnable.run();
                        }
                    }
                });
                hyperlink.setAlignment(Pos.CENTER);
                hyperlink.setTextOverrun(OverrunStyle.LEADING_ELLIPSIS);
                return hyperlink;
            }
            @Override
            public void setValueForControl(RudeArray item, Hyperlink control) {
                String directory = item.getString(Column.DIRECTORY.getPhysicalName());
                if (StringValidator.isBlank(directory)) {
                    control.setText("[未指定]");
                } else {
                    control.setText(directory);
                }
            }
        });
        this.rudeArrayTable.getHeaderLabel(Column.DIRECTORY.getPhysicalName()).setPrefWidth(tableWidth * 0.15);
        // ファイル数
        this.rudeArrayTable.addColumnLabel(COLUMN_ID_OF_ITEM_COUNT, "アイテム数", new EditableTable.ReadOnlyControlFactory<RudeArray, Label>() {
            @Override
            public Label newInstance(RudeArray item) {
                Label label = new Label();
                label.setAlignment(Pos.CENTER);
                return label;
            }
            @Override
            public void setValueForControl(RudeArray item, Label control) {
                String directory = StringConverter.nullReplace(item.getString(Column.DIRECTORY.getPhysicalName()), "");
                if (FileHelper.isExistsDirectory(directory)) {
                    File file = new File(directory);
                    control.setText(file.list().length + "個");
                } else {
                    control.setText("-");
                }
            }
        });
        this.rudeArrayTable.getHeaderLabel(COLUMN_ID_OF_ITEM_COUNT).setPrefWidth(tableWidth * 0.1);
        // 詳細
        this.rudeArrayTable.addColumnLabel(Column.DESCRIPTION.getPhysicalName(), Column.DESCRIPTION.getLogicalName(), new EditableTable.ReadOnlyControlFactory<RudeArray, Label>() {
            @Override
            public Label newInstance(RudeArray item) {
                Label label = new Label();
                label.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
                            LimitTextAreaPaneDialog dialog = new LimitTextAreaPaneDialog(editor.paneRoot);
                            dialog.setTitle("説明の入力");
                            dialog.setMessage("説明を入力してOKを押してください。");
                            dialog.setDefaultValue(item.getString(Column.DESCRIPTION.getPhysicalName()));
                            dialog.setCloseEvent(new CloseEventHandler<String>() {
                                @Override
                                public void handle(String resultValue) {
                                    if (resultValue != null) {
                                        item.put(Column.DESCRIPTION.getPhysicalName(), resultValue);
                                        editor.rudeArrayTable.updateRow(item);
                                        editor.importDataFromForm();
                                    }
                                }
                            });
                            dialog.show();
                        }
                    }
                });
                label.setPadding(new Insets(20, 20, 20, 20));
                return label;
            }
            @Override
            public void setValueForControl(RudeArray item, Label control) {
                String description = item.getString(Column.DESCRIPTION.getPhysicalName());
                if (StringValidator.isBlank(description)) {
                    control.setText("[ダブルクリックで入力]");
                    control.setOpacity(0.5);
                } else {
                    control.setText(description);
                    control.setOpacity(1);
                }
            }
        });
        this.rudeArrayTable.getHeaderLabel(Column.DESCRIPTION.getPhysicalName()).setPrefWidth(tableWidth * 0.65);
        // 削除
        this.rudeArrayTable.addColumnButton(COLUMN_ID_OF_DELETE, "削除", new FixControlFactory<RudeArray, ImeOffButton>() {
            @Override
            public ImeOffButton newInstance(RudeArray item) {
                ImeOffButton button = new ImeOffButton("削除");
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        ConfirmPane dialog = new ConfirmPane(editor.paneRoot);
                        dialog.setTitle("確認");
                        String description = StringConverter.nullReplace(item.getString(Column.DESCRIPTION.getPhysicalName()), "");
                        if (description.length() == 0) {
                            description = "[説明未設定]";
                        }
                        dialog.setMessage(StringConverter.join("TODOを削除します。", StringConverter.LINE_SEPARATOR, StringConverter.LINE_SEPARATOR, description));
                        dialog.setCloseEvent(new CloseEventHandler<DialogResult>() {
                            @Override
                            public void handle(DialogResult resultValue) {
                                if (resultValue == DialogResult.OK) {
                                    editor.rudeArrayTable.removeRow(item);
                                    editor.importDataFromForm();
                                }
                            }
                        });
                        dialog.show();
                    }
                });
                return button;
            }
        });
        this.rudeArrayTable.getHeaderLabel(COLUMN_ID_OF_DELETE).setPrefWidth(tableWidth * 0.05);
    }
    
    /**
     * RudeArrayTableを最新に更新する.
     * @throws SQLException
     */
    private void updateRudeArrayTable() throws SQLException {
        try (Database database = new Database()) {
            database.connect();
            try (ToDo todo = new ToDo(database)) {
                RudeArray[] rows = todo.search();
                this.rudeArrayTable.clearRows();
                this.rudeArrayTable.addRows(rows);
                this.rudeArrayTable.loadMoreRows();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        this.updateContentFromSetting();
    }

    @Override
    protected void importDataFromForm() {
        this.getDataController().clearRows();
        for (RudeArray row: this.rudeArrayTable.getItems()) {
            this.getDataController().addRow(row);
        }
        try {
            this.getDataController().getDatabase().begin(IsolationLevel.EXCLUSIVE);
            this.getDataController().validate();
            this.getDataController().normalize();
            this.getDataController().update();
            this.getDataController().getDatabase().commit();
        } catch (Exception exception) {
            InstantAlert.show(this.paneRoot, ExceptionHelper.createDetailMessage("情報の保存に失敗しました。", exception), Pos.CENTER, 3000);
        }
    }

    @Override
    protected void beforeCloseDoPreparation() throws Exception {
    }

}
