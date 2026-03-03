module by.egor.demo2 {
    // JavaFX модули
    requires javafx.controls;
    requires javafx.fxml;

    // Дополнительные библиотеки (если используешь)
    requires com.dlsc.formsfx;  // Можно удалить, если не нужен

    // ✅ ВАЖНО: Открыть все пакеты для JavaFX и рефлексии
    opens by.egor to javafx.fxml;                    // Main пакет
    opens by.egor.controller to javafx.fxml;         // Контроллеры
    opens by.egor.model to javafx.base;              // Модели (для TableView)

    // Экспорт (для использования вне модуля)
    exports by.egor;
    exports by.egor.controller;
    exports by.egor.model;
}