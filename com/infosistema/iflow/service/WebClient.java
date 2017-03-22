package com.infosistema.iflow.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.sun.xml.internal.messaging.saaj.packaging.mime.internet.ContentDisposition;
import com.sun.xml.internal.messaging.saaj.packaging.mime.internet.ParseException;


public class WebClient {

	public static File downloadFile(String documentServiceUrl, String cookie, String fid, String pid,
			String subpid, String docid, String variable) throws IOException, ParseException {
		URL url = new URL(documentServiceUrl + "?docid=" + docid + "&pid=" + pid + "&subpid=" + subpid + "&variable="
				+ variable + "&flowid=" + fid);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoInput(true);
		conn.setDoOutput(false);
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Cookie", cookie);
		conn.connect();
		ContentDisposition contentDisposition = null;
		String sContentDisp = conn.getHeaderField("Content-Disposition");
		contentDisposition = new ContentDisposition(sContentDisp);
		String filename = "filename.tmp";
		if (null != contentDisposition) {
			filename = contentDisposition.getParameter("filename");
		}
		
		return WorkFile.createClientSideWorkFile(fid, pid, subpid, docid, variable, conn.getInputStream(), filename);
//		String[] auxFilename = filename.split(".");
//		String suffix="filename";
//		if(auxFilename.length>=1)
//			suffix = auxFilename[0];
//		String prefix="tmp";
//		if(auxFilename.length>=2)
//			prefix = auxFilename[1];
//		
//		InputStream in = conn.getInputStream();
//		File f = new File(filename);
//		f.deleteOnExit();
//	    if(null != in) {
//	        FileOutputStream fout = null;
//	        try {
//	          fout = new FileOutputStream(f);
//	          byte[] b = new byte[8192];
//	          int r = 0;
//	          while ((r = in.read(b)) != -1)
//	            fout.write(b, 0, r);
//	        } finally {
//	          if(fout != null) {
//	            try {
//	              fout.close();
//	            } catch (IOException e) {}
//	          }
//	        }
//	      }
//	    
//		return f;
	}
	
	public static byte[] downloadRubric(String rubricServiceUrl, String cookie) throws IOException{
		URL url = new URL(rubricServiceUrl);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoInput(true);
		conn.setDoOutput(false);
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Cookie", cookie);
		conn.connect();
		
		return IOUtils.toByteArray(conn.getInputStream());		
	}
	
