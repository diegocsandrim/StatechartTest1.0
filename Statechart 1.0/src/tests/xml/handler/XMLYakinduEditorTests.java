package tests.xml.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import model.yakindu.Entry;
import model.yakindu.State;
import model.yakindu.Transition;
import model.yakindu.Vertice;
import xml.handler.XMLYakinduEditor;

public class XMLYakinduEditorTests {
	private String sourcePath = "resources\\Simple.sct";
	private String targetPath = "resources\\test-output\\test.sct";
	private String hierarchySourcePath = "resources\\Hierarchy.sct";

	@Before
	public void before() {
		File testFolder = new File(targetPath).getParentFile();

		if (testFolder.exists()) {
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
		Entry entry0 = (Entry) vertice0;
		assertEquals(entry0.getId(), "_2XWGKTX_EeePhbNkQZRfzQ");
		ArrayList<String> entry0OutgoingTransitionList = entry0.getoutgoingTransitionIdList();
		assertEquals(entry0OutgoingTransitionList.size(), 1);
		assertEquals(entry0OutgoingTransitionList.get(0), "_2Xf3NDX_EeePhbNkQZRfzQ");

		Vertice vertice1 = verticeList.get(1);
		assertEquals(vertice1.getClass().getName(), State.class.getName());
		State state1 = (State) vertice1;
		assertEquals(state1.getId(), "_2Xf3JDX_EeePhbNkQZRfzQ");
		assertEquals(state1.getName(), "StateA");
		ArrayList<String> state1OutgoingTransitionList = state1.getoutgoingTransitionIdList();
		assertEquals(state1OutgoingTransitionList.size(), 1);
		assertEquals(state1OutgoingTransitionList.get(0), "_0yah4Da-Eee84IcaXJAbLA");
		ArrayList<String> state1IncomingTransitionList = state1.getincomingTransitionIdList();
		assertEquals(state1IncomingTransitionList.size(), 1);
		assertEquals(state1IncomingTransitionList.get(0), "_2Xf3NDX_EeePhbNkQZRfzQ");

		Vertice vertice2 = verticeList.get(2);
		assertEquals(vertice2.getClass().getName(), State.class.getName());
		State state2 = (State) vertice2;
		assertEquals(state2.getId(), "_yGFegDa-Eee84IcaXJAbLA");
		assertEquals(state2.getName(), "StateB");
		ArrayList<String> state2OutgoingTransitionList = state2.getoutgoingTransitionIdList();
		assertEquals(state2OutgoingTransitionList.size(), 0);
		ArrayList<String> state2IncomingTransitionList = state2.getincomingTransitionIdList();
		assertEquals(state2IncomingTransitionList.size(), 1);
		assertEquals(state2IncomingTransitionList.get(0), "_0yah4Da-Eee84IcaXJAbLA");
	}

	@Test
	public void getTransitions() throws Exception {
		XMLYakinduEditor editor = new XMLYakinduEditor(sourcePath);
		ArrayList<Transition> transitionList = editor.getTransitionList();

		assertEquals(transitionList.size(), 2);

		Transition transition0 = transitionList.get(0);
		assertEquals(transition0.getId(), "_2Xf3NDX_EeePhbNkQZRfzQ");
		assertEquals(transition0.getSourceVerticeId(), "_2XWGKTX_EeePhbNkQZRfzQ");
		assertEquals(transition0.getSpecification(), "");
		assertEquals(transition0.getTargetVerticeId(), "_2Xf3JDX_EeePhbNkQZRfzQ");

		Transition transition1 = transitionList.get(1);
		assertEquals(transition1.getId(), "_0yah4Da-Eee84IcaXJAbLA");
		assertEquals(transition1.getSourceVerticeId(), "_2Xf3JDX_EeePhbNkQZRfzQ");
		assertEquals(transition1.getSpecification(), "TransitionAB");
		assertEquals(transition1.getTargetVerticeId(), "_yGFegDa-Eee84IcaXJAbLA");
	}

	@Test
	public void addState() throws Exception {
		XMLYakinduEditor editor = new XMLYakinduEditor(sourcePath);
		State state = new State("custom_id", "fault", "_2XMVIjX_EeePhbNkQZRfzQ", "");
		editor.addState(state);

		ArrayList<Vertice> verticeList = editor.getVerticeList();

		assertEquals(verticeList.size(), 4);

		Vertice vertice3 = verticeList.get(3);
		assertEquals(vertice3.getClass().getName(), State.class.getName());
		State state3 = (State) vertice3;
		assertEquals(state3.getId(), "custom_id");
		assertEquals(state3.getName(), "fault");
		assertEquals(state3.getoutgoingTransitionIdList().size(), 0);
		assertEquals(state3.getincomingTransitionIdList().size(), 0);
	}

	@Test
	public void addTransition() throws Exception {
		XMLYakinduEditor editor = new XMLYakinduEditor(sourcePath);

		Transition transition = new Transition("custom_id", "_yGFegDa-Eee84IcaXJAbLA", "_2Xf3JDX_EeePhbNkQZRfzQ",
				"TransitionAB");
		editor.addTransition(transition);

		ArrayList<Transition> transitionList = editor.getTransitionList();

		assertEquals(transitionList.size(), 3);

		Transition transition2 = transitionList.get(2);
		assertEquals(transition2.getId(), "custom_id");
		assertEquals(transition2.getSourceVerticeId(), "_yGFegDa-Eee84IcaXJAbLA");
		assertEquals(transition2.getSpecification(), "TransitionAB");
		assertEquals(transition2.getTargetVerticeId(), "_2Xf3JDX_EeePhbNkQZRfzQ");

		ArrayList<Vertice> verticeList = editor.getVerticeList();

		assertEquals(verticeList.size(), 3);

		State sourceState = (State) verticeList.get(2);
		assertEquals(sourceState.getoutgoingTransitionIdList().size(), 1);
		assertEquals(sourceState.getoutgoingTransitionIdList().get(0), "custom_id");
		assertEquals(sourceState.getincomingTransitionIdList().size(), 1);

		State targetState = (State) verticeList.get(1);
		assertEquals(targetState.getoutgoingTransitionIdList().size(), 1);
		assertEquals(targetState.getincomingTransitionIdList().size(), 2);
		assertEquals(targetState.getincomingTransitionIdList().get(1), "custom_id");
	}

	@Test
	public void getParentStateOutgoingTransitionIdList() throws Exception {
		XMLYakinduEditor editor = new XMLYakinduEditor(hierarchySourcePath);
		
		ArrayList<String> outgoing = editor.getParentStateOutgoingTransitionIdList("_iua5MD0aEeezT6DJfC1Xng");
		
		assertEquals(outgoing.size(), 2);
		
		assertEquals(outgoing.contains("_bIV4rD0aEeezT6DJfC1Xng"), true);
		assertEquals(outgoing.contains("_iTHD4DtyEee2dJCkaKsgEw"), true);

	}
}
