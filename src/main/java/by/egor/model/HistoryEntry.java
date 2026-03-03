package by.egor.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HistoryEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String action;
    private final String fileName;
    private final LocalDateTime timestamp;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    public HistoryEntry(String action, String fileName) {
        this.action = action;
        this.fileName = fileName;
        this.timestamp = LocalDateTime.now();
    }

    public String getAction() {
        return action;
    }

    public String getFileName() {
        return fileName;
    }

    public String getTimestamp() {
        return timestamp.format(FORMATTER);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s: %s",
                timestamp.format(FORMATTER), action, fileName);
    }
}