	public static void uploadRubric(String rubricServiceUrl, String cookie, byte[] rubric, String filename) throws IOException{
		// JSESSIONID=C90024FB7BE5CC6DCE694E06AD6700ED
		// flowid=42, encryptType=false, fileid=9094};
//		HashMap<String,String> params = new HashMap<>();
//		params.put("docid", file.getDocid());
//		params.put("signatureType", "PDF");
//		params.put("DOCUMENTBASEURL", uploadUrl);
//		params.put("RUBRICAR", "false");
//		if(file.getDocid()==null || "".equals(file.getDocid()))
//			params.put("update", "false");
//		else
//			params.put("update", "true");
//		params.put("pid", file.getPid());
//		params.put("NUMASS", numass);
//		params.put("file", file.getDocid());
//		params.put("subpid", file.getSubpid());
//		params.put("variable", file.getVariable());
//		params.put("action", "modifyFile");
//		params.put("flowid", file.getFid());
//		params.put("encriptType", "false");
//		params.put("fileid", file.getDocid());		
		
		String retObj = null;
		URL url = new URL(rubricServiceUrl);
		// upload document
		final String lineEnd = "\r\n"; //$NON-NLS-1$
		final String twoHyphens = "--"; //$NON-NLS-1$
		final String boundary = "---------------------------" + System.currentTimeMillis(); //$NON-NLS-1$

		HttpURLConnection conn = null;
		DataOutputStream dos = null;
		InputStream inStream = null;

		int pos = 0;

		byte[] buffer = new byte[8192];
		int r;

		try {
			// ------------------ CLIENT REQUEST

			// Open a HTTP connection to the URL
			conn = (HttpURLConnection) url.openConnection();
			// Allow Inputs
			conn.setDoInput(true);
			// Set Internal Buffer to 0
			conn.setChunkedStreamingMode(0);
			// Allow Outputs
			conn.setDoOutput(true);
			// Don't use a cached copy.
			conn.setUseCaches(false);
			// Use a post method.
			conn.setRequestMethod("POST"); //$NON-NLS-1$
			conn.setRequestProperty("Connection", "Keep-Alive"); //$NON-NLS-1$ //$NON-NLS-2$
			conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary); //$NON-NLS-1$ //$NON-NLS-2$

			// set cookies
			if (null != cookie && cookie.trim().length() > 0)
				conn.setRequestProperty("Cookie", cookie); //$NON-NLS-1$

			dos = new DataOutputStream(conn.getOutputStream());

			// send process identification
//			if (params != null && !params.isEmpty()) {
//				for (Map.Entry<String, String> entry : params.entrySet()) {
//					String pname = entry.getKey();
//					String pvalue = entry.getValue();
//					// ignore null params
//					if (null == pname || "".equals(pname) || null == pvalue) //$NON-NLS-1$
//						continue;
//
//					// mark as update
//					dos.writeBytes(twoHyphens + boundary + lineEnd);
//					dos.writeBytes("Content-Disposition: form-data; name=\"" + pname + "\"" + lineEnd); //$NON-NLS-1$ //$NON-NLS-2$
//					dos.writeBytes("Content-Type: text/plain; charset=UTF-8" + lineEnd); //$NON-NLS-1$
//					dos.writeBytes("Content-Transfer-Encoding: 8bit" + lineEnd); //$NON-NLS-1$
//					dos.writeBytes(lineEnd);
//					dos.write(pvalue.getBytes("UTF-8")); //$NON-NLS-1$
//					dos.writeBytes(lineEnd);
//				}
//			}

			// Upload file
			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"" + "file" + "\"; filename=\"" //$NON-NLS-1$ //$NON-NLS-2$
					+ URLEncoder.encode(filename, "UTF-8") + "\"" + lineEnd); //$NON-NLS-1$
			dos.writeBytes("Content-Length: " + rubric.length + lineEnd); //$NON-NLS-1$
			dos.writeBytes(lineEnd);

			// output file
			InputStream in = new ByteArrayInputStream(rubric);
		    while ((r = in.read(buffer)) > 0)
		        dos.write(buffer, 0, r);
		      
			// send multipart form data necesssary after file data...
			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

			dos.flush();
			dos.close();

