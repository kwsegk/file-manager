package by.egor.controller;

import by.egor.model.FileItem;
import by.egor.service.FileService;
import by.egor.service.HistoryService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import static by.egor.service.Alert.showAlert;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class TextEditorController {
    private FileItem file;
    private FileService service;
    private HistoryService historyService;

    @FXML
    private Button btnSave;

    @FXML
    private TextArea textArea;

    public void setFile(FileItem file) {
        this.file = file;
    }

    public void setService(FileService service) {
        this.service = service;
    }

    public void setHistoryService(HistoryService historyService) {
        this.historyService = historyService;
    }

    @FXML
    public void initialize(){

    }

    public void loadFileContent() {
        if (file == null || service == null) {
            textArea.setText("Ошибка: Не удалось загрузить файл.");
            return;
        }

        if (file.isDirectory()) {
            textArea.setText("Это папка, её нельзя открыть в текстовом редакторе.");
            textArea.setEditable(false);
        } else {
            StringBuilder content = service.readFile(file);
            textArea.setText(content.toString());
        }


    }

    public void saveBtn(){
        service.writeInFile(file, textArea.getText());
        Stage stage = (Stage) btnSave.getScene().getWindow();
        showAlert(Alert.AlertType.CONFIRMATION,"Файл успешно сохранен");
        historyService.addEntry("SAVE", file.getName());
        stage.close();
    }
}
