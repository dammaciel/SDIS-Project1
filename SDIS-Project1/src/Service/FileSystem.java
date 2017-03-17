package Service;

import java.io.File;

public class FileSystem {
	private int peerId;
	private static String DB_FOLDER, RESTORED_FOLDER;
	
	 public FileSystem(int _peerId) {
	        peerId = _peerId;
	        
	        //inserir caminho 
	        //DB_FOLDER = ;
	        //RESTORED_FOLDER = ;

	        File tmp = new File(RESTORED_FOLDER);
	        tmp.mkdirs();
	    }
	 
	 public static String getDBPath(String name) {
	        return DB_FOLDER+"/"+name;
	    }

	 public static String getRestoredPath(String name) {
	        return RESTORED_FOLDER+"/"+name;
	    }
}
