package by.egor.controller;


import by.egor.model.FileItem;
import by.egor.model.HistoryEntry;
import by.egor.model.Settings;
import by.egor.service.FileService;
import by.egor.service.HistoryService;
import by.egor.service.SettingsService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static by.egor.service.Alert.*;

public class MainController {

    private String basePath = "";
    private final FileService fileService = new FileService();
    private final SettingsService settingsService = new SettingsService();
    private final HistoryService historyService = new HistoryService();
    private Settings settings;

    private final List<String> navigationHistory = new ArrayList<>();
    private int currentHistoryIndex = -1;

    private FileItem clipboardFile = null;
    private boolean isCutOperation = false; // true = вырезать, false = копировать

    private final List<String> favoritePaths = new ArrayList<>();

    @FXML
    private MenuItem menuItemNewFile;
    @FXML
    private MenuItem menuItemNewFolder;
    @FXML
    private MenuItem menuItemDelete;
    @FXML
    private MenuItem menuItemExit;
    @FXML
    private MenuItem menuItemHidden;
    @FXML
    private MenuItem menuItemHistory;
    @FXML
    private MenuItem menuItemCopy;
    @FXML
    private MenuItem menuItemCut;
    @FXML
    private MenuItem menuItemPaste;
    @FXML
    private MenuItem menuItemRename;

    @FXML
    private TableView<FileItem> tableFiles;
    @FXML
    private TableColumn<FileItem,String> colIcon;
    @FXML
    private TableColumn<FileItem,String> colName;
    @FXML
    private TableColumn<FileItem,String> colSize;
    @FXML
    private TableColumn<FileItem,String> colDate;
    @FXML
    private TableColumn<FileItem,String> colType;
    @FXML
    private TableColumn<FileItem, String> colFormat;

    @FXML
    private Label labelTotal;
    @FXML
    private Label labelSelected;

    @FXML
    private Button btnBack;
    @FXML
    private Button btnForward;
    @FXML
    private Button btnUp;
    @FXML
    private Button btnHome;
    @FXML
    private Button btnSearch;

    @FXML
    private ComboBox<String> comboPath;

    @FXML
    private HBox breadcrumbBox;

    @FXML
    private ListView<String> favoritesList;


