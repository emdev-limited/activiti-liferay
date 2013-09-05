package net.emforge.activiti.service.base;

import net.emforge.activiti.service.ActivitiLocalServiceUtil;

import java.util.Arrays;


public class ActivitiLocalServiceClpInvoker {
    private String _methodName18;
    private String[] _methodParameterTypes18;
    private String _methodName19;
    private String[] _methodParameterTypes19;
    private String _methodName22;
    private String[] _methodParameterTypes22;
    private String _methodName23;
    private String[] _methodParameterTypes23;
    private String _methodName24;
    private String[] _methodParameterTypes24;
    private String _methodName25;
    private String[] _methodParameterTypes25;

    public ActivitiLocalServiceClpInvoker() {
        _methodName18 = "getBeanIdentifier";

        _methodParameterTypes18 = new String[] {  };

        _methodName19 = "setBeanIdentifier";

        _methodParameterTypes19 = new String[] { "java.lang.String" };

        _methodName22 = "createNewModel";

        _methodParameterTypes22 = new String[] {
                "java.lang.String", "java.lang.String"
            };

        _methodName23 = "test";

        _methodParameterTypes23 = new String[] { "java.lang.String" };

        _methodName24 = "findUniqueUserTaskNames";

        _methodParameterTypes24 = new String[] { "java.util.List" };

        _methodName25 = "findUniqueUserTaskAssignees";

        _methodParameterTypes25 = new String[] { "java.util.List" };
    }

    public Object invokeMethod(String name, String[] parameterTypes,
        Object[] arguments) throws Throwable {
        if (_methodName18.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes18, parameterTypes)) {
            return ActivitiLocalServiceUtil.getBeanIdentifier();
        }

        if (_methodName19.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes19, parameterTypes)) {
            ActivitiLocalServiceUtil.setBeanIdentifier((java.lang.String) arguments[0]);
        }

        if (_methodName22.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes22, parameterTypes)) {
            return ActivitiLocalServiceUtil.createNewModel((java.lang.String) arguments[0],
                (java.lang.String) arguments[1]);
        }

        if (_methodName23.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes23, parameterTypes)) {
            return ActivitiLocalServiceUtil.test((java.lang.String) arguments[0]);
        }

        if (_methodName24.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes24, parameterTypes)) {
            return ActivitiLocalServiceUtil.findUniqueUserTaskNames((java.util.List<java.lang.Long>) arguments[0]);
        }

        if (_methodName25.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes25, parameterTypes)) {
            return ActivitiLocalServiceUtil.findUniqueUserTaskAssignees((java.util.List<java.lang.Long>) arguments[0]);
        }

        throw new UnsupportedOperationException();
    }
}
