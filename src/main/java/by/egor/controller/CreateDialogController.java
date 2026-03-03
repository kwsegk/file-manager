package by.egor.controller;

import by.egor.service.FileService;
import by.egor.service.HistoryService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.File;

import static by.egor.service.Alert.showAlert;

public class CreateDialogController {
    @FXML
    private Button btnCreate;
    @FXML
    private Button btnCancel;
    @FXML
    private TextField txtName;
    @FXML
    private Label labelFolder;

    private String pathName = "/Users/egorkuznetsov/Desktop/FileManagerTest";

    private FileService service;
    private HistoryService historyService;

    private MainController controller;


    @FXML
    public void initialize(){
        labelFolder.setText(pathName + "/");
    }
    public void setPathName(String pathName) {
        this.pathName = pathName;
    }

    public void setService(FileService service) {
        this.service = service;
    }

    public void setController(MainController controller) {
        this.controller = controller;
    }

    public void setHistoryService(HistoryService historyService) {
        this.historyService = historyService;
    }

    @FXML
    private void create(){
        if (txtName.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Введите имя файла");
            return;
        }

        if (txtName.getText().matches(".*[/\\\\:*?\"<>|].*")) {
            showAlert(Alert.AlertType.WARNING,
                    "Имя содержит запрещённые символы: / \\ : * ? \" < > |");
            return;
        }

        File file = new File(pathName + "/" + txtName.getText() + ".txt");
        if (file.exists()) {
            showAlert(Alert.AlertType.WARNING, "Файл уже существует");
            return;
        }
        try{
            service.createNewFile(file.getPath());
            controller.reloadTable();
            Stage stage = (Stage) btnCreate.getScene().getWindow();
            showAlert(Alert.AlertType.INFORMATION, "Файл успешно создан!");
            historyService.addEntry("CREATE", "Создание файла " + file.getPath());
            stage.close();
            service.writeLog("Файл " + txtName.getText() + " успешно создан");
        } catch (RuntimeException e){
            showAlert(Alert.AlertType.ERROR, "Ошибка при создании файла");
        }


    }
    @FXML
    private void cancel(){
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

}
