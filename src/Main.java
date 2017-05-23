import java.awt.EventQueue;
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
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import model.yakindu.Region;
import model.yakindu.State;
import model.yakindu.Transition;
import model.yakindu.Vertice;
import xml.handler.XMLProcessor;
import xml.handler.XMLYakinduEditor;
import xml.statechart.Statechart;

public class Main {

	public enum Criteria{
		ALL_PATHS("all paths"),
		ALL_STATES("all states"),
		ALL_TRANSITIONS("all transitions"),
		ALL_TRANSITIONS_WITH_FAULTS("all transitions with faults");
		
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
	private JTextField selectedFile;
	private Criteria coverage;
		
	public static Output out;
	public static JLabel stats;
	public static DefaultTableModel model;
	public JTable table;
		
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
					window.frame.setLocationRelativeTo(null);
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

	//action for the create test cases button
	private void actionCreateTestCases_AllTransitions() {				
		this.cleanTable();
		
		String filePath = selectedFile.getText();
		String[] aux = selectedFile.getText().split("\\\\");
		
		XMLProcessor xml = new XMLProcessor();
		xml.createXmlFromYakindu(filePath);
								
		out.printRow("Selected File: " + aux[aux.length-1], "", "", "");
		out.printRow("Test Criteria: All Transitions", "", "", "");
		
		try {
			long startTime = System.currentTimeMillis();			
			
			Statechart statechart = xml.createStatechartFromXml("temp.xml");
			statechart.constructStateIdHash();
			TestGenerator tg = new TestGenerator(statechart);
			testPaths = tg.createTestPaths();
			csvLines = tg.csvLines;
			
			long endTime = System.currentTimeMillis();
			long duration = endTime - startTime;			
			this.writeExecutionStatistics(duration);	
		} catch(Exception ex) {
			JOptionPane.showMessageDialog(null, "An error occurred while generating test cases. "
					+ "Check for errors in your Yakindu SCT statechart file or maybe we can't help you. Sorry :(", "Error", JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
		}
	}
	
	// action for the create test cases button
		private void actionCreateTestCases_AllTransitionsWithFaults() {
			this.cleanTable();
			
			String filePath = selectedFile.getText();					
			String[] aux = selectedFile.getText().split("\\\\");
			
			out.printRow("Selected File: " + aux[aux.length-1], "", "", "");
			out.printRow("Test Criteria: All Transitions With Fault", "", "", "");
			
			try {
				String statechartWithErrorStateFile = generateStatechartWithFaultTransitions(filePath);				
				XMLProcessor xml = new XMLProcessor();
				
				String output = xml.createXmlFromYakindu(statechartWithErrorStateFile);
				long startTime = System.currentTimeMillis();
				Statechart statechart = xml.createStatechartFromXml(output);
				statechart.constructStateIdHash();
				TestGenerator tg = new TestGenerator(statechart);
				testPaths = tg.createTestPaths();
				csvLines = tg.csvLines;
				
				long endTime = System.currentTimeMillis();
				long duration = endTime - startTime;											
				this.writeExecutionStatistics(duration);
				
				JOptionPane.showMessageDialog(null, "These results refers to a new statechart file with faulty outgoing transitions"
						+ "\nthat we created in the same directory of your original statechart file. Check it out!", "Faulty Transitions Coverage Criteria", JOptionPane.INFORMATION_MESSAGE);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		private String generateStatechartWithFaultTransitions(String filePath) throws Exception {
			File statechartWithErrorStateFile = new File(filePath.replace(".sct", "") + "-with-error.sct");
		
			XMLYakinduEditor editor = new XMLYakinduEditor(filePath);
			
			Region mainRegion = editor.getMainRegion();
			
			ArrayList<String> specificationList = editor.getSpecificationList();
			ArrayList<Transition> transitionList = editor.getTransitionList();
			ArrayList<Vertice> verticelist = editor.getVerticeList();
			State errorState = new State(Vertice.createId(), "ErrorState", mainRegion.getId(), "");
			
			ArrayList<Transition> transitionListToAdd = new ArrayList<>();
			
			for (Vertice vertice : verticelist) {
				// Skip non state vertices
				if (vertice.getClass().getName() != State.class.getName()) {
					continue;
				}
				
				State state = (State)vertice;
				
				//Skip composite state we do it in simple states
				if(!state.getChildRegionId().equals("")){
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
					
					// TODO: Yakindu is not showing transitions from inner state to state out of parent state
					// Maybe it needs some kind of reference in design part of XML
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
	
	//action for the open button
	private void actionOpenStatechartButton() {
		String filePath = "";

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setFileFilter(new FileNameExtensionFilter(".sct", "sct"));
		int result = fileChooser.showOpenDialog(frame);
		if (result == JFileChooser.APPROVE_OPTION) {
		    File selectedFile = fileChooser.getSelectedFile();
		    filePath = selectedFile.getAbsolutePath();
		}
		selectedFile.setText(filePath);

	}
	
	//action for the export spmf button
	private void actionExportSPMFButton() {        
		String spmfPath = this.getSaveLocation(null);
		if (spmfPath != null) {        	 
        	TestPathsAdapter adapter = new TestPathsAdapter();
            Set<String> adaptedPaths = adapter.adaptToSMPF(testPaths);
            out.writeSPMFToFile(spmfPath, adaptedPaths);
        }
	}
	
	//action for the export csv button
	private void actionExportCsvButton() {		       
        String csvPath = this.getSaveLocation("csv");
		if (csvPath != null) {        	 
			out.writeCsvToFile(csvPath, csvLines);
        }               
	}
	
	private String getSaveLocation(String extension) {
	 	if(table.getRowCount() == 0) {
        	JOptionPane.showMessageDialog(null, "No data to export!", "Error", JOptionPane.ERROR_MESSAGE);
        } else {		
			JFileChooser fileChooser = new JFileChooser();
	        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooser.setAcceptAllFileFilterUsed(false);
			fileChooser.setMultiSelectionEnabled(false);
			
			if(extension != null)
				fileChooser.setFileFilter(new FileNameExtensionFilter("."+extension, extension));
	        
	        int option = fileChooser.showSaveDialog(null);
	        if(option == JFileChooser.APPROVE_OPTION)
	        	return fileChooser.getCurrentDirectory().toString() + System.getProperty("file.separator") + fileChooser.getSelectedFile().getName();
        }
	 	return null;
	}
	
	private void writeExecutionStatistics(long duration) {
		System.out.println("Execution Time: " + duration + " miliseconds");
		stats.setText("Last Execution: " + stats.getText() + " | Processing Time: " + duration + " miliseconds" );
		stats.setVisible(true);
	}
	
	public void cleanTable() {		
		if(model.getRowCount() > 2) {
			int option = JOptionPane.showConfirmDialog(null, "Do you want to clean all previous results?", "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if(option == 0) { //yes
				while( model.getRowCount() > 0 ){
			        model.removeRow(0);
			    }
			} else {
				out.printRow("", "", "", "");
			}
		}			
	}
	
	/**
	 * Initialize the contents of the frame.
	 */
	
	@SuppressWarnings("serial")
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
		

		String[] columnNames = {"State","Transition","Test Path","Expected State"};
		model= new DefaultTableModel(null,columnNames);

		table = new JTable(model) {  
		  public boolean isCellEditable(int row, int column){  
			    return false;  
		  }  
		}; 
				
		scrollPane.setViewportView(table);
		table.setFont(new Font("Arial", Font.PLAIN, 10));
		table.setBounds(68, 100, 1, 1);		
		
		out = new Output(table);
				
		JButton btnCreateTestCases = new JButton("Create test cases");
		btnCreateTestCases.setFont(new Font("Arial", Font.PLAIN, 10));
		btnCreateTestCases.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(selectedFile.getText() == null || selectedFile.getText().equals("")) {
					JOptionPane.showMessageDialog(null, "Please open your Yakindu statechart file (.sct)!", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
										
				switch (coverage){
					case ALL_PATHS:
							//System.out.println("Creating Test Cases for All Paths Coverage Criteria");
							JOptionPane.showMessageDialog(null, "This coverage criteria has not been implemented yet!", "Not Implemented", JOptionPane.ERROR_MESSAGE);
							break;
					case ALL_STATES:
							//System.out.println("Creating Test Cases for All States Coverage Criteria");
							JOptionPane.showMessageDialog(null, "This coverage criteria has not been implemented yet!", "Not Implemented", JOptionPane.ERROR_MESSAGE);
							break;
					case ALL_TRANSITIONS:
							System.out.println("Creating Test Cases for All Transitions Coverage Criteria");
							actionCreateTestCases_AllTransitions();
							break;	
					case ALL_TRANSITIONS_WITH_FAULTS:
							System.out.println("Creating Test for All Transitions With Faults Coverage Criteria");
							actionCreateTestCases_AllTransitionsWithFaults();
							break;	
				}
			}
		});
		btnCreateTestCases.setBounds(844, 39, 124, 29);
		frame.getContentPane().add(btnCreateTestCases);
		
		selectedFile = new JTextField();
		selectedFile.setFont(new Font("Arial", Font.PLAIN, 10));
		selectedFile.setBounds(264, 4, 579, 28);
		frame.getContentPane().add(selectedFile);
		selectedFile.setColumns(10);			
		
		JLabel lblNewLabel = new JLabel("Path to your Yakindu Statechart (.sct):");
		lblNewLabel.setBounds(16, 0, 258, 37);
		lblNewLabel.setFont(new Font("Arial", Font.PLAIN, 12));
		frame.getContentPane().add(lblNewLabel);

		JLabel lblCriteria = new JLabel("Test Criteria:");
		lblCriteria.setBounds(16, 30, 258, 37);
		lblCriteria.setFont(new Font("Arial", Font.PLAIN, 12));
		frame.getContentPane().add(lblCriteria);
		
		String[] criteriaList = { Criteria.ALL_TRANSITIONS.getText(), Criteria.ALL_TRANSITIONS_WITH_FAULTS.getText(), Criteria.ALL_STATES.getText(), Criteria.ALL_PATHS.getText()};
		JComboBox<String> testCriteriaList = new JComboBox<>(criteriaList);
		testCriteriaList.setFont(new Font("Arial", Font.PLAIN, 12));

		testCriteriaList.setBounds(264, 37, 258, 27);
		testCriteriaList.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				String criteria=(String)testCriteriaList.getSelectedItem();
				if (criteria.compareToIgnoreCase(Criteria.ALL_PATHS.getText())==0){
					Main.this.coverage=Criteria.ALL_PATHS;
				}
				if (criteria.compareToIgnoreCase(Criteria.ALL_STATES.getText())==0){
					Main.this.coverage=Criteria.ALL_STATES;
				} 
				if (criteria.compareToIgnoreCase(Criteria.ALL_TRANSITIONS.getText())==0){
					Main.this.coverage=Criteria.ALL_TRANSITIONS;
				} 
				if (criteria.compareToIgnoreCase(Criteria.ALL_TRANSITIONS_WITH_FAULTS.getText()) == 0) {
					Main.this.coverage = Criteria.ALL_TRANSITIONS_WITH_FAULTS;
				}
			}			
		});
		testCriteriaList.setSelectedIndex(0);
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
		btnExportInSpmf.setBounds(670, 532, 163, 29);
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
		
		stats = new JLabel();
		stats.setBounds(16, 532, 1000, 37);		
		stats.setFont(new Font("Arial", Font.PLAIN, 12));
		stats.setVisible(false);
		frame.getContentPane().add(stats);
	}
}