package tests.xml.handler;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import model.yakindu.*;
import xml.handler.XMLYakinduEditor;

public class XMLYakinduEditorTests {
	private String sourcePath = "resources\\Simple.sct";
	private String targetPath = "resources\\test-output\\test.sct";
	
	@Before
	public void before() {
		File testFolder = new File(targetPath).getParentFile();
		
		if(testFolder.exists()){
			testFolder.delete();
		}
		
		testFolder.mkdirs();
    }
	
	@Test
	public void save() throws Exception {
		XMLYakinduEditor editor = new XMLYakinduEditor(sourcePath);
		editor.save(targetPath);
		
		String targetText = new String(Files.readAllBytes(Paths.get(targetPath)), StandardCharsets.UTF_8);
		assertNotEquals(targetText, "");
	}

	@Test
	public void getVertices() throws Exception {
		XMLYakinduEditor editor = new XMLYakinduEditor(sourcePath);
		ArrayList<Vertice> verticeList = editor.getVerticeList();
		
		assertEquals(verticeList.size(), 3);
		
		Vertice vertice0 = verticeList.get(0);		
		assertEquals(vertice0.getClass().getName(), Entry.class.getName());
		Entry entry0 = (Entry)vertice0;
		assertEquals(entry0.getId(), "_2XWGKTX_EeePhbNkQZRfzQ");
		ArrayList<String> entry0OutgoingTransitionList = entry0.getOutgoingTransitionList(); 
		assertEquals(entry0OutgoingTransitionList.size(), 1); 
		assertEquals(entry0OutgoingTransitionList.get(0), "_2Xf3NDX_EeePhbNkQZRfzQ");
		
		Vertice vertice1 = verticeList.get(1);		
		assertEquals(vertice1.getClass().getName(), State.class.getName());
		State state1 = (State)vertice1;
		assertEquals(state1.getId(), "_2Xf3JDX_EeePhbNkQZRfzQ");
		assertEquals(state1.getName(), "StateA");
		ArrayList<String> state1OutgoingTransitionList = state1.getOutgoingTransitionList(); 
		assertEquals(state1OutgoingTransitionList.size(), 1); 
		assertEquals(state1OutgoingTransitionList.get(0), "_0yah4Da-Eee84IcaXJAbLA");
		
		Vertice vertice2 = verticeList.get(2);		
		assertEquals(vertice2.getClass().getName(), State.class.getName());
		State state2 = (State)vertice2;
		assertEquals(state2.getId(), "_yGFegDa-Eee84IcaXJAbLA");
		assertEquals(state2.getName(), "StateB");
		ArrayList<String> state2OutgoingTransitionList = state2.getOutgoingTransitionList(); 
		assertEquals(state2OutgoingTransitionList.size(), 0);
	}

	@Test
	public void getTransitions() throws Exception {
		XMLYakinduEditor editor = new XMLYakinduEditor(sourcePath);
		ArrayList<Transition> transitionList = editor.getTransitionList();
		
		assertEquals(transitionList.size(), 2);
		
		Transition transition0 = transitionList.get(0);
		assertEquals(transition0.getId(), "_2Xf3NDX_EeePhbNkQZRfzQ");
		assertEquals(transition0.getVerticeId(), "_2XWGKTX_EeePhbNkQZRfzQ");
		assertEquals(transition0.getTarget(), "_2Xf3JDX_EeePhbNkQZRfzQ");
		assertEquals(transition0.getSpecification(), "");
		
		Transition transition1 = transitionList.get(1);		
		assertEquals(transition1.getId(), "_0yah4Da-Eee84IcaXJAbLA");
		assertEquals(transition1.getVerticeId(), "_2Xf3JDX_EeePhbNkQZRfzQ");
		assertEquals(transition1.getTarget(), "_yGFegDa-Eee84IcaXJAbLA");
		assertEquals(transition1.getSpecification(), "TransitionAB");		
	}
	
	@Test
	public void addState() throws Exception {
		XMLYakinduEditor editor = new XMLYakinduEditor(sourcePath);
		State state = new State(Vertice.createId(), "fault");
		
		editor.addState(state);
		
		
		editor.save(targetPath);
		
		String targetText = new String(Files.readAllBytes(Paths.get(targetPath)), StandardCharsets.UTF_8);
		assertNotEquals(targetText, "");
	}
	
	@Test
	public void addTransition() throws Exception {
		XMLYakinduEditor editor = new XMLYakinduEditor(sourcePath);
		Transition transition = new Transition(Vertice.createId(), "_yGFegDa-Eee84IcaXJAbLA", "TransitionAB", "_2Xf3JDX_EeePhbNkQZRfzQ");
		
		editor.addTransition(transition);
		
		
		editor.save(targetPath);
		
		String targetText = new String(Files.readAllBytes(Paths.get(targetPath)), StandardCharsets.UTF_8);
		assertNotEquals(targetText, "");
	}
}