			in.close();
			in = null;			

		} finally {}
	}

	public static String uploadFile(String uploadUrl, String cookie, WorkFile file, String numass) throws IOException {
		// JSESSIONID=C90024FB7BE5CC6DCE694E06AD6700ED
		// flowid=42, encryptType=false, fileid=9094};
		HashMap<String,String> params = new HashMap<>();
		params.put("docid", file.getDocid());
		params.put("signatureType", "PDF");
		params.put("DOCUMENTBASEURL", uploadUrl);
		params.put("RUBRICAR", "false");
		if(file.getDocid()==null || "".equals(file.getDocid()))
			params.put("update", "false");
		else
			params.put("update", "true");
		params.put("pid", file.getPid());
		params.put("NUMASS", numass);
		params.put("file", file.getDocid());
		params.put("subpid", file.getSubpid());
		params.put("variable", file.getVariable());
		params.put("action", "modifyFile");
		params.put("flowid", file.getFid());
		params.put("encriptType", "false");
		params.put("fileid", file.getDocid());		
		
		String retObj = null;
		URL url = new URL(uploadUrl);
		// upload document
		final String lineEnd = "\r\n"; //$NON-NLS-1$
		final String twoHyphens = "--"; //$NON-NLS-1$
		final String boundary = "---------------------------" + System.currentTimeMillis(); //$NON-NLS-1$

		HttpURLConnection conn = null;
		DataOutputStream dos = null;
		InputStream inStream = null;

		int pos = 0;

		byte[] buffer = new byte[8192];
		int r;

		try {
			// ------------------ CLIENT REQUEST

			// Open a HTTP connection to the URL
			conn = (HttpURLConnection) url.openConnection();
			// Allow Inputs
			conn.setDoInput(true);
			// Set Internal Buffer to 0
			conn.setChunkedStreamingMode(0);
			// Allow Outputs
			conn.setDoOutput(true);
			// Don't use a cached copy.
			conn.setUseCaches(false);
			// Use a post method.
			conn.setRequestMethod("POST"); //$NON-NLS-1$
			conn.setRequestProperty("Connection", "Keep-Alive"); //$NON-NLS-1$ //$NON-NLS-2$
			conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary); //$NON-NLS-1$ //$NON-NLS-2$

			// set cookies
			if (null != cookie && cookie.trim().length() > 0)
				conn.setRequestProperty("Cookie", cookie); //$NON-NLS-1$

			dos = new DataOutputStream(conn.getOutputStream());

			// send process identification
			if (params != null && !params.isEmpty()) {
				for (Map.Entry<String, String> entry : params.entrySet()) {
					String pname = entry.getKey();
					String pvalue = entry.getValue();
					// ignore null params
					if (null == pname || "".equals(pname) || null == pvalue) //$NON-NLS-1$
						continue;

					// mark as update
					dos.writeBytes(twoHyphens + boundary + lineEnd);
					dos.writeBytes("Content-Disposition: form-data; name=\"" + pname + "\"" + lineEnd); //$NON-NLS-1$ //$NON-NLS-2$
					dos.writeBytes("Content-Type: text/plain; charset=UTF-8" + lineEnd); //$NON-NLS-1$
					dos.writeBytes("Content-Transfer-Encoding: 8bit" + lineEnd); //$NON-NLS-1$
					dos.writeBytes(lineEnd);
					dos.write(pvalue.getBytes("UTF-8")); //$NON-NLS-1$
					dos.writeBytes(lineEnd);
				}
			}

			// Upload file
			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"" + "file" + "\"; filename=\"" //$NON-NLS-1$ //$NON-NLS-2$
					+ URLEncoder.encode(file.getFilename(), "UTF-8") + "\"" + lineEnd); //$NON-NLS-1$
			dos.writeBytes("Content-Length: " + file.getFilelength() + lineEnd); //$NON-NLS-1$
			dos.writeBytes(lineEnd);

			// output file
			InputStream in = new FileInputStream(file);
		    while ((r = in.read(buffer)) > 0)
		        dos.write(buffer, 0, r);
		      
			// send multipart form data necesssary after file data...
			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

			dos.flush();
			dos.close();

			in.close();
			in = null;

			// Read response
			inStream = null;
			ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
			try {
				inStream = conn.getInputStream(); // $NON-NLS-1$
				while ((r = inStream.read(buffer)) > 0) {
					dataStream.write(buffer, 0, r);
				}
				inStream.close();

				String response = dataStream.toString("UTF-8"); //$NON-NLS-1$
//				log.debug("Got response " + response + " for file " + f.getName()); //$NON-NLS-1$ //$NON-NLS-2$
				retObj = response;

			} catch (IOException ioex) {
//				log.error("From (ServerResponse): " + ioex, ioex); //$NON-NLS-1$
			} finally {
				try {
					if (in != null)
						in.close();
				} catch (IOException e) {
				}
				try {
					if (inStream != null)
						inStream.close();
				} catch (IOException e) {
				}
			}

		} finally {}
		return retObj;
	}
}
