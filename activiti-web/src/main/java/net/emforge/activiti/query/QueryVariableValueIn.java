package net.emforge.activiti.query;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.impl.QueryOperator;
import org.activiti.engine.impl.QueryVariableValue;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.impl.variable.VariableType;
import org.activiti.engine.impl.variable.VariableTypes;
import org.apache.commons.collections.CollectionUtils;

/**
 * 
 * @author Dmitry Farafonov
 */
public class QueryVariableValueIn extends QueryVariableValue {
	private static final long serialVersionUID = 1L;
	private String name;
	private List values;
	private String type;

	private List<VariableInstanceEntity> variableInstanceEntities;

	public QueryVariableValueIn(String name, List values,
			QueryOperator operator, boolean local) {
		super(name, values, operator, local);
		this.name = name;
		this.values = values;
	}

	public QueryVariableValueIn(String name, List values, boolean local) {
		super(name, values, null, local);
		this.name = name;
		this.values = values;
	}

	public void initialize(VariableTypes types) {
		if (variableInstanceEntities == null) {
			if (CollectionUtils.isNotEmpty(values)) {
				variableInstanceEntities = new ArrayList<VariableInstanceEntity>();
				for (Object value : values) {
					VariableType type = types.findVariableType(value);
					VariableInstanceEntity variableInstanceEntity = VariableInstanceEntity.create(name, type,
							value);
					variableInstanceEntities.add(variableInstanceEntity);
				}
				type = variableInstanceEntities.get(0).getType().getTypeName();
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public List getList() {
		if (variableInstanceEntities != null) {
			return (List) variableInstanceEntities;
		}
		return null;
	}

	@Override
	public String getType() {
		if (variableInstanceEntities != null && !"null".equals(type)) {
			return type;
		}
		return null;
	}
}
