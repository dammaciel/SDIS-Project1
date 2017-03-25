package FileSystem;

import java.util.HashMap;

public class FileSystem {
	private HashMap<String, File> files;
	
	private static FileSystem instance = null;

    public static FileSystem instance() {

        if(instance == null){
            synchronized (FileSystem.class) {
                if(instance == null){
                    instance = new FileSystem();
                }
            }
        }

        return instance;
    }

    private FileSystem() {
        files = new HashMap<>();
    }



    public void addFile(String fileId, File file) {
        files.put(fileId, file);
    }

    public boolean hasFile(String fileId) {
        return files.containsKey(fileId);
    }

    public File getFile(String fileId) {
        return files.get(fileId);
    }
}
