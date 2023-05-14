package jp.ac.cm0107.musicplayer;

import java.io.File;

public class RowModel {
    private File file;
    public RowModel(File file){
        this.file = file;
    }
    public String getFileName() {
        return file.getName();
    }
    public long getFileSize(){
        return  file.length();
    }
    public File getFile() {
        return file;
    }
}
