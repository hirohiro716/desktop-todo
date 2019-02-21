package com.hirohiro716.desktop.todo;

import java.sql.SQLException;

import com.hirohiro716.RudeArray;
import com.hirohiro716.StringConverter;
import com.hirohiro716.database.AbstractBindTable;
import com.hirohiro716.database.InterfaceColumn;
import com.hirohiro716.database.ValidationException;
import com.hirohiro716.database.sqlite.AbstractBindTableRows;
import com.hirohiro716.database.sqlite.SQLite;
import com.hirohiro716.file.xml.InterfaceProperty;
import com.hirohiro716.validate.StringValidator;

/**
 * 設定のクラス.
 * @author hiro
 */
public class Setting extends AbstractBindTableRows {
    
    /**
     * 設定のカラム列挙型.
     * @author hiro
     */
    @SuppressWarnings("javadoc")
    public enum Column implements InterfaceColumn {
        NAME("設定名", null),
        VALUE("値", null),
        ;
        
        private Column(String logicalName, Object defaultValue) {
            this(logicalName, defaultValue, -1);
        }
        
        private Column(String logicalName, Object defaultValue, int maxLength) {
            this.logicalName = logicalName;
            this.defaultValue = defaultValue;
            this.maxLength = maxLength;
        }
        
        private String logicalName;
        
        @Override
        public String getLogicalName() {
            return this.logicalName;
        }

        @Override
        public String getFullPhysicalName() {
            return StringConverter.join(Database.getTableName(ToDo.class), ".", this.getPhysicalName());
        }
        
        private Object defaultValue;

        @Override
        public Object getDefaultValue() {
            return this.defaultValue;
        }
        
        private int maxLength;

        @Override
        public int getMaxLength() {
            return this.maxLength;
        }
        
    }
    
    /**
     * コンストラクタ.
     * @param database
     */
    public Setting(Database database) {
        super(database);
    }

    @Override
    public String getTableName() {
        return "setting";
    }

    @Override
    public String getDescription() {
        return "設定";
    }

    @Override
    public RudeArray createDefaultRow() {
        RudeArray row = new RudeArray();
        return row;
    }

    @Override
    public boolean isEditing(SQLite sqlite) throws SQLException {
        return false;
    }

    @Override
    protected void updateToEditing(SQLite sqlite) throws SQLException {
    }

    @Override
    protected void updateToEditingFinish() throws SQLException {
    }

    @Override
    public boolean isPermittedSearchConditioEmptyUpdate() {
        return true;
    }
    
    /**
     * 編集中のすべての設定値を連想配列で取得する.
     * @return RudeArray
     */
    public RudeArray getEditingAllSettings() {
        RudeArray settings = AbstractBindTable.createDefaultRow(Property.values());
        for (RudeArray row: this.getRows()) {
            settings.put(row.getString(Column.NAME.getPhysicalName()), row.getString(Column.VALUE.getPhysicalName()));
        }
        return settings;
    }
    
    /**
     * 編集中のすべての設定値を連想配列でセットする.
     * @param rudeArray
     */
    public void setEditingAllSettings(RudeArray rudeArray) {
        this.clearRows();
        for (Property property: Property.values()) {
            RudeArray row = this.createDefaultRow();
            row.put(Column.NAME.getPhysicalName(), property.getPhysicalName());
            row.put(Column.VALUE.getPhysicalName(), rudeArray.getString(property.getPhysicalName()));
            this.addRow(row);
        }
    }
    
    /**
     * すべての設定を連想配列で取得する.
     * @return RudeArray
     * @throws SQLException
     */
    public RudeArray fetchAllSettings() throws SQLException {
        RudeArray[] rows = this.search();
        RudeArray settings = AbstractBindTable.createDefaultRow(Property.values());
        for (RudeArray row: rows) {
            settings.put(row.getString(Column.NAME.getPhysicalName()), row.getString(Column.VALUE.getPhysicalName()));
        }
        return settings;
    }
    
