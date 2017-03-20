package Protocol;

import java.io.Serializable;

public class Chunk implements Serializable{
	 public String fileID;
	 public int chunkNo;
	 public int replDeg;
     public byte[] data;
     
     public Chunk(String fileId, int chunkNo, int replDeg, byte[] data) {
         this.fileID = fileId;
         this.chunkNo = chunkNo;
         this.replDeg = replDeg;
         this.data = data;
     }
     
     
     
     public String getFileId() {
		return fileID;
	}



	public int getChunkNo() {
		return chunkNo;
	}



	public int getReplDeg() {
		return replDeg;
	}



	public byte[] getData() {
		return data;
	}



	@Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;

         Chunk chunk = (Chunk) o;

         if (getChunkNo() != chunk.getChunkNo()) return false;
         return getFileId().equals(chunk.getFileId());

     }
}
