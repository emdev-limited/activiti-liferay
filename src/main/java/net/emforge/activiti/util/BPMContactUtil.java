/**
 *
 */
package net.emforge.activiti.util;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.model.Address;
import com.liferay.portal.model.Contact;
import com.liferay.portal.model.Phone;
import com.liferay.portal.service.AddressLocalServiceUtil;
import com.liferay.portal.service.ContactLocalServiceUtil;
import com.liferay.portal.service.PhoneLocalServiceUtil;


/**
 * @author Khalutin Vladimir
 *
 */
@Service("bpmContact")
public class BPMContactUtil {
	private static Log _log = LogFactoryUtil.getLog("bpmContact");
	private static final String LIST_TYPE_INVALID = "invalid";
	private static final String LIST_TYPE_MOBILE_PHONE = "mobile-phone";
	
	private static long _getCompanyId(long contactId) throws PortalException, SystemException {
		return ContactLocalServiceUtil.getContact(contactId).getCompanyId();
	}
	
	public static Contact getContact(long contactId) {
		try {
			return ContactLocalServiceUtil.getContact(contactId);
		} catch (PortalException e) {
			_log.error("getContact FAILED!", e);
		} catch (SystemException e) {
			_log.error("getContact FAILED!", e);
		}
		return null;
	}
	
	public static List<Phone> getPhones(long contactId) {
		try {
			
			List<Phone> phones = PhoneLocalServiceUtil.getPhones(
					_getCompanyId(contactId), Contact.class.getName(), contactId);
			// remove invalid phones
			for(Phone phone : phones) {
				if(LIST_TYPE_INVALID.equalsIgnoreCase(phone.getType().getName())) {
					phones.remove(phone);
				}
			}
			return phones;
		} catch (SystemException e) {
			_log.error("getPhones FAILED!", e);
		} catch (PortalException e) {
			_log.error("getPhones FAILED!", e);
		}
		return new ArrayList<Phone>(0);
	}
	
	public static Phone getPrimaryPhone(long contactId) {
		List<Phone> phones = getPhones(contactId);
		for(Phone phone : phones) {
			if(phone.getPrimary()) {
				return phone;
			}
		}
		if(phones.size() > 0) {
			return phones.get(0);
		} else {
			return null;
		}
	}
	
	public static Phone getMobilePhone(long contactId) {
		List<Phone> phones = getPhones(contactId);
		try {
			for(Phone phone : phones) {
				if(phone.getType().getName().contains(LIST_TYPE_MOBILE_PHONE)) {
					return phone;
				}
			} 
		} catch (SystemException e) {
			_log.error("getMobilePhone FAILED!", e);
		} catch (PortalException e) {
			_log.error("getMobilePhone FAILED!", e);
		}
		return null;
	}
	
	public static List<Address> getAddresses(long contactId) {
		try {
			List<Address> addresses = AddressLocalServiceUtil.getAddresses(
					_getCompanyId(contactId), Contact.class.getName(), contactId);
			// remove invalid addresses
			for(Address address : addresses) {
				if(LIST_TYPE_INVALID.equalsIgnoreCase(address.getType().getName())) {
					addresses.remove(address);
				}
			}
			return addresses;
		} catch (SystemException e) {
			_log.error("getAddresses FAILED!", e);
		} catch (PortalException e) {
			_log.error("getAddresses FAILED!", e);
		}
		return new ArrayList<Address>(0);
	}
	
	public static Address getPrimaryAddress(long contactId) {
		List<Address> addresses = getAddresses(contactId);
		for(Address address : addresses) {
			if(address.getPrimary()) {
				return address;
			}
		}
		if(addresses.size() > 0) {
			return addresses.get(0);
		} else {
			return null;
		}
	}
/*	
	private static <T> List<T> _getEntries(long contactId, Class<T> clazz) {
		List<T> entries = new ArrayList<T>(0);
		ListType l; l.
		if(Address.class.getName().equals(clazz.getName())) {
			entries = (List<T>) AddressLocalServiceUtil.getAddresses(
					_getCompanyId(contactId), Contact.class.getName(), contactId);
		} else if (Phone.class.getName().equals(clazz.getName())) {
			entries = (List<T>) PhoneLocalServiceUtil.getAddresses(
					_getCompanyId(contactId), Contact.class.getName(), contactId);
		}
 
		// remove invalid addresses
		for(T t: entries) {
			if(LIST_TYPE_INVALID.equalsIgnoreCase(t.getType().getName())) {
				addresses.remove(address);
			}
		}
		return addresses;
		if(t instanceof Address) {
			
		}
		return null;
	}*/
}