    @Override
    public void validate() throws ValidationException {
        RudeArray row = this.getEditingAllSettings();
        for (Property property: Property.values()) {
            try {
                StringValidator validator = new StringValidator(property.getLogicalName());
                String key = property.getPhysicalName();
                switch (property) {
                case FILER:
                    validator.addBlankCheck();
                    validator.execute(row.getString(key));
                    break;
                case FONT_STYLE:
                    break;
                case FONT_SIZE:
                    validator.addBlankCheck();
                    validator.addIntegerCheck();
                    validator.execute(row.getString(key));
                    break;
                case BACKGROUND:
                case TEXT_FILL:
                case SELECTED_ROW_BACKGROUND:
                    validator.addBlankCheck();
                    validator.execute(row.getString(key));
                    break;
                case OFFSET_X:
                case OFFSET_Y:
                    validator.addBlankCheck();
                    validator.addDecimalCheck();
                    validator.execute(row.getShort(key));
                    break;
                case COLUMN_WIDTH_RATE_DIRECTORY:
                case COLUMN_WIDTH_RATE_ITEM_COUNT:
                case COLUMN_WIDTH_RATE_DESCRIPTION:
                case COLUMN_WIDTH_RATE_DELETE:
                    validator.addBlankCheck();
                    validator.addDecimalCheck();
                    validator.addMaxValueCheck(1);
                    validator.addMinValueCheck(0.01);
                    validator.execute(row.getString(key));
                    break;
                }
            } catch (com.hirohiro716.validate.ValidationException exception) {
                throw new ValidationException(exception.getMessage(), property);
            }
        }
    }

    @Override
    public void normalize() {
        RudeArray row = this.getEditingAllSettings();
        for (Property property: Property.values()) {
            String key = property.getPhysicalName();
            switch (property) {
            case FILER:
            case FONT_STYLE:
            case FONT_SIZE:
            case BACKGROUND:
            case TEXT_FILL:
            case SELECTED_ROW_BACKGROUND:
            case OFFSET_X:
            case OFFSET_Y:
            case COLUMN_WIDTH_RATE_DIRECTORY:
            case COLUMN_WIDTH_RATE_ITEM_COUNT:
            case COLUMN_WIDTH_RATE_DESCRIPTION:
            case COLUMN_WIDTH_RATE_DELETE:
                row.put(key, row.getString(key));
                break;
            }
        }
        this.setEditingAllSettings(row);
    }
    
    /**
     * 設定項目の列挙型.
     * @author hiro
     */
    @SuppressWarnings("javadoc")
    public enum Property implements InterfaceProperty {
        FILER("ファイラー", null),
        FONT_STYLE("フォントスタイル", "System"),
        FONT_SIZE("フォントサイズ", 16),
        BACKGROUND("背景色", "rgba(255,255,255,1)"),
        TEXT_FILL("文字色", "#333"),
        SELECTED_ROW_BACKGROUND("選択行の背景色", "rgba(100,100,100,1)"),
        OFFSET_X("画面の横方向オフセット", 0),
        OFFSET_Y("画面の縦方向オフセット", 0),
        COLUMN_WIDTH_RATE_DIRECTORY("関連ディレクトリの幅比率", 0.15),
        COLUMN_WIDTH_RATE_ITEM_COUNT("アイテム数の幅比率", 0.1),
        COLUMN_WIDTH_RATE_DESCRIPTION("説明の幅比率", 0.65),
        COLUMN_WIDTH_RATE_DELETE("削除の幅比率", 0.05),
        ;
        
        private Property(String logicalName, Object defaultValue) {
            this(logicalName, defaultValue, -1);
        }
        
        private Property(String logicalName, Object defaultValue, int maxLength) {
            this.logicalName = logicalName;
            this.defaultValue = defaultValue;
            this.maxLength = maxLength;
        }
        
        private String logicalName;
        
        @Override
        public String getLogicalName() {
            return this.logicalName;
        }
        
        private Object defaultValue;

        @Override
        public Object getDefaultValue() {
            return this.defaultValue;
        }
        
        private int maxLength;

        @Override
        public int getMaxLength() {
            return this.maxLength;
        }
       
    }
    
}
