import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import model.yakindu.*;
import xml.handler.XMLProcessor;
import xml.handler.XMLYakinduEditor;
import xml.statechart.Statechart;

public class Main {

	public enum Criteria {
		ALL_PATHS("all paths"), ALL_STATES("all states"), ALL_TRANSITIONS(
				"all transitions"), ALL_TRANSITIONS_WITH_FAULTS("all transitions with faults");

		private final String text;

		private Criteria(final String text) {
			this.text = text;
		}

		public String getText() {
			return this.text;
		}

		public String toString() {
			return text;
		}
	}

	private JFrame frame;
	private JTextField textField;
	private Criteria coverage;
	JButton btnNewButton;

	public static Output out;
	public JTable table;
	public DefaultTableModel model;

	private Set<String> testPaths;
	private Set<String> csvLines;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Main window = new Main();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Main() {
		initialize();
	}

	// action for the create test cases button
	private void actionCreateTestCases_AllTransitions() {
		XMLProcessor xml = new XMLProcessor();
		String filePath = textField.getText();
		String output = xml.createXmlFromYakindu(filePath);

		try {
			long startTime = System.nanoTime();
			Statechart statechart = xml.createStatechartFromXml(output);
			statechart.constructStateIdHash();
			TestGenerator tg = new TestGenerator(statechart);
			testPaths = tg.createTestPaths();
			csvLines = tg.csvLines;
			long endTime = System.nanoTime();
			long duration = (endTime - startTime) / 1000;
			System.out.println("Test Cases Generation takes: " + duration + " miliseconds");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// action for the create test cases button
	private void actionCreateTestCases_AllTransitionsWithFaults() {
		String originalFilePath = textField.getText();
		
		try {
			String statechartWithErrorStateFile = generateStatechartWithFaultTransitions(originalFilePath);
			
			XMLProcessor xml = new XMLProcessor();
			
			String output = xml.createXmlFromYakindu(statechartWithErrorStateFile);
			long startTime = System.nanoTime();
			Statechart statechart = xml.createStatechartFromXml(output);
			statechart.constructStateIdHash();
			TestGenerator tg = new TestGenerator(statechart);
			testPaths = tg.createTestPaths();
			csvLines = tg.csvLines;
			long endTime = System.nanoTime();
			long duration = (endTime - startTime) / 1000;
			System.out.println("Test Cases Generation takes: " + duration + " miliseconds");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private String generateStatechartWithFaultTransitions(String filePath) throws Exception {
		File statechartWithErrorStateFile = new File(filePath + "-with-error.sct");
	
		XMLYakinduEditor editor = new XMLYakinduEditor(filePath);
		
		Region mainRegion = editor.getMainRegion();
		
		ArrayList<String> specificationList = editor.getSpecificationList();
		ArrayList<Transition> transitionList = editor.getTransitionList();
		ArrayList<Vertice> verticelist = editor.getVerticeList();
		State errorState = new State(Vertice.createId(), "ErrorState", mainRegion.getId());
		
		ArrayList<Transition> transitionListToAdd = new ArrayList<>();
		
		for (Vertice vertice : verticelist) {
			// Skip non state vertices
			if (vertice.getClass().getName() != State.class.getName()) {
				continue;
			}
			
			State state = (State)vertice;
			
			//Skip composite state we do it in simple states
			if(state.getChildRegionIdList().size() != 0){
				continue;
			}
			
			for (String specification : specificationList) {
				// Skip always transition
				if(specification.equals("") || specification.equals("always")){
					continue;
				}
				
				// Ignore transitions present in state
				if (stateContainsSpecification(transitionList, state.getoutgoingTransitionIdList(), specification)) {
					continue;
				}
				
				// Ignore transitions present in parent states
				ArrayList<String> parentStateOutgoingTransitionIdList = editor.getParentStateOutgoingTransitionIdList(state.getId());
				if(stateContainsSpecification(transitionList, parentStateOutgoingTransitionIdList, specification)){
					continue;
				}
				

				// Ignore transitions present in other parallel regions that can be executed simultaneous to this
				if(editor.isSpecidicationInConcurrentRegion(state.getId(), specification))
				{
					continue;
				}
								
				// TODO: Yakindu is not showing transitions from inner state to state out of parent state
				// Maybe it needs some kind of reference is design part of XML
				Transition errorTransition = new Transition(
						Vertice.createId(),
						vertice.getId(),
						errorState.getId(),
						specification);
				
				transitionListToAdd.add(errorTransition);

			}
		}
		
		editor.addState(errorState);
		
		for (Transition transition : transitionListToAdd) {
			editor.addTransition(transition);
		}

		editor.save(statechartWithErrorStateFile.getPath());
		
		return statechartWithErrorStateFile.getPath();
	}
	
	private boolean stateContainsSpecification(ArrayList<Transition> allTransitions, ArrayList<String> stateTransitionIdList, String specifiation){
		Transition transition = null;
		for (String stateTransitionId : stateTransitionIdList) {			
			for (Transition checkTransition : allTransitions) {
				if(checkTransition.getId().equals(stateTransitionId)){
					transition = checkTransition;
					break;
				}
			}
			
			if (transition != null && transition.getSpecification().equals(specifiation)){
				return true;
			}
			
		}
		
		
		return false;
		
	}

	// action for the open button
	private void actionOpenStatechartButton() {
		String filePath = "";

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
		int result = fileChooser.showOpenDialog(frame);
		if (result == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();
			filePath = selectedFile.getAbsolutePath();
		}
		textField.setText(filePath);

	}

	// action for the export spmf button
	private void actionExportSPMFButton() {
		FileDialog fDialog = new FileDialog(frame, "Save", FileDialog.SAVE);
		fDialog.setVisible(true);
		String spmfPath = fDialog.getDirectory() + fDialog.getFile();
		TestPathsAdapter adapter = new TestPathsAdapter();
		Set<String> adaptedPaths = adapter.adaptToSMPF(testPaths);
		out.writeSPMFToFile(spmfPath, adaptedPaths);
	}

	// action for the export csv button
	private void actionExportCsvButton() {
		FileDialog fDialog = new FileDialog(frame, "Save", FileDialog.SAVE);
		fDialog.setVisible(true);
		String csvPath = fDialog.getDirectory() + fDialog.getFile();
		out.writeCsvToFile(csvPath, csvLines);
	}

	/**
	 * Initialize the contents of the frame.
	 */

	private void initialize() {
		frame = new JFrame();
		frame.setResizable(false);
		frame.setBounds(100, 100, 984, 596);
		frame.setTitle("Statechart Test Tool");
		frame.setIconImage(new ImageIcon("Statechart_Test_Tool.png").getImage());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(16, 80, 962, 450);
		frame.getContentPane().add(scrollPane);

		String[] columnNames = { "State", "Transition", "Test Path", "Expected State" };
		model = new DefaultTableModel(null, columnNames);

		table = new JTable(model);

		scrollPane.setViewportView(table);
		table.setFont(new Font("Arial", Font.PLAIN, 10));
		table.setBounds(68, 100, 1, 1);

		out = new Output(table);

		btnNewButton = new JButton("Create test cases");
		btnNewButton.setFont(new Font("Arial", Font.PLAIN, 10));
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				switch (coverage) {
				case ALL_PATHS:
					System.out.println("Creating Test for All Paths Coverage Criteria");
					break;
				case ALL_STATES:
					System.out.println("Creating Test for All States Coverage Criteria");
					break;
				case ALL_TRANSITIONS:
					System.out.println("Creating Test for All Transitions Coverage Criteria");
					actionCreateTestCases_AllTransitions();
					break;
				case ALL_TRANSITIONS_WITH_FAULTS:
					System.out.println("Creating Test for All Transitions With Faults Coverage Criteria");
					actionCreateTestCases_AllTransitionsWithFaults();
					break;
				}
			}
		});
		btnNewButton.setBounds(844, 39, 124, 29);
		frame.getContentPane().add(btnNewButton);

		textField = new JTextField();
		textField.setFont(new Font("Arial", Font.PLAIN, 10));
		textField.setBounds(264, 4, 579, 28);
		frame.getContentPane().add(textField);
		textField.setColumns(10);

		JLabel lblNewLabel = new JLabel("Path to your Yakindu Statechart (.sct):");
		lblNewLabel.setBounds(16, 0, 258, 37);
		lblNewLabel.setFont(new Font("Arial", Font.PLAIN, 10));
		frame.getContentPane().add(lblNewLabel);

		JLabel lblCriteria = new JLabel("Test Criteria:");
		lblCriteria.setBounds(16, 30, 258, 37);
		lblCriteria.setFont(new Font("Arial", Font.PLAIN, 10));
		frame.getContentPane().add(lblCriteria);

		String[] criteriaList = { Criteria.ALL_PATHS.getText(), Criteria.ALL_TRANSITIONS.getText(),
				Criteria.ALL_STATES.getText(), Criteria.ALL_TRANSITIONS_WITH_FAULTS.getText(), };

		JComboBox<String> testCriteriaList = new JComboBox<>(criteriaList);
		testCriteriaList.setFont(new Font("Arial", Font.PLAIN, 10));

		testCriteriaList.setBounds(264, 37, 258, 27);
		testCriteriaList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String criteria = (String) testCriteriaList.getSelectedItem();
				if (criteria.compareToIgnoreCase(Criteria.ALL_PATHS.getText()) == 0) {
					Main.this.coverage = Criteria.ALL_PATHS;
				}
				if (criteria.compareToIgnoreCase(Criteria.ALL_STATES.getText()) == 0) {
					Main.this.coverage = Criteria.ALL_STATES;
				}
				if (criteria.compareToIgnoreCase(Criteria.ALL_TRANSITIONS.getText()) == 0) {
					Main.this.coverage = Criteria.ALL_TRANSITIONS;
				}
				if (criteria.compareToIgnoreCase(Criteria.ALL_TRANSITIONS_WITH_FAULTS.getText()) == 0) {
					Main.this.coverage = Criteria.ALL_TRANSITIONS_WITH_FAULTS;
				}
			}
		});
		testCriteriaList.setSelectedIndex(1);
		frame.getContentPane().add(testCriteriaList);

		JButton btnOpenStatechart = new JButton("Open Statechart");
		btnOpenStatechart.setFont(new Font("Arial", Font.PLAIN, 10));

		btnOpenStatechart.setBounds(844, 5, 124, 29);
		btnOpenStatechart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionOpenStatechartButton();
			}
		});
		frame.getContentPane().add(btnOpenStatechart);

		JButton btnExportInSpmf = new JButton("Export in SPMF format");
		btnExportInSpmf.setFont(new Font("Arial", Font.PLAIN, 10));
		btnExportInSpmf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionExportSPMFButton();
			}
		});
		btnExportInSpmf.setBounds(660, 532, 163, 29);
		frame.getContentPane().add(btnExportInSpmf);

		JButton btnExportTocsv = new JButton("Export to .csv");
		btnExportTocsv.setFont(new Font("Arial", Font.PLAIN, 10));
		btnExportTocsv.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionExportCsvButton();
			}
		});
		btnExportTocsv.setBounds(844, 532, 124, 29);
		frame.getContentPane().add(btnExportTocsv);

	}
}