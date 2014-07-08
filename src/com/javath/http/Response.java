package com.javath.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;

import com.javath.util.Instance;
import com.javath.util.ObjectException;

public class Response extends Instance {
	
	private Header[] headers;
	private Header[] locations;
	private StatusLine status;
	private final ByteArrayEntity entity;
	// Content-Type
	private String mime;
	private String charset;
	
	private String filename;
	
	public Response(HttpResponse http_response) {
		try {
			headers = http_response.getAllHeaders();
			locations = http_response.getHeaders("Location");
			status = http_response.getStatusLine();
			HttpEntity entity = http_response.getEntity();
			this.entity = new ByteArrayEntity(EntityUtils.toByteArray(entity),
			        ContentType.getOrDefault(entity));
			parseContentType(this.entity.getContentType());
		} catch (UnsupportedCharsetException e) {
			throw new ObjectException(e);
		} catch (ParseException e) {
			throw new ObjectException(e);
		} catch (IOException e) {
			throw new ObjectException(e);
		}
	}
	
	public String getFilename() {
		return filename;
	}

	protected Response setFilename(String filename) {
		this.filename = filename;
		return this;
	}

	// Information from ByteArrayEntity
	private void parseContentType(Header header) {
		if (header.getName() == "Content-Type") {
			String type = header.getValue();
			int delimiter = type.indexOf(';');
			if (delimiter == -1)
				mime = type;
			else {
				mime = type.substring(0, delimiter);
				delimiter = type.indexOf('=', delimiter);
				charset = type.substring(delimiter + 1);
			}
		}
	}
	public String getMime() {
		return mime;
	}
	public String getCharset() {
		if (charset == null)
			return Charset.defaultCharset().toString();
		else
			return charset;
	}
	public InputStream getContent() {
		return entity.getContent();
	}
	public long getContentLength() {
		return entity.getContentLength();
	}
	
	// Information from StatusLine
	public String getProtocolVersion() {
		return status.getProtocolVersion().toString();
	}
	public int getStatusCode() {
		return status.getStatusCode();
	}
	public String getReasonPhrase() {
		return status.getReasonPhrase();
	}
	public String getStatusLine() {
		return status.toString();
	}
	
	public Header[] getLocations() {
		return locations;
	}
	public void printLocations() {
		for (int index = 0; index < locations.length; index++) {
			Header header = locations[index];
			System.out.println(header);
		}
	}
	public void printHeaders() {
		for (int index = 0; index < headers.length; index++) {
			Header header = headers[index];
			System.out.println(header);
		}
	}
	public void printContent() {
		if (entity != null) {
			try {
				InputStream input_stream = getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(input_stream, Charset.forName(getCharset())));
				String line;
				while ((line = reader.readLine()) != null) {
				    System.out.println(line);
				}
			} catch (IOException e) {
				throw new ObjectException(e);
			}
		}
	}
	public void print() {
		System.out.println(getStatusLine());
		printHeaders();
		System.out.println();
		printContent();
	}

	public String save(String file) {
		return save(new File(file));
	}
	public String save(File file) {
		String filename = file.getAbsolutePath();
		// Add extension with sequence number when exists file 
		int sequence = 0;
		while (file.exists()) {
			sequence += 1;
			int extension = filename.lastIndexOf('.');
			file = new File(filename.substring(0, extension) + "." + sequence +
					filename.substring(extension));
		}
		//
		InputStream input_stream = getContent();
		FileOutputStream output_stream = null;
        try {  
        	output_stream = new FileOutputStream(file);
            byte[] buffer = new byte[2048];
            int counter = 0;
            while ((counter = input_stream.read(buffer)) > 0)  
            	output_stream.write(buffer, 0, counter);
            filename = file.getCanonicalPath(); 
        } catch (FileNotFoundException e) {
			throw new ObjectException(e);
        } catch (IOException e) {
        	throw new ObjectException(e);
		}finally {
        	try {
        		if (input_stream != null)
        			input_stream.close();
        		if (output_stream != null)  
                   	output_stream.close();
			} catch (IOException e) {
				throw new ObjectException(e);
			}
        }
        return filename;
	}

}
