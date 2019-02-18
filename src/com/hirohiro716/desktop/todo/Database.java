package com.hirohiro716.desktop.todo;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import com.hirohiro716.ByteConverter;
import com.hirohiro716.StringConverter;
import com.hirohiro716.database.AbstractBindTable;
import com.hirohiro716.database.sqlite.SQLite;
import com.hirohiro716.file.FileHelper;

/**
 * 本システムのデータベースコントロールクラス.
 * @author hiro
 */
public class Database extends SQLite {
    
    /**
     * AbstractBindTableを継承したクラスからテーブル名を取得する.
     * @param <T> AbstractBindTableを継承したクラス
     * @param tableClass テーブル名を求めるクラス
     * @return テーブル名
     */
    public static <T extends AbstractBindTable> String getTableName(Class<T> tableClass) {
        return AbstractBindTable.getTableName(tableClass, Database.class);
    }

    /**
     * データベースに接続する.
     * @throws SQLException
     */
    public void connect() throws SQLException {
        try {
            String location = StringConverter.join(System.getProperty("user.home"), File.separator, ".hirohiro716", File.separator, "desktop-todo", File.separator);
            String databaseLocation = location + "database.db";
            if (FileHelper.isExistsDirectory(location) == false) {
                // データベースファイルの保存場所確保
                if (FileHelper.makeDirectory(location) == false) {
                    throw new IOException("Unable to create directory for configuration file.");
                }
            }
            if (FileHelper.isExistsFile(databaseLocation) == false) {
                // データベースファイルの新規作成
                super.connect(databaseLocation);
                String allSQL = new String(ByteConverter.loadBytesFromResource(this.getClass().getResourceAsStream("Database.sql")));
                for (String sql: allSQL.split(";")) {
                    try {
                        this.execute(sql);
                    } catch (SQLException exception) {
                        exception.printStackTrace();
                    }
                }
            } else {
                super.connect(databaseLocation);
            }
        } catch (IOException exception) {
            throw new SQLException(exception);
        } catch (ClassNotFoundException exception) {
            exception.printStackTrace();
        }
    }
    
}
