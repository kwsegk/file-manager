package by.egor.controller;

import by.egor.model.HistoryEntry;
import by.egor.service.HistoryService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.List;

public class HistoryController {
    @FXML
    private TableView<HistoryEntry> historyTable;
    @FXML
    private TableColumn<HistoryEntry,String> operationColumn;
    @FXML
    private TableColumn<HistoryEntry,String> filenameColumn;
    @FXML
    private TableColumn<HistoryEntry, String> timestampColumn;

    @FXML
    private Button exitBtn;

    private HistoryService historyService;

    public void setHistoryService(HistoryService historyService) {
        this.historyService = historyService;
    }

    @FXML
    public void initialize(){
        operationColumn.setSortable(false);
        filenameColumn.setSortable(false);
        timestampColumn.setSortable(false);

        operationColumn.setCellValueFactory(new PropertyValueFactory<>("action"));
        filenameColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));

    }

    public void loadHistory() {
        if (historyService != null) {
            List<HistoryEntry> history = historyService.loadHistory();
            historyTable.getItems().setAll(history);
        } else {
            System.err.println("HistoryService не был передан в HistoryController!");
        }
    }

    @FXML
    private void exit(){
        Stage stage = (Stage) exitBtn.getScene().getWindow();
        stage.close();
    }
}
