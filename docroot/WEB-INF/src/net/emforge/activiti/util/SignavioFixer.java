package net.emforge.activiti.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

/** This class is supposed to fix different issues we found in xml produced by signavio modeler
 * 
 * @author akakunin
 *
 */
public class SignavioFixer {
	private static Log _log = LogFactoryUtil.getLog(SignavioFixer.class);
	
	String processName;
	
	public SignavioFixer(String processName) {
		this.processName = processName;
	}
	
	/** fix xml produced by signavio
	 * 
	 * @param sourceBytes
	 * @return
	 */
	public byte[] fixSignavioXml(byte[] sourceBytes) {
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(sourceBytes);
			ByteArrayOutputStream baos = new ByteArrayOutputStream(sourceBytes.length);

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            Document doc = builder.parse(bais);
            NodeList processes = doc.getElementsByTagName("process");
            
            // add process name
            for (int i=0; i < processes.getLength(); i++) {
            	Node node = processes.item(i);
            	if (node.getAttributes().getNamedItem("name") == null) {
            		_log.info("name attribute is missed in process tag, add it");
            		 Element element = (Element)node;
            		 element.setAttribute("name", processName);
            	}
            }

            // remove resourceRef attribute
            String[] tagNames = new String[] {"performer", "humanPerformer", "potentialOwner"};
            for (String tagName : tagNames) {
            	NodeList nodes = doc.getElementsByTagName(tagName);
            	for (int i=0; i < nodes.getLength(); i++) {
                	Node node = nodes.item(i);
                	if (node.getAttributes().getNamedItem("resourceRef") != null) {
                		_log.info("found resouceRef attribute - remove it");
                		 Element element = (Element)node;
                		 element.removeAttribute("resourceRef");
                	}
                }
            }
            
            Result result = new StreamResult(baos);

            // Write the DOM document to the file
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            Source source = new DOMSource(doc);
            xformer.transform(source, result);
            

			
			return baos.toByteArray();
		} catch (Exception ex) {
			_log.debug("Cannot fix xml", ex);
			_log.info("Cannot fix xml: " + ex.getMessage());
			return null;
		}
	}
}
