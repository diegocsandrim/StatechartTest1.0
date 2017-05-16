package xml.handler;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import model.yakindu.*;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
	
	public void addState(State state) throws Exception {
		Element mainRegion = this.getMainRegion();
		
		Element verticeElement = this.createState(state);
		
		mainRegion.appendChild(verticeElement);
	}

	public void addTransition(Transition transition) throws Exception {		
		Element sourceVerticeElement = findElements("*", "xmi:id", transition.getVerticeId()).get(0);
		
		Element transitionElement = this.createTransition(transition);
		sourceVerticeElement.appendChild(transitionElement);
		
		Element targetVerticeElement = findElements("*", "xmi:id", transition.getTarget()).get(0);
		String targetVerticeIncomingTransitionsText = targetVerticeElement.getAttribute("incomingTransitions");
		targetVerticeIncomingTransitionsText += " " + transition.getId();
		targetVerticeIncomingTransitionsText = targetVerticeIncomingTransitionsText.trim();
		
		
		targetVerticeElement.setAttribute("incomingTransitions", targetVerticeIncomingTransitionsText);
	}

	private ArrayList<Element> findElements(String tagName, String attributeName, String attributeValue) {
		
		NodeList nodeList = this.document.getElementsByTagName(tagName);
		
		ArrayList<Element> elementList = new ArrayList<>();
		
		for (int i = 0; i < nodeList.getLength(); i++) {
			Element element = (Element)nodeList.item(i);
			if(element.getAttribute(attributeName).equals(attributeValue)){
				elementList.add(element);
			}
		}
		
		return elementList;
	}

	public ArrayList<Vertice> getVerticeList() throws Exception {
		ArrayList<Vertice> verticeList = new ArrayList<Vertice>();
		
		Element mainRegionElement = this.getMainRegion();
		
		
		NodeList verticesNodeList = mainRegionElement.getElementsByTagName("vertices");
		
		for (int i = 0; i < verticesNodeList.getLength(); i++) {
			Element verticeNode = (Element)verticesNodeList.item(i);
			Vertice vertice = this.buildVertice(verticeNode);
			verticeList.add(vertice);
		}
		return verticeList;
		
	}
	
	public ArrayList<Transition> getTransitionList() throws Exception {
		ArrayList<Transition> transitionList = new ArrayList<>();
		Element mainRegionElement = this.getMainRegion();
		
		NodeList outgoingTransitionNodeList = mainRegionElement.getElementsByTagName("outgoingTransitions");
		
		for (int i = 0; i < outgoingTransitionNodeList.getLength(); i++) {
			Element outgoingTransitionNode = (Element)outgoingTransitionNodeList.item(i);
			Transition transition = this.buildTransition(outgoingTransitionNode);
			transitionList.add(transition);
		}
		return transitionList;
		
	}
	
	private Transition buildTransition(Element transitionElement) {
		Element verticeElement = (Element)transitionElement.getParentNode();
		
		String id = transitionElement.getAttribute("xmi:id");
		String specification = transitionElement.getAttribute("specification");
		String target = transitionElement.getAttribute("target");
		
		String verticeId = verticeElement.getAttribute("xmi:id");
		
		Transition transition = new Transition(id, verticeId, specification, target);
		
		return transition;
	}

	private Vertice buildVertice(Element verticeNode) {
		String type = verticeNode.getAttribute("xsi:type");
		
		Vertice vertice;
		
		switch (type) {
		case "sgraph:Entry":
			vertice = this.buildEntry(verticeNode);
			break;
		case "sgraph:State":
			vertice = this.buildState(verticeNode);
			break;
		default:
			System.err.println(String.format("Vertice type %s is currently not supported.", type));
			throw new NotImplementedException();
		}
		
		return vertice;
	}

	private State buildState(Element verticeNode) {
		String id = verticeNode.getAttribute("xmi:id");
		String name = verticeNode.getAttribute("name");
		List<String> incomingTransitionIdList = Arrays.asList(verticeNode.getAttribute("incomingTransitions").split(" "));
		
		State state = new State(id, name);
		state.getIncomingTransitionList().addAll((incomingTransitionIdList));
		
		ArrayList<String> transitionIdList = getTransitionIdList(verticeNode);
		
		state.getOutgoingTransitionList().addAll(transitionIdList);
		
		return state;
	}

	private Entry buildEntry(Element verticeNode) {
		String id = verticeNode.getAttribute("xmi:id");
		
		Entry entry = new Entry(id);
		
		ArrayList<String> transitionIdList = getTransitionIdList(verticeNode);
		
		entry.getOutgoingTransitionList().addAll(transitionIdList);
		
		return entry;
	}

	private ArrayList<String> getTransitionIdList(Element verticeNode) {
		ArrayList<String> transitionIdList = new ArrayList<>();
		
		NodeList outgoingTransitionsNodeList = verticeNode.getElementsByTagName("outgoingTransitions");
		
		for (int i = 0; i < outgoingTransitionsNodeList.getLength(); i++) {
			
			Element outgoingTransitionsElement = (Element)outgoingTransitionsNodeList.item(i);
			String transitionId = outgoingTransitionsElement.getAttribute("xmi:id");
			transitionIdList.add(transitionId);
		}
		
		return transitionIdList;
	}

	private Element createState(State state) {
		Element verticeElement = this.document.createElement("vertices");
		
		verticeElement.setAttribute("xsi:type", "sgraph:State");
		verticeElement.setAttribute("xmi:id", state.getId());
		verticeElement.setAttribute("name", state.getName());
		verticeElement.setAttribute("incomingTransitions", String.join(" ", state.getIncomingTransitionList()));		
		
		return verticeElement;
	}
	
	private Element createTransition(Transition transition) {
		Element transitionElement = this.document.createElement("outgoingTransitions");
		
		transitionElement.setAttribute("xmi:id", transition.getId());
		transitionElement.setAttribute("specification", transition.getSpecification());
		transitionElement.setAttribute("target", transition.getTarget());		
		
		return transitionElement;
	}
	
	private Element getMainRegion(){
		Element rootElement = document.getDocumentElement();
		Element statechartElement = (Element)rootElement.getElementsByTagName("sgraph:Statechart").item(0);
		Element mainRegionElement = (Element)statechartElement.getElementsByTagName("regions").item(0);
		
		return mainRegionElement;
	}
}
