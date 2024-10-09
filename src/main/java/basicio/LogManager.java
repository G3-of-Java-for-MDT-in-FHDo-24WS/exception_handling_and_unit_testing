package basicio;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import io.github.cdimascio.dotenv.Dotenv;

public class LogManager {
    public static enum LogType {
        CHARGING_STATION,
        ENERGY_SOURCE,
        WHOLE_SYSTEM,
        DEFAULT,
        ARCHIVE
    }

    private static final Dotenv dotenv = Dotenv.load();
    private final Path logDirPath;
    
    private final static Map<LogType, String> logTypeDirMap = Map.of(
    		LogType.CHARGING_STATION, dotenv.get("LOG_DIR_CHARGING_STATION"),
    		LogType.ENERGY_SOURCE, dotenv.get("LOG_DIR_CHARGING_STATION"),
    		LogType.WHOLE_SYSTEM, dotenv.get("LOG_DIR_CHARGING_STATION"),
    		LogType.DEFAULT, dotenv.get("LOG_DIR_CHARGING_STATION"),
    		LogType.ARCHIVE, dotenv.get("LOG_DIR_ARCHIVE"));
    

    public LogManager(LogType logType) {
    	logDirPath = Paths.get(logTypeDirMap.get(logType));

        if (!Files.exists(logDirPath) || !Files.isDirectory(logDirPath)) {
            try {
				Files.createDirectories(logDirPath);
				
				System.out.format("Log manager %s has created!", logType.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
    }

    public void addContentToLog(String fileName, String content) {
        Path logFilePath = logDirPath.resolve(fileName);

        try (BufferedWriter writer = Files.newBufferedWriter(logFilePath, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writer.write(content);
        } catch (IOException e) {
        	System.err.format("Error during adding content to %s: %s", fileName, e.getMessage());
        }
    }

    public void moveLog(String fileName, String targetDir) throws IOException {
        Path logFilePath = logDirPath.resolve(fileName);
        Path targetDirPath = Paths.get(targetDir);
        
        if(!Files.exists(logFilePath)) {
        	throw new FileNotFoundException("The log file does not exist!");
        }

        if (!Files.exists(targetDirPath)) {
            Files.createDirectories(targetDirPath);
        }
        
        Files.move(logFilePath, targetDirPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
    }

    public void deleteLog(String fileName) throws IOException {
        Path logFilePath = logDirPath.resolve(fileName);
        
        Files.deleteIfExists(logFilePath);
    }

    public void archiveLog(String fileName) throws IOException {
        Path logFilePath = logDirPath.resolve(fileName);
        
        if(!Files.exists(logFilePath)) {
        	throw new FileNotFoundException("The log file does not exist!");
        }
        
        Path archivePath = Paths.get(logTypeDirMap.get(LogType.ARCHIVE));
        
        if (!Files.exists(archivePath)) {
            Files.createDirectories(archivePath);
        }
        
        Files.move(logFilePath, archivePath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
    }

    public static String generateLogName(String equipmentName) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        return equipmentName + "_" + date + ".log";
    }
}