    @FXML
    public void initialize(){
        try {
            settings = settingsService.loadSettings();
            basePath = settings.getLastOpenedPath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (basePath == null || basePath.isEmpty()) {
            basePath = System.getProperty("user.home");
        }
        addToNavigationHistory(basePath);

        colIcon.setSortable(false);
        colName.setSortable(true);
        colSize.setSortable(true);
        colDate.setSortable(true);
        colType.setSortable(true);
        colFormat.setSortable(true);

        colIcon.setCellValueFactory(new PropertyValueFactory<>("icon"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colSize.setCellValueFactory(new PropertyValueFactory<>("size"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colFormat.setCellValueFactory(new PropertyValueFactory<>("extension"));

        tableFiles.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        tableFiles.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                handleDoubleClick();
            }
        });

        tableFiles.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    int selectedCount = tableFiles.getSelectionModel().getSelectedItems().size();
                    if (selectedCount == 0) {
                        labelSelected.setText("Файл не выбран");
                    } else if (selectedCount == 1) {
                        if (!newValue.getExtension().isEmpty()){
                            labelSelected.setText("Выбран - " + newValue.getName() + "." + newValue.getExtension());
                        }
                        else {
                            labelSelected.setText("Выбран - " + newValue.getName());
                        }
                    } else {
                        labelSelected.setText("Выбрано файлов: " + selectedCount);
                    }
                }
        );

        ContextMenu contextMenu = new ContextMenu();
        
        MenuItem ctxOpen = new MenuItem("Открыть");
        ctxOpen.setOnAction(e -> handleDoubleClick());
        
        MenuItem ctxRename = new MenuItem("Переименовать");
        ctxRename.setOnAction(e -> renameFile());
        
        MenuItem ctxCopy = new MenuItem("Копировать");
        ctxCopy.setOnAction(e -> copyFile());
        
        MenuItem ctxCut = new MenuItem("Вырезать");
        ctxCut.setOnAction(e -> cutFile());
        
        MenuItem ctxPaste = new MenuItem("Вставить");
        ctxPaste.setOnAction(e -> pasteFile());
        
        MenuItem ctxDelete = new MenuItem("Удалить");
        ctxDelete.setOnAction(e -> deleteFile());
        
        contextMenu.getItems().addAll(
            ctxOpen,
            new SeparatorMenuItem(),
            ctxRename,
            new SeparatorMenuItem(),
            ctxCopy,
            ctxCut,
            ctxPaste,
            new SeparatorMenuItem(),
            ctxDelete
        );
        
        tableFiles.setContextMenu(contextMenu);

        comboPath.setEditable(false);
        comboPath.setValue(basePath);

        comboPath.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(basePath) && !newValue.equals(oldValue)) {
                navigateToPath(newValue);
                historyService.addEntry("NAVIGATE_FROM_HISTORY", newValue);
            }
        });

        loadFavorites();
        favoritesList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selected = favoritesList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    navigateToPath(selected);
                    historyService.addEntry("NAVIGATE_FAVORITE", selected);
                }
            }
        });

        updateNavigationButtons();
        updateBreadcrumb();
        reloadTable();
    }

    private void handleDoubleClick() {
        FileItem selected = tableFiles.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        if (selected.isDirectory()) {
            navigateToPath(selected.getPath());
            historyService.addEntry("NAVIGATE", selected.getPath());
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/text-editor.fxml"));
            Parent root = loader.load();

            TextEditorController controller = loader.getController();

            controller.setFile(selected);
            controller.setService(fileService);
            controller.setHistoryService(historyService);

            controller.loadFileContent();

            Stage stage = new Stage();
            stage.setTitle("Редактор: " + selected.getName());
            stage.setScene(new Scene(root));
            historyService.addEntry("OPEN", selected.getPath());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            reloadTable();

        } catch (IOException e) {
            System.err.println("Ошибка при открытии редактора: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void createNewFile(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/create-dialog.fxml"));
            Parent root = loader.load();

            CreateDialogController controller = loader.getController();
            controller.setPathName(basePath);
            controller.setService(fileService);
            controller.setController(this);
            controller.setHistoryService(historyService);

            Stage stage = new Stage();
            stage.setTitle("Создание файла");
            stage.setScene(new Scene(root));
            stage.showAndWait();


        } catch (IOException e) {
            System.err.println("Ошибка при загрузке диалогового окна: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void deleteFile(){
        var selectedItems = tableFiles.getSelectionModel().getSelectedItems();
        if (selectedItems.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Выберите файлы для удаления!");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение");
        alert.setHeaderText("Удаление файлов");
        
        if (selectedItems.size() == 1) {
            alert.setContentText("Вы уверены, что хотите удалить файл " + selectedItems.get(0).getName() + "?");
        } else {
            alert.setContentText("Вы уверены, что хотите удалить " + selectedItems.size() + " файлов?");
        }

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        int successCount = 0;
        int failCount = 0;
        
        for (FileItem item : new ArrayList<>(selectedItems)) {
            if (item.getFile().delete()){
                successCount++;
                historyService.addEntry("DELETE", item.getPath());
            } else{
                failCount++;
            }
        }
        
        if (successCount > 0 && failCount == 0) {
            showAlert(Alert.AlertType.INFORMATION, "Успешно удалено файлов: " + successCount);
        } else if (successCount > 0 && failCount > 0) {
            showAlert(Alert.AlertType.WARNING, "Удалено: " + successCount + ", ошибок: " + failCount);
        } else {
            showAlert(Alert.AlertType.ERROR, "Ошибка при удалении файлов");
        }
        
        reloadTable();
    }

    @FXML
    private void exit(){
        settings.setLastOpenedPath(basePath);
        settingsService.saveSettings(settings);
        Stage stage = (Stage) tableFiles.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void hidePressed(){
        settings.setShowHiddenFiles(!settings.isShowHiddenFiles());
        reloadTable();
    }

    @FXML
    private void historyShow(){
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/history-window.fxml"));
        try {
            Parent root = loader.load();
            HistoryController controller = loader.getController();
            controller.setHistoryService(historyService);
            controller.loadHistory();
            Stage stage = new Stage();
            stage.setTitle("История");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void reloadTable(){
        List<FileItem> items =  fileService.getFiles(basePath, settings.isShowHiddenFiles());
        tableFiles.getItems().setAll(items);
        labelTotal.setText("Всего файлов: " + items.size());
        comboPath.setValue(basePath);
        updateBreadcrumb();
    }

    private void updateBreadcrumb() {
        breadcrumbBox.getChildren().clear();
        
        Label pathLabel = new Label("Путь:");
        pathLabel.setStyle("-fx-font-weight: bold;");
        breadcrumbBox.getChildren().add(pathLabel);

        File currentFile = new File(basePath);
        List<File> pathSegments = new ArrayList<>();

        File temp = currentFile;
        while (temp != null) {
            pathSegments.add(0, temp);
            temp = temp.getParentFile();
        }

        for (int i = 0; i < pathSegments.size(); i++) {
            File segment = pathSegments.get(i);
            String segmentName = segment.getName();

            if (segmentName.isEmpty()) {
                segmentName = segment.getPath();
            }

            Button segmentButton = new Button(segmentName);

            String segmentPath = segment.getAbsolutePath();
            segmentButton.setOnAction(e -> {
                navigateToPath(segmentPath);
                historyService.addEntry("BREADCRUMB_NAVIGATE", segmentPath);
            });

            breadcrumbBox.getChildren().add(segmentButton);

            if (i < pathSegments.size() - 1) {
                Label separator = new Label(">");
                breadcrumbBox.getChildren().add(separator);
            }
        }
    }

    private void navigateToPath(String newPath) {
        File directory = new File(newPath);
        if (!directory.exists() || !directory.isDirectory()) {
            showAlert(Alert.AlertType.ERROR, "Путь не существует или это не директория: " + newPath);
            return;
        }

        basePath = newPath;
        addToNavigationHistory(newPath);
        updateNavigationButtons();
        reloadTable();
    }

    private void addToNavigationHistory(String path) {
        if (currentHistoryIndex < navigationHistory.size() - 1) {
            navigationHistory.subList(currentHistoryIndex + 1, navigationHistory.size()).clear();
        }

        navigationHistory.add(path);
        currentHistoryIndex = navigationHistory.size() - 1;

        if (!comboPath.getItems().contains(path)) {
            comboPath.getItems().add(0, path);
            if (comboPath.getItems().size() > 20) {
                comboPath.getItems().remove(comboPath.getItems().size() - 1);
            }
        }
    }

    private void updateNavigationButtons() {
        btnBack.setDisable(currentHistoryIndex <= 0);
        btnForward.setDisable(currentHistoryIndex >= navigationHistory.size() - 1);
        
        File currentDir = new File(basePath);
        btnUp.setDisable(currentDir.getParent() == null);
    }

    @FXML
    private void navigateBack() {
        if (currentHistoryIndex > 0) {
            currentHistoryIndex--;
            basePath = navigationHistory.get(currentHistoryIndex);
            updateNavigationButtons();
            reloadTable();
            historyService.addEntry("NAVIGATE_BACK", basePath);
        }
    }

    @FXML
    private void navigateForward() {
        if (currentHistoryIndex < navigationHistory.size() - 1) {
            currentHistoryIndex++;
            basePath = navigationHistory.get(currentHistoryIndex);
            updateNavigationButtons();
            reloadTable();
            historyService.addEntry("NAVIGATE_FORWARD", basePath);
        }
    }

    @FXML
    private void navigateUp() {
        File currentDir = new File(basePath);
        String parentPath = currentDir.getParent();
        
        if (parentPath != null) {
            navigateToPath(parentPath);
            historyService.addEntry("NAVIGATE_UP", parentPath);
        }
    }

    @FXML
    private void navigateHome() {
        String homePath = System.getProperty("user.home");
        navigateToPath(homePath);
        historyService.addEntry("NAVIGATE_HOME", homePath);
    }

    @FXML
    private void searchOrNavigate() {
        TextInputDialog dialog = new TextInputDialog(basePath);
        dialog.setTitle("Переход к папке");
        dialog.setHeaderText("Введите путь к директории");
        dialog.setContentText("Путь:");

        dialog.getDialogPane().setPrefWidth(500);

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(path -> {
            if (!path.trim().isEmpty()) {
                navigateToPath(path.trim());
                historyService.addEntry("NAVIGATE_SEARCH", path.trim());
            }
        });
    }

    @FXML
    private void createNewFolder() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Создание папки");
        dialog.setHeaderText("Создание новой папки");
        dialog.setContentText("Имя папки:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(folderName -> {
            if (!folderName.trim().isEmpty()) {
                File newFolder = new File(basePath, folderName.trim());
                if (newFolder.exists()) {
                    showAlert(Alert.AlertType.ERROR, "Папка с таким именем уже существует!");
                } else {
                    if (newFolder.mkdir()) {
                        showAlert(Alert.AlertType.INFORMATION, "Папка успешно создана!");
                        historyService.addEntry("CREATE_FOLDER", newFolder.getAbsolutePath());
                        reloadTable();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Ошибка при создании папки!");
                    }
                }
            }
        });
    }

    @FXML
    private void renameFile() {
        FileItem selected = tableFiles.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Выберите файл или папку для переименования!");
            return;
        }

        String currentName = selected.getName();
        if (!selected.getExtension().isEmpty()) {
            currentName = selected.getName() + "." + selected.getExtension();
        }
        
        final String finalCurrentName = currentName;

        TextInputDialog dialog = new TextInputDialog(currentName);
        dialog.setTitle("Переименование");
        dialog.setHeaderText("Переименование " + (selected.isDirectory() ? "папки" : "файла"));
        dialog.setContentText("Новое имя:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            if (!newName.trim().isEmpty() && !newName.equals(finalCurrentName)) {
                File oldFile = selected.getFile();
                File newFile = new File(basePath, newName.trim());
                
                if (newFile.exists()) {
                    showAlert(Alert.AlertType.ERROR, "Файл с таким именем уже существует!");
                } else {
                    if (oldFile.renameTo(newFile)) {
                        showAlert(Alert.AlertType.INFORMATION, "Успешно переименовано!");
                        historyService.addEntry("RENAME", oldFile.getName() + " -> " + newName);
                        reloadTable();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Ошибка при переименовании!");
                    }
                }
            }
        });
    }

    @FXML
    private void copyFile() {
        FileItem selected = tableFiles.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Выберите файл или папку для копирования!");
            return;
        }

        clipboardFile = selected;
        isCutOperation = false;
        labelSelected.setText("Скопировано: " + selected.getName());
        historyService.addEntry("COPY", selected.getPath());
    }

    @FXML
    private void cutFile() {
        FileItem selected = tableFiles.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Выберите файл или папку для вырезания!");
            return;
        }

        clipboardFile = selected;
        isCutOperation = true;
        labelSelected.setText("Вырезано: " + selected.getName());
        historyService.addEntry("CUT", selected.getPath());
    }

    @FXML
    private void pasteFile() {
        if (clipboardFile == null) {
            showAlert(Alert.AlertType.WARNING, "Буфер обмена пуст!");
            return;
        }

        File sourceFile = clipboardFile.getFile();
        String fileName = sourceFile.getName();
        File targetFile = new File(basePath, fileName);

        if (sourceFile.getParent().equals(basePath) && isCutOperation) {
            showAlert(Alert.AlertType.WARNING, "Файл уже находится в этой папке!");
            return;
        }

        if (targetFile.exists()) {
            String baseName = fileName;
            String extension = "";
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex > 0) {
                baseName = fileName.substring(0, dotIndex);
                extension = fileName.substring(dotIndex);
            }
            
            int counter = 1;
            while (targetFile.exists()) {
                targetFile = new File(basePath, baseName + "_копия_" + counter + extension);
                counter++;
            }
        }

        try {
            if (isCutOperation) {
                if (sourceFile.renameTo(targetFile)) {
                    showAlert(Alert.AlertType.INFORMATION, "Файл успешно перемещён!");
                    historyService.addEntry("MOVE", sourceFile.getName() + " -> " + basePath);
                    clipboardFile = null; // Очищаем буфер после перемещения
                } else {
                    showAlert(Alert.AlertType.ERROR, "Ошибка при перемещении файла!");
                }
            } else {
                copyFileOrDirectory(sourceFile, targetFile);
                showAlert(Alert.AlertType.INFORMATION, "Файл успешно скопирован!");
                historyService.addEntry("PASTE", targetFile.getName() + " -> " + basePath);
            }
            reloadTable();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка при копировании: " + e.getMessage());
        }
    }

    private void copyFileOrDirectory(File source, File target) throws IOException {
        if (source.isDirectory()) {
            if (!target.exists()) {
                target.mkdir();
            }

            String[] files = source.list();
            if (files != null) {
                for (String file : files) {
                    File srcFile = new File(source, file);
                    File destFile = new File(target, file);
                    copyFileOrDirectory(srcFile, destFile);
                }
            }
        } else {
            java.nio.file.Files.copy(
                source.toPath(),
                target.toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );
        }
    }

    private void loadFavorites() {
        try {
            favoritePaths.clear();
            favoritePaths.addAll(settings.getFavoritePaths());
            favoritesList.getItems().setAll(favoritePaths);
        } catch (Exception e) {
        }
    }

    private void saveFavorites() {
        settings.setFavoritePaths(new ArrayList<>(favoritePaths));
        settingsService.saveSettings(settings);
    }

    @FXML
    private void addToFavorites() {
        if (favoritePaths.contains(basePath)) {
            showAlert(Alert.AlertType.INFORMATION, "Эта папка уже в избранном!");
            return;
        }

        favoritePaths.add(basePath);
        favoritesList.getItems().add(basePath);
        saveFavorites();
        showAlert(Alert.AlertType.INFORMATION, "Папка добавлена в избранное!");
        historyService.addEntry("ADD_FAVORITE", basePath);
    }

    @FXML
    private void removeFromFavorites() {
        String selected = favoritesList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Выберите папку для удаления!");
            return;
        }

        favoritePaths.remove(selected);
        favoritesList.getItems().remove(selected);
        saveFavorites();
        showAlert(Alert.AlertType.INFORMATION, "Папка удалена из избранного!");
        historyService.addEntry("REMOVE_FAVORITE", selected);
    }

    @FXML
    private void searchFiles() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Поиск файлов");
        dialog.setHeaderText("Поиск файлов в текущей папке и подпапках");
        dialog.setContentText("Введите имя файла или маску (*. txt):");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(searchPattern -> {
            if (!searchPattern.trim().isEmpty()) {
                List<File> foundFiles = searchFilesRecursive(new File(basePath), searchPattern.trim());
                
                if (foundFiles.isEmpty()) {
                    showAlert(Alert.AlertType.INFORMATION, "Файлы не найдены!");
                } else {
                    showSearchResults(foundFiles, searchPattern);
                }
                historyService.addEntry("SEARCH", searchPattern);
            }
        });
    }

    private List<File> searchFilesRecursive(File directory, String pattern) {
        List<File> results = new ArrayList<>();
        
        File[] files = directory.listFiles();
        if (files == null) return results;

        String regex = pattern.replace("*", ".*").replace("?", ".").toLowerCase();
        
        for (File file : files) {
            if (file.isDirectory()) {
                results.addAll(searchFilesRecursive(file, pattern));
            } else {
                if (file.getName().toLowerCase().matches(regex)) {
                    results.add(file);
                }
            }
        }
        
        return results;
    }

    private void showSearchResults(List<File> files, String pattern) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Результаты поиска");
        alert.setHeaderText("Найдено файлов: " + files.size() + " (шаблон: " + pattern + ")");
        
        StringBuilder content = new StringBuilder();
        int limit = Math.min(files.size(), 50);
        
        for (int i = 0; i < limit; i++) {
            File file = files.get(i);
            content.append(file.getAbsolutePath()).append("\n");
        }
        
        if (files.size() > 50) {
            content.append("\n... и ещё ").append(files.size() - 50).append(" файлов");
        }
        
        TextArea textArea = new TextArea(content.toString());
        textArea.setEditable(false);
        textArea.setWrapText(false);
        textArea.setPrefSize(600, 400);
        
        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    @FXML
    private void analyzeFolderSize() {
        File currentDir = new File(basePath);
        
        Alert progressAlert = new Alert(Alert.AlertType.INFORMATION);
        progressAlert.setTitle("Анализ папки");
        progressAlert.setHeaderText("Подождите, идёт анализ...");
        progressAlert.setContentText("Анализируется: " + currentDir.getName());
        progressAlert.show();

        new Thread(() -> {
            long totalSize = 0;
            int fileCount = 0;
            int folderCount = 0;
            
            List<File> allFiles = getAllFilesRecursive(currentDir);
            
            for (File file : allFiles) {
                if (file.isFile()) {
                    totalSize += file.length();
                    fileCount++;
                } else if (file.isDirectory()) {
                    folderCount++;
                }
            }
            
            final long finalSize = totalSize;
            final int finalFileCount = fileCount;
            final int finalFolderCount = folderCount;

            javafx.application.Platform.runLater(() -> {
                progressAlert.close();
                
                Alert resultAlert = new Alert(Alert.AlertType.INFORMATION);
                resultAlert.setTitle("Анализ папки");
                resultAlert.setHeaderText("Результаты анализа папки: " + currentDir.getName());
                
                String sizeStr = formatBytes(finalSize);
                String content = String.format(
                    "Путь: %s\n\n" +
                    "Всего файлов: %d\n" +
                    "Всего папок: %d\n" +
                    "Общий размер: %s\n\n" +
                    "Средний размер файла: %s",
                    currentDir.getAbsolutePath(),
                    finalFileCount,
                    finalFolderCount,
                    sizeStr,
                    finalFileCount > 0 ? formatBytes(finalSize / finalFileCount) : "0 Б"
                );
                
                resultAlert.setContentText(content);
                resultAlert.showAndWait();
            });
            
            historyService.addEntry("ANALYZE_FOLDER", basePath);
        }).start();
    }

    private List<File> getAllFilesRecursive(File directory) {
        List<File> results = new ArrayList<>();
        
        File[] files = directory.listFiles();
        if (files == null) return results;
        
        for (File file : files) {
            results.add(file);
            if (file.isDirectory()) {
                results.addAll(getAllFilesRecursive(file));
            }
        }
        
        return results;
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " Б";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f КБ", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f МБ", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f ГБ", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }

    @FXML
    private void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("О программе");
        alert.setHeaderText("Файловый менеджер v1.0");
        
        String content = "🗂️ Современный файловый менеджер на JavaFX\n\n" +
                "Автор: Егор Кузнецов\n" +
                "Версия: 1.0.0\n" +
                "Дата: Март 2026\n\n" +
                "Возможности:\n" +
                "• Навигация по файловой системе\n" +
                "• Копирование, вырезание, вставка\n" +
                "• Создание и переименование файлов/папок\n" +
                "• Множественное выделение\n" +
                "• Поиск файлов\n" +
                "• Анализ размера папок\n" +
                "• Избранное\n" +
                "• История операций\n" +
                "• Текстовый редактор\n\n" +
                "© 2026 Все права защищены";
        
        alert.setContentText(content);
        alert.getDialogPane().setPrefWidth(450);
        alert.showAndWait();
    }

    @FXML
    private void showDocumentation() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Документация");
        alert.setHeaderText("Горячие клавиши и возможности");
        
        String content = "📖 ОСНОВНЫЕ ОПЕРАЦИИ:\n\n" +
                "Навигация:\n" +
                "• Двойной клик на папку - открыть\n" +
                "• Двойной клик на файл - редактировать\n" +
                "• Кнопки Назад/Вперёд/Вверх/Домой\n" +
                "• Breadcrumb - клик на сегмент пути\n\n" +
                "Файловые операции:\n" +
                "• Правка → Копировать/Вырезать/Вставить\n" +
                "• Файл → Создать файл/папку\n" +
                "• Правка → Переименовать\n" +
                "• Правка → Удалить\n\n" +
                "Выделение:\n" +
                "• Ctrl + клик - множественный выбор\n" +
                "• Shift + клик - выбор диапазона\n\n" +
                "Инструменты:\n" +
                "• Поиск файлов (с маской *.txt)\n" +
                "• Анализ размера папки\n" +
                "• Сортировка по колонкам (клик на заголовок)\n\n" +
                "Избранное:\n" +
                "• Кнопка \"+ Добавить\" - добавить папку\n" +
                "• Двойной клик - быстрый переход\n\n" +
                "Контекстное меню:\n" +
                "• Правая кнопка мыши на файле";
        
        TextArea textArea = new TextArea(content);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefSize(500, 450);
        
        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    @FXML
    private void showSettings() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Настройки");
        alert.setHeaderText("Настройки файлового менеджера");
        
        String content = "⚙️ ТЕКУЩИЕ НАСТРОЙКИ:\n\n" +
                "Отображение:\n" +
                "• Показывать скрытые файлы: " + (settings.isShowHiddenFiles() ? "Да" : "Нет") + "\n" +
                "• Цветовая схема: Синяя\n" +
                "• Размер окна: " + settings.getWindowWidth() + "x" + settings.getWindowHeight() + "\n\n" +
                "Пути:\n" +
                "• Домашняя папка: " + System.getProperty("user.home") + "\n" +
                "• Текущая папка: " + basePath + "\n" +
                "• Избранных папок: " + favoritePaths.size() + "\n\n" +
                "История:\n" +
                "• Записей в истории: " + historyService.loadHistory().size() + "\n" +
                "• История навигации: " + navigationHistory.size() + " путей\n\n" +
                "Примечание:\n" +
                "Для изменения настроек используйте соответствующие пункты меню.\n" +
                "Например, \"Вид → Показывать скрытые файлы\" для переключения отображения.";
        
        TextArea textArea = new TextArea(content);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefSize(500, 400);
        
        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    @FXML
    private void openHexEditor() {
        FileItem selected = tableFiles.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Выберите файл для просмотра в Hex-редакторе!");
            return;
        }

        if (selected.isDirectory()) {
            showAlert(Alert.AlertType.WARNING, "Нельзя открыть папку в Hex-редакторе!");
            return;
        }

        File file = selected.getFile();

        if (file.length() > 1024 * 1024) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Предупреждение");
            confirmAlert.setHeaderText("Файл слишком большой");
            confirmAlert.setContentText("Размер файла больше 1 МБ. Показать первые 1 МБ?");
            
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }
        }

        try {
            String hexContent = readFileAsHex(file);
            showHexEditor(file.getName(), hexContent);
            historyService.addEntry("HEX_VIEW", file.getPath());
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка при чтении файла: " + e.getMessage());
        }
    }

    private String readFileAsHex(File file) throws IOException {
        StringBuilder hexOutput = new StringBuilder();
        
        try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
            byte[] buffer = new byte[16];
            int bytesRead;
            long offset = 0;
            int maxBytes = 1024 * 1024;
            int totalRead = 0;
            
            while ((bytesRead = fis.read(buffer)) != -1 && totalRead < maxBytes) {
                hexOutput.append(String.format("%08X  ", offset));

                for (int i = 0; i < 16; i++) {
                    if (i < bytesRead) {
                        hexOutput.append(String.format("%02X ", buffer[i]));
                    } else {
                        hexOutput.append("   ");
                    }
                    
                    if (i == 7) {
                        hexOutput.append(" ");
                    }
                }
                
                hexOutput.append(" |");

                for (int i = 0; i < bytesRead; i++) {
                    char c = (char) buffer[i];
                    if (c >= 32 && c <= 126) {
                        hexOutput.append(c);
                    } else {
                        hexOutput.append('.');
                    }
                }
                
                hexOutput.append("|\n");
                offset += bytesRead;
                totalRead += bytesRead;
            }
            
            if (totalRead >= maxBytes) {
                hexOutput.append("\n... показаны первые 1 МБ из ").append(formatBytes(file.length()));
            }
        }
        
        return hexOutput.toString();
    }

    private void showHexEditor(String fileName, String hexContent) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Hex-редактор");
        alert.setHeaderText("Файл: " + fileName);
        
        TextArea textArea = new TextArea(hexContent);
        textArea.setEditable(false);
        textArea.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 12px;");
        textArea.setPrefSize(800, 600);
        
        alert.getDialogPane().setContent(textArea);
        alert.getDialogPane().setPrefWidth(850);
        alert.showAndWait();
    }
}
