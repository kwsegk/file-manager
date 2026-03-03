package by.egor.model;

import java.util.ArrayList;
import java.util.List;

public class Settings {
    private String lastOpenedPath;
    private boolean showHiddenFiles;
    private int windowWidth;
    private int windowHeight;
    private List<String> favoritePaths;

    public Settings() {
        this.lastOpenedPath = "/Users/egorkuznetsov/Desktop/FileManagerTest";
        this.showHiddenFiles = false;
        this.windowWidth = 800;
        this.windowHeight = 600;
        this.favoritePaths = new ArrayList<>();
    }

    public String getLastOpenedPath() {
        return lastOpenedPath;
    }

    public void setLastOpenedPath(String lastOpenedPath) {
        this.lastOpenedPath = lastOpenedPath;
    }

    public boolean isShowHiddenFiles() {
        return showHiddenFiles;
    }

    public void setShowHiddenFiles(boolean showHiddenFiles) {
        this.showHiddenFiles = showHiddenFiles;
    }

    public int getWindowWidth() {
        return windowWidth;
    }

    public void setWindowWidth(int windowWidth) {
        this.windowWidth = windowWidth;
    }

    public int getWindowHeight() {
        return windowHeight;
    }

    public void setWindowHeight(int windowHeight) {
        this.windowHeight = windowHeight;
    }

    public List<String> getFavoritePaths() {
        if (favoritePaths == null) {
            favoritePaths = new ArrayList<>();
        }
        return favoritePaths;
    }

    public void setFavoritePaths(List<String> favoritePaths) {
        this.favoritePaths = favoritePaths;
    }
}
