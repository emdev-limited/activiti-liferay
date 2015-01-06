package net.emforge.activiti.service.base;

import net.emforge.activiti.service.ActivitiLocalServiceUtil;

import java.util.Arrays;

/**
 * @author Brian Wing Shun Chan
 * @generated
 */
public class ActivitiLocalServiceClpInvoker {
    private String _methodName16;
    private String[] _methodParameterTypes16;
    private String _methodName17;
    private String[] _methodParameterTypes17;
    private String _methodName20;
    private String[] _methodParameterTypes20;
    private String _methodName21;
    private String[] _methodParameterTypes21;
    private String _methodName22;
    private String[] _methodParameterTypes22;
    private String _methodName24;
    private String[] _methodParameterTypes24;
    private String _methodName25;
    private String[] _methodParameterTypes25;
    private String _methodName26;
    private String[] _methodParameterTypes26;
    private String _methodName27;
    private String[] _methodParameterTypes27;
    private String _methodName30;
    private String[] _methodParameterTypes30;
    private String _methodName31;
    private String[] _methodParameterTypes31;
    private String _methodName32;
    private String[] _methodParameterTypes32;
    private String _methodName33;
    private String[] _methodParameterTypes33;
    private String _methodName34;
    private String[] _methodParameterTypes34;

    public ActivitiLocalServiceClpInvoker() {
        _methodName16 = "getBeanIdentifier";

        _methodParameterTypes16 = new String[] {  };

        _methodName17 = "setBeanIdentifier";

        _methodParameterTypes17 = new String[] { "java.lang.String" };

        _methodName20 = "createNewModel";

        _methodParameterTypes20 = new String[] {
                "java.lang.String", "java.lang.String"
            };

        _methodName21 = "test";

        _methodParameterTypes21 = new String[] { "java.lang.String" };

        _methodName22 = "findAllExecutions";

        _methodParameterTypes22 = new String[] { "java.util.List" };

        _methodName24 = "findUniqueUserTaskNames";

        _methodParameterTypes24 = new String[] { "java.util.List" };

        _methodName25 = "findUniqueUserTaskAssignees";

        _methodParameterTypes25 = new String[] { "java.util.List" };

        _methodName26 = "findTopLevelProcessInstances";

        _methodParameterTypes26 = new String[] {
                "java.lang.String", "java.lang.String", "java.lang.String"
            };

        _methodName27 = "findTopLevelProcess";

        _methodParameterTypes27 = new String[] { "java.lang.String" };

        _methodName30 = "suspendWorkflowInstance";

        _methodParameterTypes30 = new String[] { "long", "long" };

        _methodName31 = "resumeWorkflowInstance";

        _methodParameterTypes31 = new String[] { "long", "long" };

        _methodName32 = "stopWorkflowInstance";

        _methodParameterTypes32 = new String[] {
                "long", "long", "long", "java.lang.String"
            };

        _methodName33 = "addWorkflowInstanceComment";

        _methodParameterTypes33 = new String[] {
                "long", "long", "long", "long", "int", "java.lang.String"
            };

        _methodName34 = "findHistoricActivityByName";

        _methodParameterTypes34 = new String[] {
                "java.lang.String", "java.lang.String"
            };
    }

    public Object invokeMethod(String name, String[] parameterTypes,
        Object[] arguments) throws Throwable {
        if (_methodName16.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes16, parameterTypes)) {
            return ActivitiLocalServiceUtil.getBeanIdentifier();
        }

        if (_methodName17.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes17, parameterTypes)) {
            ActivitiLocalServiceUtil.setBeanIdentifier((java.lang.String) arguments[0]);

            return null;
        }

        if (_methodName20.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes20, parameterTypes)) {
            return ActivitiLocalServiceUtil.createNewModel((java.lang.String) arguments[0],
                (java.lang.String) arguments[1]);
        }

        if (_methodName21.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes21, parameterTypes)) {
            return ActivitiLocalServiceUtil.test((java.lang.String) arguments[0]);
        }

        if (_methodName22.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes22, parameterTypes)) {
            return ActivitiLocalServiceUtil.findAllExecutions((java.util.List) arguments[0]);
        }

        if (_methodName24.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes24, parameterTypes)) {
            return ActivitiLocalServiceUtil.findUniqueUserTaskNames((java.util.List<java.lang.String>) arguments[0]);
        }

        if (_methodName25.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes25, parameterTypes)) {
            return ActivitiLocalServiceUtil.findUniqueUserTaskAssignees((java.util.List<java.lang.String>) arguments[0]);
        }

        if (_methodName26.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes26, parameterTypes)) {
            return ActivitiLocalServiceUtil.findTopLevelProcessInstances((java.lang.String) arguments[0],
                (java.lang.String) arguments[1], (java.lang.String) arguments[2]);
        }

        if (_methodName27.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes27, parameterTypes)) {
            return ActivitiLocalServiceUtil.findTopLevelProcess((java.lang.String) arguments[0]);
        }

        if (_methodName30.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes30, parameterTypes)) {
            return ActivitiLocalServiceUtil.suspendWorkflowInstance(((Long) arguments[0]).longValue(),
                ((Long) arguments[1]).longValue());
        }

        if (_methodName31.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes31, parameterTypes)) {
            return ActivitiLocalServiceUtil.resumeWorkflowInstance(((Long) arguments[0]).longValue(),
                ((Long) arguments[1]).longValue());
        }

        if (_methodName32.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes32, parameterTypes)) {
            return ActivitiLocalServiceUtil.stopWorkflowInstance(((Long) arguments[0]).longValue(),
                ((Long) arguments[1]).longValue(),
                ((Long) arguments[2]).longValue(),
                (java.lang.String) arguments[3]);
        }

        if (_methodName33.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes33, parameterTypes)) {
            ActivitiLocalServiceUtil.addWorkflowInstanceComment(((Long) arguments[0]).longValue(),
                ((Long) arguments[1]).longValue(),
                ((Long) arguments[2]).longValue(),
                ((Long) arguments[3]).longValue(),
                ((Integer) arguments[4]).intValue(),
                (java.lang.String) arguments[5]);

            return null;
        }

        if (_methodName34.equals(name) &&
                Arrays.deepEquals(_methodParameterTypes34, parameterTypes)) {
            return ActivitiLocalServiceUtil.findHistoricActivityByName((java.lang.String) arguments[0],
                (java.lang.String) arguments[1]);
        }

        throw new UnsupportedOperationException();
    }
}
