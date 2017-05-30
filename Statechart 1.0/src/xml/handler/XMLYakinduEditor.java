package xml.handler;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import model.yakindu.Choice;
import model.yakindu.Entry;
import model.yakindu.FinalState;
import model.yakindu.Region;
import model.yakindu.State;
import model.yakindu.Transition;
import model.yakindu.Vertice;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class XMLYakinduEditor {

	final String xmiNS = "http://www.omg.org/XMI";
	final String xsiNS = "http://www.w3.org/2001/XMLSchema-instance";
	final String notationNS = "http://www.eclipse.org/gmf/runtime/1.0.2/notation";
	final String sgraphNS = "http://www.yakindu.org/sct/sgraph/2.0.0";

	private Document document;

	public Document getDocument() {
		return document;
	}

	public XMLYakinduEditor(String path) throws Exception {
		super();
		document = this.read(path);
	}

	private Document read(String path) throws Exception {
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
		Element region = this.getRegionElement(state.getParentRegionId());

		Element verticeElement = this.createState(state);

		region.appendChild(verticeElement);
	}

	private Element getRegionElement(String parentRegionId) {
		Element region = findElements("regions", "xmi:id", parentRegionId).get(0);

		return region;
	}

	public void addTransition(Transition transition) throws Exception {
		Element sourceVerticeElement = findElements("*", "xmi:id", transition.getSourceVerticeId()).get(0);

		Element transitionElement = this.createTransition(transition);
		sourceVerticeElement.appendChild(transitionElement);

		Element targetVerticeElement = findElements("*", "xmi:id", transition.getTargetVerticeId()).get(0);
		String targetVerticeIncomingTransitionsText = targetVerticeElement.getAttribute("incomingTransitions");
		targetVerticeIncomingTransitionsText += " " + transition.getId();
		targetVerticeIncomingTransitionsText = targetVerticeIncomingTransitionsText.trim();

		targetVerticeElement.setAttribute("incomingTransitions", targetVerticeIncomingTransitionsText);
	}

	public ArrayList<Element> findElements(String tagName, String attributeName, String attributeValue) {

		NodeList nodeList = this.document.getElementsByTagName(tagName);

		ArrayList<Element> elementList = new ArrayList<>();

		for (int i = 0; i < nodeList.getLength(); i++) {
			Element element = (Element) nodeList.item(i);
			if (element.getAttribute(attributeName).equals(attributeValue)) {
				elementList.add(element);
			}
		}

		return elementList;
	}

	public ArrayList<Vertice> getVerticeList() throws Exception {
		ArrayList<Vertice> verticeList = new ArrayList<Vertice>();

		Element mainRegionElement = this.getMainRegionElement();

		NodeList verticesNodeList = mainRegionElement.getElementsByTagName("vertices");

		for (int i = 0; i < verticesNodeList.getLength(); i++) {
			Element verticeNode = (Element) verticesNodeList.item(i);
			Vertice vertice = this.buildVertice(verticeNode);
			verticeList.add(vertice);
		}
		return verticeList;

	}

	public ArrayList<Transition> getTransitionList() throws Exception {
		ArrayList<Transition> transitionList = new ArrayList<>();
		Element mainRegionElement = this.getMainRegionElement();

		NodeList outgoingTransitionNodeList = mainRegionElement.getElementsByTagName("outgoingTransitions");

		for (int i = 0; i < outgoingTransitionNodeList.getLength(); i++) {
			Element outgoingTransitionNode = (Element) outgoingTransitionNodeList.item(i);
			Transition newTransition = this.buildTransition(outgoingTransitionNode);

			transitionList.add(newTransition);	
		}
		
		return transitionList;
	}

	public ArrayList<String> getSpecificationList() throws Exception {
		ArrayList<String> specificationList = new ArrayList<>();
		Element mainRegionElement = this.getMainRegionElement();

		NodeList outgoingTransitionNodeList = mainRegionElement.getElementsByTagName("outgoingTransitions");

		for (int i = 0; i < outgoingTransitionNodeList.getLength(); i++) {
			Element outgoingTransitionNode = (Element) outgoingTransitionNodeList.item(i);
			Transition newTransition = this.buildTransition(outgoingTransitionNode);
			
			boolean isDuplicatedSpecification = false;
			
			for (String alreadyAddedTransition : specificationList) {
				if(newTransition.getSpecification().equals(alreadyAddedTransition)){
					isDuplicatedSpecification = true;
					break;
				}
			}
			
			if(!isDuplicatedSpecification){
				specificationList.add(newTransition.getSpecification());	
			}
		}
		
		return specificationList;
	}
	
	private Transition buildTransition(Element transitionElement) {
		Element verticeElement = (Element) transitionElement.getParentNode();

		String id = transitionElement.getAttribute("xmi:id");
		String specification = transitionElement.getAttribute("specification");
		String target = transitionElement.getAttribute("target");

		String verticeId = verticeElement.getAttribute("xmi:id");

		Transition transition = new Transition(id, verticeId, target, specification);

		return transition;
	}

	private String getParentRegionId(Element verticeNode) {
		Element parentElement = ((Element)verticeNode.getParentNode());
		
		String parentRegionId;
		
		if(parentElement.getTagName().equals("regions")){
			parentRegionId = parentElement.getAttribute("xmi:id");
		}else{
			parentRegionId = "";
		}
		return parentRegionId;
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
		case "sgraph:FinalState":
			vertice = this.buildFinalState(verticeNode);
			break;
		case "sgraph:Choice":
			vertice = this.buildChoice(verticeNode);
			break;
		default:
			System.err.println(String.format("Vertice type %s is currently not supported.", type));
			throw new NotImplementedException();
		}

		return vertice;
	}

	private Choice buildChoice(Element verticeNode) {
		String id = verticeNode.getAttribute("xmi:id");
		List<String> incomingTransitionIdList = Arrays
				.asList(verticeNode.getAttribute("incomingTransitions").split(" "));
		String parentRegionId = getParentRegionId(verticeNode);
		
		Choice choice = new Choice(id, parentRegionId);

		for (String incomingTransition : incomingTransitionIdList) {
			incomingTransition = incomingTransition.trim();

			if (incomingTransition.equals("")) {
				continue;
			}

			choice.getincomingTransitionIdList().add(incomingTransition);
		}

		ArrayList<String> transitionIdList = getTransitionIdList(verticeNode);

		choice.getoutgoingTransitionIdList().addAll(transitionIdList);

		return choice;
	}

	private FinalState buildFinalState(Element verticeNode) {
		String parentRegionId = getParentRegionId(verticeNode);		
		String id = verticeNode.getAttribute("xmi:id");
		List<String> incomingTransitionIdList = Arrays
				.asList(verticeNode.getAttribute("incomingTransitions").split(" "));
	
		FinalState state = new FinalState(id, parentRegionId);
	
		for (String incomingTransition : incomingTransitionIdList) {
			incomingTransition = incomingTransition.trim();
	
			if (incomingTransition.equals("")) {
				continue;
			}
	
			state.getincomingTransitionIdList().add(incomingTransition);
		}
	
		return state;
	}

	private State buildState(Element verticeNode) {
		String id = verticeNode.getAttribute("xmi:id");
		String name = verticeNode.getAttribute("name");
		List<String> incomingTransitionIdList = Arrays
				.asList(verticeNode.getAttribute("incomingTransitions").split(" "));
		String parentRegionId = getParentRegionId(verticeNode);
		
		NodeList nodeList = verticeNode.getElementsByTagName("regions");
		
		State state = new State(id, name, parentRegionId);
		
		for (int i = 0; i < nodeList.getLength(); i++) {
			Element element = (Element)nodeList.item(i);
			String childRegionId = element.getAttribute("xmi:id").trim();
			
			if(childRegionId.equals("")){
				continue;
			}
			
			state.getChildRegionIdList().add(childRegionId);
		}
		
		for (String incomingTransition : incomingTransitionIdList) {
			incomingTransition = incomingTransition.trim();

			if (incomingTransition.equals("")) {
				continue;
			}

			state.getincomingTransitionIdList().add(incomingTransition);
		}

		ArrayList<String> transitionIdList = getTransitionIdList(verticeNode);

		state.getoutgoingTransitionIdList().addAll(transitionIdList);

		return state;
	}

	private Entry buildEntry(Element verticeNode) {
		String id = verticeNode.getAttribute("xmi:id");
		String parentRegionId = getParentRegionId(verticeNode);
		
		Entry entry = new Entry(id, parentRegionId);

		ArrayList<String> transitionIdList = getTransitionIdList(verticeNode);

		entry.getoutgoingTransitionIdList().addAll(transitionIdList);

		return entry;
	}

	public Region buildRegion(Element regionNode) {
		String id = regionNode.getAttribute("xmi:id");
		String name = regionNode.getAttribute("name");
		//String parentRegionId = getParentRegionId(verticeNode); parentState?
		
		//NodeList nodeList = verticeNode.getElementsByTagName("regions"); verticeList?
		
		Region region = new Region(id, name);
		
//		
//		for (int i = 0; i < nodeList.getLength(); i++) {
//			Element element = (Element)nodeList.item(i);
//			String childRegionId = element.getAttribute("xmi:id").trim();
//			
//			if(childRegionId.equals("")){
//				continue;
//			}
//			
//			state.getChildRegionIdList().add(childRegionId);
//		}
//		
//		for (String incomingTransition : incomingTransitionIdList) {
//			incomingTransition = incomingTransition.trim();
//
//			if (incomingTransition.equals("")) {
//				continue;
//			}
//
//			state.getincomingTransitionIdList().add(incomingTransition);
//		}
//
//		ArrayList<String> transitionIdList = getTransitionIdList(verticeNode);
//
//		state.getoutgoingTransitionIdList().addAll(transitionIdList);

		return region;
	}
	
	private ArrayList<String> getTransitionIdList(Element verticeNode) {
		ArrayList<String> transitionIdList = new ArrayList<>();

		NodeList outgoingTransitionsNodeList = verticeNode.getElementsByTagName("outgoingTransitions");

		for (int i = 0; i < outgoingTransitionsNodeList.getLength(); i++) {

			Element outgoingTransitionsElement = (Element) outgoingTransitionsNodeList.item(i);
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
		verticeElement.setAttribute("incomingTransitions", String.join(" ", state.getincomingTransitionIdList()));

		return verticeElement;
	}

	private Element createTransition(Transition transition) {
		Element transitionElement = this.document.createElement("outgoingTransitions");

		transitionElement.setAttribute("xmi:id", transition.getId());
		transitionElement.setAttribute("specification", transition.getSpecification());
		transitionElement.setAttribute("target", transition.getTargetVerticeId());

		return transitionElement;
	}

	public Region getMainRegion() {
		Element rootElement = document.getDocumentElement();
		Element statechartElement = (Element) rootElement.getElementsByTagName("sgraph:Statechart").item(0);
		Element mainRegionElement = (Element) statechartElement.getElementsByTagName("regions").item(0);
		
		String id = mainRegionElement.getAttribute("xmi:id");
		String name = mainRegionElement.getAttribute("name");
		
		Region region = new Region(id, name);

		return region;
	}

	public Element getMainRegionElement() {
		Element rootElement = document.getDocumentElement();
		Element statechartElement = (Element) rootElement.getElementsByTagName("sgraph:Statechart").item(0);
		Element mainRegionElement = (Element) statechartElement.getElementsByTagName("regions").item(0);
		
		return mainRegionElement;
	}
	
	public ArrayList<String> getParentStateOutgoingTransitionIdList(String stateId){
		Element stateElement = this.findElements("vertices", "xmi:id", stateId).get(0);
		
		ArrayList<String> parentStateOutgoingTransitionIdList = new ArrayList<>();
		
		Element parentRegionElement = (Element)stateElement.getParentNode();
		String parentRegionId = parentRegionElement.getAttribute("xmi:id"); 
		Region mainRegion = this.getMainRegion();
		
		if(parentRegionId.equals(mainRegion.getId())){
			return parentStateOutgoingTransitionIdList;
		}
		
		Element parentStateElement = (Element)parentRegionElement.getParentNode();
		
		NodeList parentStateChildNodeList = parentStateElement.getChildNodes();
		
		for (int i = 0; i < parentStateChildNodeList.getLength(); i++) {
			if(parentStateChildNodeList.item(i).getNodeType() != 1){
				continue;
			}
			
			Element childElement = (Element)parentStateChildNodeList.item(i);
			
			if(!childElement.getTagName().equals("outgoingTransitions")){
				continue;
			}
			
			String outgoingTransitionId = childElement.getAttribute("xmi:id");
			
			parentStateOutgoingTransitionIdList.add(outgoingTransitionId);
		}
		
		String parentStateElementId = parentStateElement.getAttribute("xmi:id");
		ArrayList<String> parents = getParentStateOutgoingTransitionIdList(parentStateElementId);
		parentStateOutgoingTransitionIdList.addAll(parents);
		
		return parentStateOutgoingTransitionIdList;
	}

	public boolean isSpecidicationInConcurrentRegion(String stateId, String specification) {
		Element currentStateElement = this.findElements("vertices", "xmi:id", stateId).get(0);
		Element currentRegionElement = (Element)currentStateElement.getParentNode();
		Element parentStateElement = (Element)currentRegionElement.getParentNode();
		
		if(parentStateElement.getTagName().equals("sgraph:Statechart")){
			return false;
		}
		
		NodeList regionNodeList = parentStateElement.getChildNodes();
		
		for (int i = 0; i < regionNodeList.getLength(); i++) {
			if(regionNodeList.item(i).getNodeType() != 1){
				continue;
			}
			
			Element childElement = (Element)regionNodeList.item(i);
			
			if(!childElement.getTagName().equals("regions")){
				continue;
			}
			
			Element parallelRegion = childElement;
			
			//Skip if is the same region the state is in.
			if(parallelRegion.getAttribute("xmi:id").trim().equals(currentRegionElement.getAttribute("xmi:id").trim())){
				continue;
			}
			
			NodeList parallelRegionVerticeNodeList = parallelRegion.getChildNodes();
			
			for (int j = 0; j < parallelRegionVerticeNodeList.getLength(); j++) {
				if(parallelRegionVerticeNodeList.item(j).getNodeType() != 1){
					continue;
				}
				
				Element parallelVerticeElement = (Element)parallelRegionVerticeNodeList.item(j);
				
				if(!parallelVerticeElement.getAttribute("xsi:type").trim().equals("sgraph:State")){
					continue;
				}
				
				Element parallelStateElement = parallelVerticeElement;
				//Check the outgoing transitions
				
				NodeList parallelStateOutgoingTransitionNodeList = parallelStateElement.getChildNodes();

				for (int k = 0; k < parallelStateOutgoingTransitionNodeList.getLength(); k++) {
					if(parallelStateOutgoingTransitionNodeList.item(k).getNodeType() != 1){
						continue;
					}
					
					Element parallelStateChildElement = (Element)parallelStateOutgoingTransitionNodeList.item(k);
					
					if(!parallelStateChildElement.getTagName().equals("outgoingTransitions")){
						continue;
					}
					
					Element parallelOutgoingTransitionsElement = parallelStateChildElement;
					
					if(parallelOutgoingTransitionsElement.getAttribute("specification").equals(specification)){
						return true; //find !!!
					}
				}
			}			
		}
		
		return false;
	}
}
