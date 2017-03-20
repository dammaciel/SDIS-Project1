package Utils;

import java.io.File;
import java.security.MessageDigest;

public class Utils {
	 public static String FILE_PATH = null;
	 public static String PROTOCOL_VERSION = "1.0";
	 public static int REP_DEGREE_IGNORE = 999999; //default
	 
	 public static String sha256(String base) {
	        try{
	            MessageDigest digest = MessageDigest.getInstance("SHA-256");
	            byte[] hash = digest.digest(base.getBytes("UTF-8"));
	            StringBuffer hexString = new StringBuffer();

	            for (int i = 0; i < hash.length; i++) {
	                String hex = Integer.toHexString(0xff & hash[i]);
	                if(hex.length() == 1) hexString.append('0');
	                hexString.append(hex);
	            }

	            return hexString.toString();
	        } catch(Exception ex){
	            throw new RuntimeException(ex);
	        }
	    }
	 
	 public static String getFileID(File f) {
	        return sha256(f.getName() + f.lastModified());
	    }
}
