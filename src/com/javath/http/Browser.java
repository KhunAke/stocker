package com.javath.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.RequestLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

import com.javath.util.Assign;
import com.javath.util.Instance;
import com.javath.util.ObjectException;
import com.javath.util.ObjectPoolable;
import com.javath.util.Service;

public class Browser extends Instance implements ObjectPoolable {
	
	private final static Assign assign;
	private final static String USER_AGENT;
	private final static int CONNECTION_TIMEOUT;
	private final static int SOCKET_TIMEOUT;
	private final static boolean TCP_NODELAY;
	
	private final static HttpClient default_client;
	public final static HttpHost LOCALHOST;
	private final static HttpRequest default_request;
	private final static HttpResponse no_context_response;
	private final static URI default_uri;
	private final static HttpEntity empty_entity;
	
	static {
		String default_Properties = Assign.etc + Assign.File_Separator +
				"http" + Assign.File_Separator +
				"Browser.properties";
		assign = Assign.getInstance(Browser.class, default_Properties);
		USER_AGENT = assign.getProperty("USER_AGENT", "Mozilla/5.0 (Compatible)");
		CONNECTION_TIMEOUT = (int) assign.getLongProperty("CONNECTION_TIMEOUT", 60000);
		SOCKET_TIMEOUT = (int) assign.getLongProperty("SOCKET_TIMEOUT", 60000);
		TCP_NODELAY = assign.getBooleanProperty("TCP_NODELAY", true);
		int max_total = (int) assign.getLongProperty("max_total", 200);
		int max_per_route = (int) assign.getLongProperty("max_per_route", 20);
		
		SchemeRegistry scheme_registry = new SchemeRegistry();
		scheme_registry.register(
				new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
		scheme_registry.register(
				new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));
		PoolingClientConnectionManager connection = 
				new PoolingClientConnectionManager(scheme_registry);
		// Increase max total connection to 200
		connection.setMaxTotal(max_total);
		// Increase default max connection per route to 20
		connection.setDefaultMaxPerRoute(max_per_route);
		// Increase max connections for localhost:80 to 50
		//HttpHost localhost = new HttpHost("locahost", 80);
		//cm.setMaxPerRoute(new HttpRoute(localhost), 50);
		//
		default_client = new DefaultHttpClient(connection);
		HttpParams httpParams = default_client.getParams();
		// the content of the User-Agent header.
		httpParams.setParameter(CoreProtocolPNames.USER_AGENT, USER_AGENT);
		// the timeout in milliseconds until a connection is established.
		httpParams.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONNECTION_TIMEOUT);
		// The socket timeout (SO_TIMEOUT) in milliseconds, which is the timeout for waiting for data.
		httpParams.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, SOCKET_TIMEOUT);
		// 
		httpParams.setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, TCP_NODELAY);
				
		LOCALHOST = new HttpHost("localhost");
		default_request = new HttpGet("/");
		empty_entity = new ByteArrayEntity(new byte[] {});
		try {
			default_uri = new URI("http://localhost/");
		} catch (URISyntaxException e) {
			throw new ObjectException(e);
		}
		no_context_response = new BasicHttpResponse(
				HttpVersion.HTTP_1_1, HttpStatus.SC_NO_CONTENT, "No Content");
		no_context_response.setEntity(new ByteArrayEntity(new byte[] {}));
	}
		
	// Variable for Browser
	private HttpClient client;
	private final HttpContext context;
	// Variable for Request
	private URI address;
	private HttpEntity entity;
	
	public Browser() {
		context = new BasicHttpContext();
	}
	
	@Override
	public void initialObject() {
		if (client != default_client)
			client = default_client;
		// Reset socket timeout
		setTimeout(CONNECTION_TIMEOUT);
		// Reset variable of Browser
		context.setAttribute(ExecutionContext.HTTP_TARGET_HOST, LOCALHOST);
		context.setAttribute(ExecutionContext.HTTP_REQUEST, default_request);
		context.setAttribute(ExecutionContext.HTTP_RESPONSE, no_context_response);
		// Reset variable of Request
		address(default_uri);
		entity = empty_entity;
	}
	
	public HttpClient getHttpClient() {
		return client;
	}
	public Browser setHttpClient(HttpClient client) {
		this.client = client;
		return this;
	}
	public Browser setTimeout(int timeout) {
		// The socket timeout (SO_TIMEOUT) in milliseconds, which is the timeout for waiting for data.
		client.getParams().setIntParameter(CoreConnectionPNames.SO_TIMEOUT, timeout);
		return this;
	}
	
	public Browser address(URI uri) {
 		this.address = uri;
		return this;
	}
	public Browser address(String uri) {
		try {
			return address(new URI(uri));
		} catch (URISyntaxException e) {
			throw new ObjectException(e);
		}
	}
	
	// HTTP request method : get, head, post, put, delete, trace, options
	public Response get() {
		return execute(new HttpGet(address));
	}
	public Response head() {
		return execute(new HttpHead(address));
	}
	public Response post() {// Support Entity
		return execute(requestWithEntity(new HttpPost(address), entity));
	}
	public Response put() {// Support Entity
		return execute(requestWithEntity(new HttpPut(address), entity));
	}
	public Response delete() {
		return execute(new HttpDelete(address));
	}
	public Response trace() {
		return execute(new HttpTrace(address));
	}
	public Response options() {
		return execute(new HttpOptions(address));
	}
	
	private void body(final HttpEntity entity) {
		this.entity = entity;
	}
	public Browser body(final Form form) {
		try {
			body(new UrlEncodedFormEntity(form.build()));
		} catch (UnsupportedEncodingException e) {
			throw new ObjectException(e);
		}
		return this;
	}
	public Browser body(final File file) {
		try {
			body(new FileInputStream(file));
			return this;
		} catch (FileNotFoundException e) {
			throw new ObjectException(e);
		}
	}
	public Browser body(final byte[] binary) {
		body(new ByteArrayEntity(binary));
		return this;
	}
	public Browser body(final byte[] binary, final int offset, final int length) {
		body(new ByteArrayEntity(binary, offset, length));
		return this;
	}
	public Browser body(final InputStream instream) {
		body(new InputStreamEntity(instream, -1));
		return this;
	}
	
	private static HttpRequestBase requestWithEntity(HttpRequestBase request, final HttpEntity entity) {
        if (request instanceof HttpEntityEnclosingRequest) {
            ((HttpEntityEnclosingRequest) request).setEntity(entity);
        } else {
            throw new IllegalStateException(request.getMethod()
                    + " request cannot enclose an entity");
        }
        return request;
    }	
	private synchronized Response execute(HttpRequestBase request) {
		try {
			return new Response(client.execute(request, context)).setFilename(generateFilename());
		} catch (IllegalStateException e) {
			if (e.getMessage().equals("Target host must not be null, or set in parameters."))
				request.setURI(redirect(address));	
			else
				throw e;
			return execute(request);
		} catch (ClientProtocolException e) {
			throw new ObjectException(e);
		} catch (IOException e) {
			throw new ObjectException(e);
		}
	}
	private URI redirect(URI uri) {
		HttpHost target = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
		try {
			String requestLine = uri.toString();
			if (requestLine.charAt(0) != '/') {
				String path = new URI(getRequestLine().getUri()).getPath();
				path = path.substring(0, path.lastIndexOf('/') + 1);
				requestLine = path + requestLine;
			}
			return new URI(target.toURI() + requestLine);
		} catch (URISyntaxException e) {
			throw new ObjectException(e);
		} 
	}

	private static String splitValue(String subQuery) {
		return subQuery.substring(subQuery.indexOf('=') + 1);
	}
	public String generateFilename() {
		String filename = "";
		try {
			URI uri = new URI(getRequestLine().getUri());
			try {
				filename = uri.getPath();
			} catch (java.lang.NullPointerException e) {}
			// Default filename is "index.html"
			if (filename.equals(""))
				filename = "index.html";
			else {
				filename = filename.substring(filename.lastIndexOf('/') + 1);
				if (filename.equals(""))
					filename = "index.html";
			}
			// Add extension with URI query
			String query = "";
			try {
				query = uri.getQuery();
			} catch (java.lang.NullPointerException e) {}
		
			String sequenceValue = "";
			if (query != null) {
				int index = 0;
				while(query.indexOf('&', index) > -1) {
					int start = index;
					index = query.indexOf('&', start);
					String value = splitValue(query.substring(start, index));
					if (!value.equals(""))
						sequenceValue += ("." +  value);
					index += 1;
				}
				String value = splitValue(query.substring(index));
				if (!value.equals(""))
					sequenceValue += ("." +  value);
			}
		
			if (!sequenceValue.equals(""))
				if (filename.indexOf(".") > -1)
					filename = filename.replace(".", sequenceValue + "." );
				else 
					filename = filename.replace(".", sequenceValue + ".html" );
		} catch (URISyntaxException ex) {
			filename = "index.html";
		}
		return filename; 
	}
	public RequestLine getRequestLine() {
		return getHttpRequest().getRequestLine();
	}
	public HttpRequest getHttpRequest() {
		return (HttpRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
	}
	public HttpResponse getHttpResponse() {
		return (HttpResponse) context.getAttribute(ExecutionContext.HTTP_RESPONSE);
	}
	
	public CookieStore getCookie() {
		return (CookieStore) context.getAttribute(ClientContext.COOKIE_STORE);
	}
	public Browser setCookie(CookieStore cookie) {
		context.setAttribute(ClientContext.COOKIE_STORE, cookie);
		return this;
	}
	public void printCookie() {
		List<Cookie> cookies = getCookie().getCookies();
		for (Iterator<Cookie> iterator = cookies.iterator(); iterator.hasNext();) {
			Cookie cookie = iterator.next();
			System.out.println(cookie);
		}
	}

}
