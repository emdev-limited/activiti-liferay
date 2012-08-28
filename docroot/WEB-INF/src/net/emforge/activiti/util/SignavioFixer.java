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

import org.apache.commons.lang.StringUtils;
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
            //set activiti NS
            Element definitions = doc.getDocumentElement();
            definitions.setAttributeNS("http://www.w3.org/2000/xmlns/","xmlns:activiti", "http://activiti.org/bpmn");
//            definitions.setAttribute("targetNamespace", "http://www.activiti.org/test");
            
            
            NodeList processes = doc.getElementsByTagName("process");
            
            // add process name
            for (int i=0; i < processes.getLength(); i++) {
            	Node node = processes.item(i);
            	Element element = (Element)node;
            	if (node.getAttributes().getNamedItem("name") == null) {
            		_log.info("name attribute is missed in process tag, add it");
            		 element.setAttribute("name", processName);
            	}
            	//fix isExecutable="false"
            	element.setAttribute("isExecutable", "true");
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
                		 Node userTask = node.getParentNode();
                     	 if (userTask.getNodeName().equals("userTask")) {
                     		_log.info(String.format("found userTask [%s] to improve", userTask.getAttributes().getNamedItem("name")));
                     		NodeList expressions = element.getElementsByTagName("formalExpression");
                     		if (expressions != null && expressions.item(0) != null) {
                     			Element formalExpression = (Element) expressions.item(0);
                     			String formalExpressionVal = formalExpression.getLastChild().getNodeValue();
                     			Element usrTaskElement = (Element) userTask;
                     			if (StringUtils.isNotEmpty(formalExpressionVal)) {
                     				if (tagName.equals("humanPerformer")) {
                             			//handle assignee - should be activiti:assignee="${responsibleUserId}"
                     					usrTaskElement.setAttribute("activiti:assignee", formalExpressionVal);
                             		} else if (tagName.equals("potentialOwner")) {
                             			//handle candidate groups - should be activiti:candidateGroups="#{liferayGroups.getGroups(execution, &quot;Role1, Role2&quot;)}"
                             			String attrValue = String.format("#{liferayGroups.getGroups(execution, \" %s \")}", formalExpressionVal);
                             			usrTaskElement.setAttribute("activiti:candidateGroups", attrValue);
                             		} else if (tagName.equals("performer")) {
                             			//TODO handle candidate users
                             		}
                     			}
                     		}
                     	}
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
