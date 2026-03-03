package by.egor.service;

import by.egor.model.FileItem;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FileService {

    public List<FileItem> getFiles(String path, boolean neccesseryToHide) {
        List<FileItem> result = new ArrayList<>();

        File directory = new File(path);

        if(!directory.exists()){
            System.err.println("    Директория не существует!!!!");
            return result;
        }

        if(!directory.isDirectory()){
            System.err.println("    Это не директория!!!!");
            return result;
        }


        File[] files = directory.listFiles();

        if (files == null) {
            System.err.println("    Нет доступа к директории: " + path);
            return result;
        }

        for (File file : files) {
            if(file.isHidden() && neccesseryToHide){
                continue;
            }
            result.add(new FileItem(file));
            System.out.println(new FileItem(file).getExtension());
        }

        result.sort(new Comparator<FileItem>() {
            @Override
            public int compare(FileItem o1, FileItem o2) {
                if(o1.isDirectory() && !o2.isDirectory()) return -1;
                if(!o1.isDirectory() && o2.isDirectory()) return 1;

                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });

        return result;


    }

    public void createNewFile(String path){
        File file = new File(path);
        try(FileWriter writer = new FileWriter(file)){
            writer.write("");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public StringBuilder readFile(FileItem item){
        try(BufferedReader reader = new BufferedReader(new FileReader(item.getFile()))){
            StringBuilder sb = new StringBuilder();
            String line = "";
            while ((line = reader.readLine())!=null){
                sb.append(line).append("\n");
            }
            return sb;

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeInFile(FileItem item, String content){
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(item.getFile()))){
            writer.write(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeLog(String message){
        File file = new File("app.log");
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(file,true))){
            LocalDateTime ldt = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
            writer.write("[" + formatter.format(ldt) + "] " + message);
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
