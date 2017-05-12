package tests.xml.handler;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

import xml.handler.XMLYakinduEditor;

public class XMLYakinduEditorTests {

	@Test
	public void save() throws Exception {
		String sourcePath = "resources\\Simple.sct";
		String targetPath = "resources\\Simple.sct.temp";
		File targetFile = new File(targetPath);
		
		if(targetFile.exists()){
			targetFile.delete();
		}
		
		XMLYakinduEditor editor = new XMLYakinduEditor(sourcePath);
		editor.save(targetPath);
		
		String targetText = new String(Files.readAllBytes(Paths.get(targetPath)), StandardCharsets.UTF_8);
		assertNotEquals(targetText, "");
	}
	
	@Test
	public void addState() throws Exception {
		String sourcePath = "resources\\Simple.sct";
		String targetPath = "resources\\Simple.sct.temp";
		File targetFile = new File(targetPath);
		
		if(targetFile.exists()){
			targetFile.delete();
		}
		
		XMLYakinduEditor editor = new XMLYakinduEditor(sourcePath);
		editor.addState("fault");
		
		editor.save(targetPath);
		
		String targetText = new String(Files.readAllBytes(Paths.get(targetPath)), StandardCharsets.UTF_8);
		assertNotEquals(targetText, "");
	}

}
