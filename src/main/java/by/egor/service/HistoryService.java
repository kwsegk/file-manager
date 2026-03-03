package by.egor.service;

import by.egor.model.HistoryEntry;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class HistoryService {
    private static final String HISTORY_FILE = "history.dat";

    public void addEntry(String action, String filename){
        List<HistoryEntry> history = loadHistory();
        history.add(new HistoryEntry(action, filename));
        saveHistory(history);
    }

    public void saveHistory(List<HistoryEntry> history){
        try(ObjectOutputStream ous = new ObjectOutputStream(new FileOutputStream(HISTORY_FILE))){
            ous.writeObject(history);
        } catch (
                FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<HistoryEntry> loadHistory(){
        File file = new File(HISTORY_FILE);
        if(!file.exists()){
            return new ArrayList<>();
        }
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(HISTORY_FILE))){
            return (List<HistoryEntry>) ois.readObject();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            return new ArrayList<>();
        }
    }
}
