package net.emforge.activiti.test;

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import net.emforge.activiti.util.SignavioFixer;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class SignavioFixerTest {
	@Test
	public void testOnSignavioXml() throws Exception {
		InputStream is = this.getClass().getClassLoader().getResourceAsStream("ToDo.bpmn20.xml");
		assertNotNull(is);
		byte[] sourceXml = IOUtils.toByteArray(is);
		assertNotNull(sourceXml);
		
		SignavioFixer fixer = new SignavioFixer("procName");
		byte[] resultXml = fixer.fixSignavioXml(sourceXml);
		
		assertNotNull(resultXml);
	}
}
