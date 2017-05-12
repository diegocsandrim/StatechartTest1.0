package xml.handler;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;

public class XMLYakinduEditor {

	final String xmiNS = "http://www.omg.org/XMI";
	final String xsiNS = "http://www.w3.org/2001/XMLSchema-instance";
	final String notationNS = "http://www.eclipse.org/gmf/runtime/1.0.2/notation";
	final String sgraphNS = "http://www.yakindu.org/sct/sgraph/2.0.0";
		
	private Document document;
	
	public XMLYakinduEditor(String path) throws Exception{
		super();
		document = this.read(path);
	}
	
	private Document read(String path) throws Exception{
		File file = new File(path);
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		docBuilderFactory.setNamespaceAware(true);
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		
		Document document = docBuilder.parse(file);
		
		document.getDocumentElement().normalize();
		
		return document;
	}

	public void save(String targetPath) throws Exception {
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		Result output = new StreamResult(new File(targetPath));
		Source input = new DOMSource(document);
		
		transformer.transform(input, output);
	}
	
	public void addState(String name) throws Exception {
		Element verticeElement = createVertice(name);
		
		Node mainRegion = document.getElementsByTagName("regions").item(0);
		mainRegion.appendChild(verticeElement);
	}

	private Element createVertice(String name) {
		String id =  createId();
		
		Element verticeElement = document.createElement("vertices");
		
		verticeElement.setAttribute("xsi:type", "sgraph:State");
		verticeElement.setAttribute("xmi:id", id);
		verticeElement.setAttribute("name", name);
		
		return verticeElement;
	}

	private String createId() {
		String id = new Integer(((int)Math.floor(Math.random()*10e22))).toString();
		return id;
	}
	
}
