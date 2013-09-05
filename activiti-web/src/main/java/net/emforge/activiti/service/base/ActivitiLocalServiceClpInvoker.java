package net.emforge.activiti.service.base;

import net.emforge.activiti.service.ActivitiLocalServiceUtil;

import java.util.Arrays;


public class ActivitiLocalServiceClpInvoker {
    private String _methodName20;
    private String[] _methodParameterTypes20;
    private String _methodName21;
    private String[] _methodParameterTypes21;
    private String _methodName24;
    private String[] _methodParameterTypes24;
    private String _methodName25;
    private String[] _methodParameterTypes25;
    private String _methodName26;
    private String[] _methodParameterTypes26;
    private String _methodName27;
    private String[] _methodParameterTypes27;
    private String _methodName28;
    private String[] _methodParameterTypes28;
    private String _methodName29;
    private String[] _methodParameterTypes29;

    public ActivitiLocalServiceClpInvoker() {
        _methodName20 = "getBeanIdentifier";

        _methodParameterTypes20 = new String[] {  };

        _methodName21 = "setBeanIdentifier";

        _methodParameterTypes21 = new String[] { "java.lang.String" };

        _methodName24 = "createNewModel";

        _methodParameterTypes24 = new String[] {
                "java.lang.String", "java.lang.String"
            };

        _methodName25 = "test";

        _methodParameterTypes25 = new String[] { "java.lang.String" };

        _methodName26 = "findAllExecutions";

        _methodParameterTypes26 = new String[] { "java.util.List" };

        _methodName27 = "findUniqueUserTaskNames";

        _methodParameterTypes27 = new String[] { "java.util.List" };

        _methodName28 = "findUniqueUserTaskAssignees";

        _methodParameterTypes28 = new String[] { "java.util.List" };

        _methodName29 = "findTopLevelProcessInstances";

        _methodParameterTypes29 = new String[] {
                "java.lang.String", "java.lang.String", "java.lang.String"
            };
    }

    public Object invokeMethod(String name, String[] parameterTypes,
        Object[] arguments) throws Throwable {
        if (_methodName20.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes20, parameterTypes)) {
            return ActivitiLocalServiceUtil.getBeanIdentifier();
        }

        if (_methodName21.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes21, parameterTypes)) {
            ActivitiLocalServiceUtil.setBeanIdentifier((java.lang.String) arguments[0]);
        }

        if (_methodName24.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes24, parameterTypes)) {
            return ActivitiLocalServiceUtil.createNewModel((java.lang.String) arguments[0],
                (java.lang.String) arguments[1]);
        }

        if (_methodName25.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes25, parameterTypes)) {
            return ActivitiLocalServiceUtil.test((java.lang.String) arguments[0]);
        }

        if (_methodName26.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes26, parameterTypes)) {
            return ActivitiLocalServiceUtil.findAllExecutions((java.util.List) arguments[0]);
        }

        if (_methodName27.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes27, parameterTypes)) {
            return ActivitiLocalServiceUtil.findUniqueUserTaskNames((java.util.List<java.lang.String>) arguments[0]);
        }

        if (_methodName28.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes28, parameterTypes)) {
            return ActivitiLocalServiceUtil.findUniqueUserTaskAssignees((java.util.List<java.lang.String>) arguments[0]);
        }

        if (_methodName29.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes29, parameterTypes)) {
            return ActivitiLocalServiceUtil.findTopLevelProcessInstances((java.lang.String) arguments[0],
                (java.lang.String) arguments[1], (java.lang.String) arguments[2]);
        }

        throw new UnsupportedOperationException();
    }
}
