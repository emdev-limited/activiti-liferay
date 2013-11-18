package org.activiti.engine.impl.variable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.impl.util.IoUtil;

/**
 * @author Aleksandr Zhdanov
 */
public class SerializableType extends ByteArrayType {

	public static final String TYPE_NAME = "serializable";

	private static final long serialVersionUID = 1L;

	public String getTypeName() {
		return TYPE_NAME;
	}

	public Object getValue(ValueFields valueFields) {

		Object cachedObject = valueFields.getCachedValue();
		if (cachedObject != null) {
			return cachedObject;
		}
		byte[] bytes = (byte[]) super.getValue(valueFields);
		if (bytes == null) {
			//this situation is possible for historic variables - 
			//when you do e.g. execution.removeVariable in a Script task the var is finally removed from ACT_RU_VARIABLE
			//but it remains with null value in the ACT_HI_VARINST(in ACT_GE_BYTEARRAY)
			return null;
		}
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		Object deserializedObject;
		try {
			ObjectInputStream ois = new CustomObjectInputStream(bais);
			deserializedObject = ois.readObject();
			valueFields.setCachedValue(deserializedObject);

			if (valueFields instanceof VariableInstanceEntity) {
				Context.getCommandContext().getDbSqlSession().addDeserializedObject(deserializedObject, bytes, (VariableInstanceEntity) valueFields);
			}

		} catch (Exception e) {
			throw new ActivitiException("coudn't deserialize object in variable '" + valueFields.getName() + "'", e);
		} finally {
			IoUtil.closeSilently(bais);
		}
		return deserializedObject;
	}

	public void setValue(Object value, ValueFields valueFields) {
		byte[] byteArray = serialize(value, valueFields);
		valueFields.setCachedValue(value);

		if (valueFields.getByteArrayValue() == null) {
			if (valueFields instanceof VariableInstanceEntity) {
				Context.getCommandContext().getDbSqlSession().addDeserializedObject(valueFields.getCachedValue(), byteArray, (VariableInstanceEntity) valueFields);
			}
		}

		super.setValue(byteArray, valueFields);
	}

	public static byte[] serialize(Object value, ValueFields valueFields) {
		if (value == null) {
			return null;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream ois = null;
		try {
			ois = new ObjectOutputStream(baos);
			ois.writeObject(value);
		} catch (Exception e) {
			throw new ActivitiException("coudn't serialize value '" + value + "' in variable '" + valueFields.getName() + "'", e);
		} finally {
			IoUtil.closeSilently(ois);
		}
		return baos.toByteArray();
	}

	public boolean isAbleToStore(Object value) {
		return value instanceof Serializable;
	}
}
