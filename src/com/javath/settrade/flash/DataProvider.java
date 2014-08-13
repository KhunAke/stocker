package com.javath.settrade.flash;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.codec.binary.StringUtils;

import com.javath.util.Instance;
import com.javath.util.ObjectException;

public class DataProvider extends Instance {
	
	private String[][][] data; // response, service, field
	
	public int getNumberOfServices() {
		// response = 1, service = 0, field = 0
		if (data.length >= 2)
			return Integer.valueOf(data[1][0][0]);
		else
			return 0;
	}
	
	public int getOrderOfServiceName(String name) {
		// response = 1, service = 0, field = 0
		int services = getNumberOfServices();
		for (int service = 0; service < services; service++) {
			if (data[2 + service][1][0].equals(name))
				return service;
		}
		throw new ObjectException("Service not found."); 
	}

	public int getNumberOfResults(int service) {
		// response = 2, service = 3, field = 0
		if (data.length >= 3)
			return Integer.valueOf(data[2 + service][3][0]);
		else
			return 0;
	}
	
	public String[] getResult(int service, int result) {
		// response = 2, service = 3, field = ?
		if (data.length >= 3)
			return data[2 + service][5 + result];
		else
			return null;
	}
	
	public String get(int response, int service, int field) {
		return data[response][service][field].trim();
	}
	
	public DataProvider read(InputStream input) {
		try {
			byte[] buffer = new byte[input.available()];
			input.read(buffer);
			String result = StringUtils.newStringUsAscii(buffer).trim();
			FINEST("%s", result);
			read(result);
			
			if (get(0, 0, 0).equals("T"))
				return this;
			else if (get(0, 0, 0).equals("F"))
				try {
					throw new ObjectException("%s",get(0,0,1));
				} catch (ArrayIndexOutOfBoundsException e) {
					throw new ObjectException(result);
				}
			
			if (getNumberOfServices() > 0)
				if (data[2][2][0].equals("F"))
					throw new ObjectException("%s", data[2][5][0].trim());
			return this;
		} catch (IOException e) {
			SEVERE(e);
			return null;
		}
	}
	
	public void read(String input) {
		String[] responses = input.split("[~]");
		data = new String[responses.length][][];
		for (int response = 0; response < responses.length; response++) {
			String[] services = responses[response].split("[\\^]");
			data[response] = new String[services.length][];
			for (int service = 0; service < services.length; service++) {
				String[] fields = services[service].trim().split("[|]");
				data[response][service] = new String[fields.length];
				for (int field = 0; field < fields.length; field++) {
					data[response][service][field] = fields[field];
				}
			}
		}
	}
	
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		for (int response = 0; response < data.length; response++) {
			buffer.append('[');
			for (int service = 0; service < data[response].length; service++) {
				buffer.append('[');
				for (int field = 0; field < data[response][service].length; field++) {
					buffer.append(data[response][service][field] + ",");
				}
				buffer.replace(buffer.length() - 1, buffer.length(), "");
				buffer.append(']');
			}
			buffer.append(']');
		}
		return buffer.toString();
	}

}
