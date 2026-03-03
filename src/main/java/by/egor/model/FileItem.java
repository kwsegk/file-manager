package by.egor.model;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileItem {

    private final String name;
    private final String size;
    private final String type;
    private final String date;
    private final String extension;
    private final File file;
    private final String icon;

    public FileItem(File file) {
        this.file = file;
        this.size = formatSize(file);
        this.date = formatDate(file);

        String fileName = file.getName();

        if (file.isDirectory()) {
            this.type = "Папка";
            this.extension = "";
            this.name = fileName;
            this.icon = "📁";
        } else {
            this.type = "Файл";
            int lastDotIndex = fileName.lastIndexOf('.');
            if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
                this.name = fileName.substring(0, lastDotIndex);
                this.extension = fileName.substring(lastDotIndex + 1);
            } else {
                this.name = fileName;
                this.extension = "";
            }
            this.icon = getIconForExtension(this.extension);
        }
    }

    private String getIconForExtension(String ext) {
        if (ext.isEmpty()) {
            return "📄";
        }
        
        String lower = ext.toLowerCase();

        if (lower.matches("jpg|jpeg|png|gif|bmp|svg|webp|ico")) {
            return "🖼️";
        }
        if (lower.matches("mp4|avi|mkv|mov|wmv|flv|webm")) {
            return "🎬";
        }
        if (lower.matches("mp3|wav|flac|aac|ogg|m4a|wma")) {
            return "🎵";
        }
        if (lower.matches("pdf")) {
            return "📕";
        }
        if (lower.matches("doc|docx|odt")) {
            return "📘";
        }
        if (lower.matches("xls|xlsx|ods")) {
            return "📗";
        }
        if (lower.matches("ppt|pptx|odp")) {
            return "📙";
        }
        if (lower.matches("zip|rar|7z|tar|gz|bz2")) {
            return "📦";
        }
        if (lower.matches("java|class|jar")) {
            return "☕";
        }
        if (lower.matches("py|pyc")) {
            return "🐍";
        }
        if (lower.matches("js|ts|jsx|tsx")) {
            return "📜";
        }
        if (lower.matches("html|htm|css")) {
            return "🌐";
        }
        if (lower.matches("c|cpp|h|hpp")) {
            return "⚙️";
        }
        if (lower.matches("txt|md|log")) {
            return "📝";
        }
        if (lower.matches("exe|app|dmg")) {
            return "⚡";
        }
        
        return "📄";
    }

    private String formatSize(File file) {
        if (file.isDirectory()) {
            return "--";
        }
        long bytes = file.length();
        if (bytes < 1024) {
            return bytes + " Б";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f КБ", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f МБ", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f ГБ", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }

    private String formatDate(File file) {
        Date date = new Date(file.lastModified());
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        return sdf.format(date);
    }

    public String getName() {
        return name;
    }

    public String getSize() {
        return size;
    }

    public String getType() {
        return type;
    }

    public String getDate() {
        return date;
    }

    public String getExtension() {
        return extension;
    }

    public String getIcon() {
        return icon;
    }

    public File getFile() {
        return file;
    }

    public boolean isDirectory() {
        return file.isDirectory();
    }

    public String getPath() {
        return file.getAbsolutePath();
    }

}
