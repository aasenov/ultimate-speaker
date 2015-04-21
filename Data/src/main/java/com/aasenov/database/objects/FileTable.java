package com.aasenov.database.objects;

public class FileTable extends DatabaseTable<FileItem> {

    public FileTable(String tableName, boolean recreate) {
        super(tableName, recreate);
    }

    @Override
    public String getCreateTableProperties() {
        return FileItem.getDatabaseTableProperties();
    }

    @Override
    public String getCreateTableIndexProperties() {
        return FileItem.getIndexColumns();
    }

}
