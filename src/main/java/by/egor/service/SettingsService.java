package by.egor.service;

import by.egor.model.Settings;

import java.io.*;

public class SettingsService {
    private static final String SETTINGS_FILE = "settings.dat";

    public void saveSettings(Settings settings){
        try(DataOutputStream dos = new DataOutputStream(new FileOutputStream(SETTINGS_FILE))){
            dos.writeUTF(settings.getLastOpenedPath());
            dos.writeBoolean(settings.isShowHiddenFiles());
            dos.writeInt(settings.getWindowHeight());
            dos.writeInt(settings.getWindowWidth());
        } catch (IOException e){
            throw new RuntimeException();
        }
    }

    public Settings loadSettings() throws IOException {
        try(DataInputStream dis = new DataInputStream(new FileInputStream(SETTINGS_FILE))){
            Settings settings = new Settings();
            settings.setLastOpenedPath(dis.readUTF());
            settings.setShowHiddenFiles(dis.readBoolean());
            settings.setWindowHeight(dis.readInt());
            settings.setWindowWidth(dis.readInt());
            return settings;
        } catch (IOException e){
            return new Settings();
        }
    }
}
