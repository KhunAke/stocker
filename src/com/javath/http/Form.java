package com.javath.http;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class Form {

	private final List<NameValuePair> params;
	
	public Form() {
		this.params = new ArrayList<NameValuePair>();
	}
	
	public Form add(final String name, final String value) {
        params.add(new BasicNameValuePair(name, value));
        return this;
    }
	
	public Form delete(final String name) {
		for (int index = 0; index < params.size(); index++) {
			NameValuePair element = params.get(index);
			if (element.getName() == name) {
				params.remove(index);
				break;
			}
		}
        return this;
    }
	
	public Form set(final String name, final String value) {
		boolean modify = false;
		for (int index = 0; index < params.size(); index++) {
			NameValuePair element = params.get(index);
			if (element.getName() == name) {
				params.remove(index);
				params.add(index, new BasicNameValuePair(name, value));
				modify = true;
				break;
			}
		}
		if (!modify)
			params.add(new BasicNameValuePair(name, value));
        return this;
    }
	
	public List<NameValuePair> build() {
        return new ArrayList<NameValuePair>(this.params);
    }
	
}
