package com.fsphere.liferay.ext;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.Phone;
/**
 * Extension for PhoneImpl to make possible to serialize it to JSON
 * cause original object could not be serialized 
 * (bean com.liferay.portal.model.impl.PhoneImpl bean com.liferay.portal.model.impl.ListTypeImpl can't invoke getExpandoBridge: null)
 * 
 * @author fav
 *
 */
@XmlRootElement
public class PhoneResponse {
	
	public PhoneResponse() {}
	
	public PhoneResponse(Phone phone) throws SystemException {
		this._phoneId 	= phone.getPhoneId();
		this._companyId = phone.getCompanyId();
		this._userId = phone.getUserId();
		this._userUuid = phone.getUserUuid();
		this._userName = phone.getUserName();
		this._createDate = phone.getCreateDate();
		this._modifiedDate = phone.getModifiedDate();
		this._classNameId = phone.getClassNameId();
		this._classPK = phone.getClassPK();
		this._number = phone.getNumber();
		this._extension = phone.getExtension();
		this._typeId = phone.getTypeId();
		this._primary = phone.getPrimary();
		
	}
	
	private long _phoneId;
	private long _companyId;
	private long _userId;
	private String _userUuid;
	private String _userName;
	private Date _createDate;
	private Date _modifiedDate;
	private long _classNameId;
	private long _classPK;
	private String _number;
	private String _extension;
	private int _typeId;
	private boolean _primary;
	
	public long getPhoneId() {
		return _phoneId;
	}

	public void setPhoneId(long _phoneId) {
		this._phoneId = _phoneId;
	}

	public long getCompanyId() {
		return _companyId;
	}

	public void setCompanyId(long _companyId) {
		this._companyId = _companyId;
	}

	public long getUserId() {
		return _userId;
	}

	public void setUserId(long _userId) {
		this._userId = _userId;
	}

	public String getUserUuid() {
		return _userUuid;
	}

	public void setUserUuid(String _userUuid) {
		this._userUuid = _userUuid;
	}

	public String getUserName() {
		return _userName;
	}

	public void setUserName(String _userName) {
		this._userName = _userName;
	}

	public Date getCreateDate() {
		return _createDate;
	}

	public void setCreateDate(Date _createDate) {
		this._createDate = _createDate;
	}

	public Date getModifiedDate() {
		return _modifiedDate;
	}

	public void setModifiedDate(Date _modifiedDate) {
		this._modifiedDate = _modifiedDate;
	}

	public long getClassNameId() {
		return _classNameId;
	}

	public void setClassNameId(long _classNameId) {
		this._classNameId = _classNameId;
	}

	public long getClassPK() {
		return _classPK;
	}

	public void setClassPK(long _classPK) {
		this._classPK = _classPK;
	}

	public String getNumber() {
		return _number;
	}

	public void setNumber(String _number) {
		this._number = _number;
	}

	public String getExtension() {
		return _extension;
	}

	public void setExtension(String _extension) {
		this._extension = _extension;
	}

	public int getTypeId() {
		return _typeId;
	}

	public void setTypeId(int _typeId) {
		this._typeId = _typeId;
	}

	public boolean isPrimary() {
		return _primary;
	}

	public void setPrimary(boolean _primary) {
		this._primary = _primary;
	}

}
