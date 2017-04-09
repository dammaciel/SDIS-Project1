package FileSystem;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

public class FileAttributes implements Serializable {
	private String path;
    private String fileId;
    private long size;
    private long lastModified;
    private long lastAccess;
    private long creation;
    
    public FileAttributes(String path, String fileId) throws IOException {
        this.path = path;
        this.fileId = fileId;
        
        Path filePath = Paths.get(path);
        BasicFileAttributes attr = Files.readAttributes(filePath, BasicFileAttributes.class);
        size = attr.size();
        lastModified = attr.lastModifiedTime().toMillis();
        lastAccess = attr.lastAccessTime().toMillis();
        creation = attr.creationTime().toMillis();
    }

	public String getPath() {
		return path;
	}

	public String getFileId() {
		return fileId;
	}

	public long getSize() {
		return size;
	}

	public long getLastModified() {
		return lastModified;
	}

	public long getLastAccess() {
		return lastAccess;
	}

	public long getCreation() {
		return creation;
	}
    
    
}
