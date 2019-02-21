package com.hirohiro716.desktop.todo;

import java.sql.SQLException;

import com.hirohiro716.RudeArray;
import com.hirohiro716.StringConverter;
import com.hirohiro716.database.AbstractBindTable;
import com.hirohiro716.database.InterfaceColumn;
import com.hirohiro716.database.ValidationException;
import com.hirohiro716.database.sqlite.AbstractBindTableRows;
import com.hirohiro716.database.sqlite.SQLite;
import com.hirohiro716.validate.StringValidator;

/**
 * TODO情報のクラス.
 * @author hiro
 */
public class ToDo extends AbstractBindTableRows {
    
    /**
     * TODO情報のカラム列挙型.
     * @author hiro
     */
    @SuppressWarnings("javadoc")
    public enum Column implements InterfaceColumn {
        ID("ID", null),
        DESCRIPTION("説明", null),
        DIRECTORY("関連ディレクトリ", null),
        LIMIT_OF_ITEM_COUNT("アイテム数の制限", 999),
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
    public ToDo(Database database) {
        super(database);
    }

    @Override
    public String getTableName() {
        return "todo";
    }

    @Override
    public String getDescription() {
        return "TODO";
    }

    @Override
    public RudeArray createDefaultRow() {
        return AbstractBindTable.createDefaultRow(Column.values());
    }
    
    @Override
    public boolean isEditing(SQLite sqlite) {
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

    @Override
    public void validate() throws ValidationException {
        for (RudeArray row: this.getRows()) {
            for (Column column: Column.values()) {
                try {
                    StringValidator validator = new StringValidator(column.getLogicalName());
                    String key = column.getPhysicalName();
                    switch (column) {
                    case ID:
                        validator.addIntegerCheck();
                        validator.execute(row.getString(key));
                        break;
                    case DESCRIPTION:
                        break;
                    case DIRECTORY:
                        break;
                    case LIMIT_OF_ITEM_COUNT:
                        validator.addBlankCheck();
                        validator.addIntegerCheck();
                        validator.execute(row.getString(key));
                        break;
                    }
                } catch (com.hirohiro716.validate.ValidationException exception) {
                    throw new ValidationException(exception.getMessage(), column);
                }
            }
        }
    }

    @Override
    public void normalize() {
        int incrementId = 1;
        for (RudeArray row: this.getRows()) {
            for (Column column: Column.values()) {
                String key = column.getPhysicalName();
                switch (column) {
                case ID:
                    row.put(key, incrementId);
                    break;
                case DESCRIPTION:
                    break;
                case DIRECTORY:
                    break;
                case LIMIT_OF_ITEM_COUNT:
                    break;
                }
            }
            incrementId++;
        }
    }

}
