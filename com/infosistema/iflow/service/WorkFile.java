package com.infosistema.iflow.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class WorkFile extends File{
	
	private static final long serialVersionUID = 4618960928612419968L;
	
	private String fid; 
	private String pid;
	private String subpid;
	private String docid;
	private String variable;
	private String filename;
	private Integer filelength;
	
	public static File createClientSideWorkFile(String fid, String pid,
			String subpid, String docid, String variable, InputStream in, String filename) throws IOException{
		
		if (null == filename || "".equals(filename)) {
			filename = "filename.tmp";
		}
		
		File tmpFile = createTempFile("WorkFile", null);	
		tmpFile.deleteOnExit();
		Integer filelengthAux=0;
	    if(null != in) {
	        FileOutputStream fout = null;
	        try {
	          fout = new FileOutputStream(tmpFile);
	          byte[] b = new byte[8192];
	          int r = 0;
	          while ((r = in.read(b)) != -1){
	            fout.write(b, 0, r);
	            filelengthAux += r;
	          }
	        } finally {
	          if(fout != null) {
	            try {
	              fout.close();
	            } catch (IOException e) {}
	          }
	        }
	      }
	    
	    WorkFile f = new WorkFile(tmpFile.getAbsolutePath());
	    f.setDocid(docid);
	    f.setFid(fid);
	    f.setPid(pid);
	    f.setSubpid(subpid);
	    f.setVariable(variable);
	    f.setFilename(filename);
	    f.setFilelength(filelengthAux);
	    
		return f;		
	}

	public String getFid() {
		return fid;
	}

	public void setFid(String fid) {
		this.fid = fid;
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public String getSubpid() {
		return subpid;
	}

	public void setSubpid(String subpid) {
		this.subpid = subpid;
	}

	public String getDocid() {
		return docid;
	}

	public void setDocid(String docid) {
		this.docid = docid;
	}

	public String getVariable() {
		return variable;
	}

	public void setVariable(String variable) {
		this.variable = variable;
	}

	public WorkFile(File parent, String child) {
		super(parent, child);
		// TODO Auto-generated constructor stub
	}

	public WorkFile(String parent, String child) {
		super(parent, child);
		// TODO Auto-generated constructor stub
	}

	public WorkFile(String pathname) {
		super(pathname);
		// TODO Auto-generated constructor stub
	}

	public WorkFile(URI uri) {
		super(uri);
		// TODO Auto-generated constructor stub
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public Integer getFilelength() {
		return filelength;
	}

	public void setFilelength(Integer filelength) {
		this.filelength = filelength;
	}


	

}
