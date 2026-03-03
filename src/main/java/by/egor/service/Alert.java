package by.egor.service;

public class Alert {
    public static void showAlert(javafx.scene.control.Alert.AlertType type, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
