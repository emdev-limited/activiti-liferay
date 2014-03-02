package net.emforge.activiti.hook;

import java.util.ArrayList;
import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.activiti.engine.impl.bpmn.behavior.MailActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import com.liferay.mail.service.MailServiceUtil;
import com.liferay.portal.kernel.io.unsync.UnsyncStringWriter;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.mail.MailMessage;
import com.liferay.portal.kernel.util.PrefsPropsUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

/** This is implementation of Mail Behavior implemented sending email via Liferay Service
 * 
 * As result, Liferay's settings (as well as implementation at all) used for sending email
 * 
 * @author akakunin
 *
 */
public class LiferayMailActivityBehavior extends MailActivityBehavior  {
	private static final long serialVersionUID = 4581033173970339063L;
	private static Log _log = LogFactoryUtil.getLog(LiferayMailActivityBehavior.class);

	@Override
	public void execute(ActivityExecution execution) {
		_log.debug("Execute Email Task");
		
	    String toStr = getStringFromField(to, execution);
	    String fromStr = getStringFromField(from, execution);
	    String ccStr = getStringFromField(cc, execution);
	    String bccStr = getStringFromField(bcc, execution);
	    String subjectStr = getStringFromField(subject, execution);
	    String textStr = getStringFromField(text, execution);
	    String htmlStr = getStringFromField(html, execution);
	    
	    InternetAddress[] internetAddressesFrom = getEmailAddresses(fromStr);
	    InternetAddress internetAddressFrom = internetAddressesFrom != null && internetAddressesFrom.length > 0 ? internetAddressesFrom[0] : null;
	    
	    InternetAddress[] internetAddressesTo = getEmailAddresses(toStr);
	    InternetAddress[] internetAddressesCc = getEmailAddresses(ccStr);
	    InternetAddress[] internetAddressesBcc = getEmailAddresses(bccStr);
	    
	    String body = StringUtils.isNotBlank(htmlStr) ? htmlStr : textStr;
	    boolean isHtml = StringUtils.isNotBlank(htmlStr);
	    
	    // call velocity engine to process subject
	    subjectStr = processBody(subjectStr, execution);
	    // call velocity engine for body to substitute variables
	    body = processBody(body, execution);
	    
	    Long companyId = null;
	    try {
	    	companyId = Long.valueOf((String)execution.getVariable(WorkflowConstants.CONTEXT_COMPANY_ID));
	    } catch (Exception ex) {}
	    
	    sendEmail(internetAddressFrom, internetAddressesTo, internetAddressesCc, internetAddressesBcc, subjectStr, body, isHtml, companyId);
	    
	    leave(execution);
	}
	
	
	private String processBody(String body, ActivityExecution execution) {
	    // evaluate template
	    try {
			Velocity.init();

			VelocityContext velocityContext = new VelocityContext();
	    	
		    // copy variables from execution
		    for (String variableName : execution.getVariableNames()) {
		    	velocityContext.put(variableName, execution.getVariable(variableName));
		    }
	    
			UnsyncStringWriter unsyncStringWriter = new UnsyncStringWriter();

			Velocity.evaluate(velocityContext, unsyncStringWriter, this.getClass().getName(), body);	    	
	    	
	    	String result = unsyncStringWriter.toString();
	    	return result;
	    } catch (Exception ex) {
	    	_log.warn("Cannot process body: " + ex.getMessage());
	    	_log.debug("Cannot process body",ex);
	    	
	    	return body;
	    }
	}


	protected InternetAddress[] getEmailAddresses(String str) {
		if (StringUtils.isBlank(str)) {
			return null;
		}
		
		String[] emails = str.split(",");
		List<InternetAddress> emailAddresses = new ArrayList<InternetAddress>();
		
		for(String email : emails) {
			try {
				emailAddresses.add(new InternetAddress(email));
			} catch (AddressException e) {
				_log.error("Failed to get email address: " + e);
			}
		}
		
		InternetAddress[] addresses = new InternetAddress[emailAddresses.size()];
		return emailAddresses.toArray(addresses);
	}
	
	/** Send email with using Liferay functionality
	 * 
	 * @param internetAddressesTo
	 * @param internetAddressesCc
	 * @param internetAddressesBcc
	 * @param internetAddressFrom
	 * @param subject
	 * @param body
	 * @param isHtml
	 */
	protected void sendEmail(InternetAddress internetAddressFrom,
								InternetAddress[] internetAddressesTo, 
								 InternetAddress[] internetAddressesCc,
								 InternetAddress[] internetAddressesBcc,
								 String subject, 
								 String body, 
								 boolean isHtml,
								 Long companyId) {

		if (internetAddressFrom == null) {
			try {
				String fromAddr = PropsUtil.get(PropsKeys.ADMIN_EMAIL_FROM_ADDRESS);
				String fromName = PropsUtil.get(PropsKeys.ADMIN_EMAIL_FROM_NAME);
				
				if (companyId != null && companyId > 0l) {
					fromAddr = PrefsPropsUtil.getString(companyId, PropsKeys.ADMIN_EMAIL_FROM_ADDRESS);
	                fromName = PrefsPropsUtil.getString(companyId, PropsKeys.ADMIN_EMAIL_FROM_NAME);
				}
			
			
				internetAddressFrom = new InternetAddress(fromAddr, fromName);
			} catch (Exception e) {
				_log.error(String
						.format("Error occured, while trying to create internet address: %s", e.getMessage()));
				return;
			}
		}

		// always send mail one-by-one
		for (InternetAddress ia : internetAddressesTo) {
			MailMessage mailMessage = new MailMessage();
			
			mailMessage.setFrom(internetAddressFrom);
			mailMessage.setBody(body);
			mailMessage.setSubject(subject);
			mailMessage.setHTMLFormat(isHtml);

			InternetAddress[] iAddresses = new InternetAddress[1];
			iAddresses[0] = ia;
			mailMessage.setTo(iAddresses);
			
			// set CC & BCC
			if (internetAddressesCc != null) {
				mailMessage.setCC(internetAddressesCc);
			}
			if (internetAddressesBcc != null) {
				mailMessage.setBCC(internetAddressesBcc);
			}
			MailServiceUtil.sendEmail(mailMessage);
		}

		_log.info("Notification e-mail to addresses "
				+ ArrayUtils.toString(internetAddressesTo)
				+ " has been sent successfully");
	}

}
