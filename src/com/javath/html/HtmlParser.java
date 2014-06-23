package com.javath.html;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.NoSuchElementException;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.html.dom.HTMLDocumentImpl;
import org.cyberneko.html.parsers.DOMFragmentParser;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.html.HTMLDocument;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.javath.util.Instance;
import com.javath.util.ObjectException;

public class HtmlParser extends Instance {
	
	private final static ObjectPool<DOMFragmentParser> pool_parser;
	private final static ObjectPool<HTMLDocument> pool_document;
	
	static {
		pool_parser = initialPoolParser();
		pool_document = initialPoolDocument();
	}
	
	private final static ObjectPool<DOMFragmentParser> initialPoolParser() {
		return new GenericObjectPool<DOMFragmentParser>(
				new BasePooledObjectFactory<DOMFragmentParser>() {
					@Override
					public DOMFragmentParser create() throws Exception {
						return new DOMFragmentParser();
					}
					/**
				     * Use the default PooledObject implementation.
				     */
					@Override
					public PooledObject<DOMFragmentParser> wrap(DOMFragmentParser parser) {
						return new DefaultPooledObject<DOMFragmentParser>(parser);
					}
					/**
				     * When an object is returned to the pool, clear the buffer.
				     * /
				    @Override
				    public void passivateObject(PooledObject<MulticastEvent> pooled) {
				    	pooled.getObject().clear();
				    }
				    /**/
				});
	}
	private final static ObjectPool<HTMLDocument> initialPoolDocument() {
		return new GenericObjectPool<HTMLDocument>(
				new BasePooledObjectFactory<HTMLDocument>() {
					@Override
					public HTMLDocument create() throws Exception {
						return new HTMLDocumentImpl();
					}
					/**
				     * Use the default PooledObject implementation.
				     */
					@Override
					public PooledObject<HTMLDocument> wrap(HTMLDocument document) {
						return new DefaultPooledObject<HTMLDocument>(document);
					}
					/**
				     * When an object is returned to the pool, clear the buffer.
				     * /
				    @Override
				    public void passivateObject(PooledObject<MulticastEvent> pooled) {
				    	pooled.getObject().clear();
				    }
				    /**/
				});
	}

	private static DOMFragmentParser borrowParser() {
		try {
			return pool_parser.borrowObject();
		} catch (NoSuchElementException e) {
			throw new ObjectException(e);
		} catch (IllegalStateException e) {
			throw new ObjectException(e);
		} catch (Exception e) {
			throw new ObjectException(e);
		}
	}	
	private static void returnParser(DOMFragmentParser parser) {
		try {
			pool_parser.returnObject(parser);
		} catch (Exception e) {
			throw new ObjectException(e);
		}
	}
	private static HTMLDocument borrowDocument() {
		try {
			return pool_document.borrowObject();
		} catch (NoSuchElementException e) {
			throw new ObjectException(e);
		} catch (IllegalStateException e) {
			throw new ObjectException(e);
		} catch (Exception e) {
			throw new ObjectException(e);
		}
	}	
	private static void returnDocument(HTMLDocument document) {
		try {
			pool_document.returnObject(document);
		} catch (Exception e) {
			throw new ObjectException(e);
		}
	}
	
	private InputSource source;
	private DocumentFragment fragment;
	
	public HtmlParser(InputStream source) {
		this.setInputStream(source);
	}
	public HtmlParser(InputStream source, String charset) { 
        this.setInputStream(source, charset);
	}
	
	public HtmlParser setInputStream(InputStream source) {
		setInputStream(source, Charset.defaultCharset().toString());
		return this;
	}
	public HtmlParser setInputStream(InputStream source, String charset) {
		this.source = new InputSource(source);
        this.source.setEncoding(charset);
        this.fragment = null;
		return this;
	}
	
	public DocumentFragment parse() {
		if (fragment == null) {
			DOMFragmentParser parser = borrowParser();
			HTMLDocument document = borrowDocument();
			try {
			fragment = document.createDocumentFragment();
			parser.parse(source, fragment);
			source = null;
			} catch (SAXException e) {
				throw new ObjectException(e);
			} catch (IOException e) {
				throw new ObjectException(e);
			} finally {
				returnDocument(document);
				returnParser(parser);
			}
		}
		//travel(null,fragment);
		return fragment;
	}
	
	public static String attribute(Node node, String name) {
		NamedNodeMap attributes = node.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			Node attribute = attributes.item(i);
			if (attribute.getNodeName().equals(name))
				return attribute.getNodeValue();
		}
		return null;
	}
	
	public static String text(Node node) {
		StringBuffer string = new StringBuffer();
		if (node.getNodeName().equals("#text"))
			string.append(node.getNodeValue());
		Node child = node.getFirstChild();
		while (child != null) {
			string.append(text(child));
            child = child.getNextSibling();
        }
		return string.toString().trim();
	}
	
	public static void print(Node node) {
		print("", node);
	}
	
	private static void print(String space, Node node) {
		printNode(space, node);
		Node child = node.getFirstChild();
		while (child != null) {
			print(space + " ", child);
            child = child.getNextSibling();
        }
	}
	
	public static void printNode(String space, Node node) {
		StringBuffer string = new StringBuffer();
		NamedNodeMap attributes = node.getAttributes();
		try {
			string.append(node.getNodeName());
			string.append(": ");
			for (int i = 0; i < attributes.getLength(); i++) {
				Node attribute = attributes.item(i);
				string.append(attribute.getNodeName());
				string.append("=\"");
				string.append(attribute.getNodeValue());
				string.append("\", ");
			}
			if (string.charAt(string.length() - 2) == ',')
				string.delete(string.length() - 2, string.length());
		} catch (NullPointerException e) {} //bypass 
		System.out.println(space + (string.toString() + " : " + node.getNodeValue()).trim());
	}
	
	public static String stringNode(Node node) {
		StringBuffer string = new StringBuffer();
		NamedNodeMap attributes = node.getAttributes();
		try {
			string.append(node.getNodeName());
			string.append(": ");
			for (int i = 0; i < attributes.getLength(); i++) {
				Node attribute = attributes.item(i);
				string.append(attribute.getNodeName());
				string.append("=\"");
				string.append(attribute.getNodeValue());
				string.append("\", ");
			}
			if (string.charAt(string.length() - 2) == ',')
				string.delete(string.length() - 2, string.length());
		} catch (NullPointerException e) {} //bypass
		return (string.toString() + " : " + node.getNodeValue()).trim();
	}
	
}
