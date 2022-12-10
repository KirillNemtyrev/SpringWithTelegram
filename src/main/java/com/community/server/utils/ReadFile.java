package com.community.server.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ReadFile {
    private File file;

    public ReadFile(){}

    public ReadFile(File file){
        this.file = file;
    }

    public String get(){
        return get(this.file);
    }

    public String get(File file){

        if(!file.isFile()){
            return null;
        }

        try (FileInputStream in = new FileInputStream(file.getPath())) {

            byte[] buffer = new byte[in.available()];
            in.read(buffer, 0, buffer.length);

            return new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }

    }
}
