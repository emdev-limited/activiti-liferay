package net.emforge.activiti.comparator;

import java.lang.reflect.Method;

import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.query.Query;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.TaskInfoQueryWrapper;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.StringPool;

/**
 * Applies comparator to queries
 * 
 * @author Dmitry Farafonov
 *
 */
public class WorkflowComparatorUtil {
	private static final String _ORDER_BY_ASC = " ASC";
	private static final String _ORDER_BY_DESC = " DESC";
	
	private static Log _log = LogFactoryUtil.getLog(WorkflowComparatorUtil.class);

	/**
	 * <p>
	 * Applies comparator to query.
	 * <p>
	 * Unified liferay's comparator may be used: 
	 * <blockquote><pre>
	 * OrderByComparator unifiedComparator = OrderByComparatorFactoryUtil.create(null,
	 * 		&quot;DeleteReason&quot;, false, &quot;TaskCreateTime&quot;, false, &quot;TaskName&quot;, true);
	 * </pre></blockquote>
	 * <p>
	 * Comparator's column name equals existing {@code orderBy} method name 
	 * (see {@link org.activiti.engine.task.TaskInfoQuery TaskInfoQuery})
	 * without {@code orderBy} substring. Example: to apply orderBy method
	 * {@code TaskQuery.orderByTaskCreateTime()} use "TaskCreateTime" as column
	 * name.
	 * 
	 * @param taskInfoQueryWrapper
	 * @param orderByComparator
	 */
	public static void applyComparator(
			TaskInfoQueryWrapper taskInfoQueryWrapper,
			OrderByComparator orderByComparator) {
		if (taskInfoQueryWrapper.getTaskInfoQuery() instanceof HistoricTaskInstanceQuery) {
			_log.debug("1");
		} else {
			_log.debug("2");
		}
		_applyComparator(taskInfoQueryWrapper.getTaskInfoQuery(), orderByComparator);
	}
	
	public static void applyComparator(Query<?, ?> query, OrderByComparator orderByComparator) {
		_applyComparator(query, orderByComparator);
	}
	
	/*public static void applyComparator(ProcessInstanceQuery processInstanceQuery, OrderByComparator orderByComparator) {
		_applyComparator(processInstanceQuery, orderByComparator);
	}

	public static void applyComparator(HistoricProcessInstanceQuery processInstanceQuery, OrderByComparator orderByComparator) {
		_applyComparator(processInstanceQuery, orderByComparator);
	}*/

	private static void _applyComparator(Object query, OrderByComparator orderByComparator) {
		if (orderByComparator != null) {
			String[] orderFields = orderByComparator.getOrderByFields();
			for (String field : orderFields) {
				String methodName = "orderBy" + field;
				try {
					Method orderByMethod = query.getClass().getMethod(methodName);
					orderByMethod.invoke(query);
					if (isAscending(orderByComparator, field)) {
						Method ascendingMethod = query.getClass().getMethod("asc");
						ascendingMethod.invoke(query);
					} else {
						Method descendingMethod = query.getClass().getMethod("desc");
						descendingMethod.invoke(query);
					}
				} catch (NoSuchMethodException e) {
					_log.warn("Ordering '" + methodName + "' is not implemented for " + query.getClass().getName());
				} catch (Exception e) {
					_log.error("Error in applying orders: " + query.getClass().getName() + "." + methodName + "", e);
				}
			}
		}
	}

	private static boolean isAscending(OrderByComparator orderByComparator, String field) {
		String orderBy = orderByComparator.getOrderBy();

		if (orderBy == null) {
			return false;
		}

		int x = orderBy.indexOf(
			field + StringPool.SPACE);

		if (x == -1) {
			return false;
		}

		int y = orderBy.indexOf(_ORDER_BY_ASC, x);

		if (y == -1) {
			return false;
		}

		int z = orderBy.indexOf(_ORDER_BY_DESC, x);

		if ((z >= 0) && (z < y)) {
			return false;
		}
		else {
			return true;
		}
	}

}
