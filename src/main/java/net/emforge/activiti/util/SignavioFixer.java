package net.emforge.activiti.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.Normalizer;

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
    private String SID_PREFIX = "sid-";

	public SignavioFixer(String processName) {
		this.processName = normalize(processName);
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
			for (int i = 0; i < processes.getLength(); i++) {
				Node node = processes.item(i);
				Element element = (Element) node;
				if (node.getAttributes().getNamedItem("name") == null) {
					_log.info("name attribute is missed in process tag, add process name: "
							+ processName);

					element.setAttribute("name", processName);
				}
				
				// also set processName into id - to avoid multiple
				// workflows definitions in the system
				String oldProcessId = element.getAttribute("id");
				if (oldProcessId.startsWith(SID_PREFIX)) {
					_log.info("Replace process id with : " + processName);
					element.setAttribute("id", processName);
					// now need also change ID for related bpmndi:BPMNPlane
					replaceSid(doc, "bpmndi:BPMNPlane", "bpmnElement", oldProcessId, processName);
				}
				
				// fix isExecutable="false"
				element.setAttribute("isExecutable", "true");
			}

            // remove resourceRef attribute
            String[] tagNames = new String[] {"performer", "humanPerformer", "potentialOwner","endEvent"};
            for (String tagName : tagNames) {
                if (tagName.equals("endEvent")){
                    processEndEvent(doc);
                    continue;
                }
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

    /**
     * Replaces endEvent signavio id with normalized name
     * @param doc
     */
    private void processEndEvent(Document doc) {
        NodeList nodes = doc.getElementsByTagName("endEvent");
        
        for (int i=0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            Element element = (Element)node;
            
            String oldEndEventId = element.getAttribute("id");
            String endEventName = element.getAttribute("name");
            
            if (oldEndEventId.startsWith(SID_PREFIX) && StringUtils.isNotBlank(endEventName)) {
                 
                 endEventName = normalize(endEventName);
                 
                 _log.info("End Event " + oldEndEventId + " replaced to " + endEventName);
                 //element.setAttribute("name",endEventName);
                 element.setAttribute("id",endEventName);
                 
                 replaceSid(doc,"sequenceFlow","targetRef",oldEndEventId,endEventName);
                 replaceSid(doc,"bpmndi:BPMNShape","bpmnElement",oldEndEventId,endEventName);
            }
        }
    }

    /**
     * Replaces signavio generated id with normalized name
     * @param doc  xml document
     * @param tagName
     * @param attribute
     * @param oldId id which we want to replace
     * @param newId new normalized name
     */
    private void replaceSid(Document doc,String tagName,String attribute,String oldId, String newId){
        NodeList nodeList = doc.getElementsByTagName(tagName);
        for (int j=0; j < nodeList.getLength(); j++) {
            Node node = nodeList.item(j);
            Element element = (Element)node;
            if (StringUtils.equals(element.getAttribute(attribute), oldId)) {
                element.setAttribute(attribute, newId);
            }
        }
    }

    /**
     * normalize name
     * @return normalized name
     */
    private String normalize(String name){
        name = name.replaceAll("[^a-zA-Z\\u0410-\\u042F\\u0430-\\u044F0-9]+","");
        return Normalizer.normalize(name, Normalizer.Form.NFD);
    }
}
