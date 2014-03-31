package net.emforge.activiti.service;

import com.liferay.portal.service.InvokableLocalService;

/**
 * @author Brian Wing Shun Chan
 * @generated
 */
public class ActivitiLocalServiceClp implements ActivitiLocalService {
    private InvokableLocalService _invokableLocalService;
    private String _methodName0;
    private String[] _methodParameterTypes0;
    private String _methodName1;
    private String[] _methodParameterTypes1;
    private String _methodName3;
    private String[] _methodParameterTypes3;
    private String _methodName4;
    private String[] _methodParameterTypes4;
    private String _methodName5;
    private String[] _methodParameterTypes5;
    private String _methodName6;
    private String[] _methodParameterTypes6;
    private String _methodName7;
    private String[] _methodParameterTypes7;
    private String _methodName8;
    private String[] _methodParameterTypes8;
    private String _methodName9;
    private String[] _methodParameterTypes9;
    private String _methodName10;
    private String[] _methodParameterTypes10;
    private String _methodName11;
    private String[] _methodParameterTypes11;
    private String _methodName12;
    private String[] _methodParameterTypes12;
    private String _methodName13;
    private String[] _methodParameterTypes13;
    private String _methodName14;
    private String[] _methodParameterTypes14;

    public ActivitiLocalServiceClp(InvokableLocalService invokableLocalService) {
        _invokableLocalService = invokableLocalService;

        _methodName0 = "getBeanIdentifier";

        _methodParameterTypes0 = new String[] {  };

        _methodName1 = "setBeanIdentifier";

        _methodParameterTypes1 = new String[] { "java.lang.String" };

        _methodName3 = "createNewModel";

        _methodParameterTypes3 = new String[] {
                "java.lang.String", "java.lang.String"
            };

        _methodName4 = "test";

        _methodParameterTypes4 = new String[] { "java.lang.String" };

        _methodName5 = "findAllExecutions";

        _methodParameterTypes5 = new String[] { "java.util.List" };

        _methodName6 = "findUniqueUserTaskNames";

        _methodParameterTypes6 = new String[] { "java.util.List" };

        _methodName7 = "findUniqueUserTaskAssignees";

        _methodParameterTypes7 = new String[] { "java.util.List" };

        _methodName8 = "findTopLevelProcessInstances";

        _methodParameterTypes8 = new String[] {
                "java.lang.String", "java.lang.String", "java.lang.String"
            };

        _methodName9 = "findTopLevelProcess";

        _methodParameterTypes9 = new String[] { "java.lang.String" };

        _methodName10 = "suspendWorkflowInstance";

        _methodParameterTypes10 = new String[] { "long", "long" };

        _methodName11 = "resumeWorkflowInstance";

        _methodParameterTypes11 = new String[] { "long", "long" };

        _methodName12 = "stopWorkflowInstance";

        _methodParameterTypes12 = new String[] {
                "long", "long", "long", "java.lang.String"
            };

        _methodName13 = "addWorkflowInstanceComment";

        _methodParameterTypes13 = new String[] {
                "long", "long", "long", "long", "int", "java.lang.String"
            };

        _methodName14 = "findHistoricActivityByName";

        _methodParameterTypes14 = new String[] {
                "java.lang.String", "java.lang.String"
            };
    }

    @Override
    public java.lang.String getBeanIdentifier() {
        Object returnObj = null;

        try {
            returnObj = _invokableLocalService.invokeMethod(_methodName0,
                    _methodParameterTypes0, new Object[] {  });
        } catch (Throwable t) {
            t = ClpSerializer.translateThrowable(t);

            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else {
                throw new RuntimeException(t.getClass().getName() +
                    " is not a valid exception");
            }
        }

        return (java.lang.String) ClpSerializer.translateOutput(returnObj);
    }

    @Override
    public void setBeanIdentifier(java.lang.String beanIdentifier) {
        try {
            _invokableLocalService.invokeMethod(_methodName1,
                _methodParameterTypes1,
                new Object[] { ClpSerializer.translateInput(beanIdentifier) });
        } catch (Throwable t) {
            t = ClpSerializer.translateThrowable(t);

            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else {
                throw new RuntimeException(t.getClass().getName() +
                    " is not a valid exception");
            }
        }
    }

    @Override
    public java.lang.Object invokeMethod(java.lang.String name,
        java.lang.String[] parameterTypes, java.lang.Object[] arguments)
        throws java.lang.Throwable {
        throw new UnsupportedOperationException();
    }

    @Override
    public java.lang.String createNewModel(java.lang.String modelName,
        java.lang.String modelDescription)
        throws com.liferay.portal.kernel.exception.PortalException,
            com.liferay.portal.kernel.exception.SystemException {
        Object returnObj = null;

        try {
            returnObj = _invokableLocalService.invokeMethod(_methodName3,
                    _methodParameterTypes3,
                    new Object[] {
                        ClpSerializer.translateInput(modelName),
                        
                    ClpSerializer.translateInput(modelDescription)
                    });
        } catch (Throwable t) {
            t = ClpSerializer.translateThrowable(t);

            if (t instanceof com.liferay.portal.kernel.exception.PortalException) {
                throw (com.liferay.portal.kernel.exception.PortalException) t;
            }

            if (t instanceof com.liferay.portal.kernel.exception.SystemException) {
                throw (com.liferay.portal.kernel.exception.SystemException) t;
            }

            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else {
                throw new RuntimeException(t.getClass().getName() +
                    " is not a valid exception");
            }
        }

        return (java.lang.String) ClpSerializer.translateOutput(returnObj);
    }

    @Override
    public java.lang.String test(java.lang.String s)
        throws com.liferay.portal.kernel.exception.PortalException,
            com.liferay.portal.kernel.exception.SystemException {
        Object returnObj = null;

        try {
            returnObj = _invokableLocalService.invokeMethod(_methodName4,
                    _methodParameterTypes4,
                    new Object[] { ClpSerializer.translateInput(s) });
        } catch (Throwable t) {
            t = ClpSerializer.translateThrowable(t);

            if (t instanceof com.liferay.portal.kernel.exception.PortalException) {
                throw (com.liferay.portal.kernel.exception.PortalException) t;
            }

            if (t instanceof com.liferay.portal.kernel.exception.SystemException) {
                throw (com.liferay.portal.kernel.exception.SystemException) t;
            }

            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else {
                throw new RuntimeException(t.getClass().getName() +
                    " is not a valid exception");
            }
        }

        return (java.lang.String) ClpSerializer.translateOutput(returnObj);
    }

    @Override
    public java.util.List<java.lang.String> findAllExecutions(
        java.util.List instanceIds)
        throws com.liferay.portal.kernel.exception.SystemException {
        Object returnObj = null;

        try {
            returnObj = _invokableLocalService.invokeMethod(_methodName5,
                    _methodParameterTypes5,
                    new Object[] { ClpSerializer.translateInput(instanceIds) });
        } catch (Throwable t) {
            t = ClpSerializer.translateThrowable(t);

            if (t instanceof com.liferay.portal.kernel.exception.SystemException) {
                throw (com.liferay.portal.kernel.exception.SystemException) t;
            }

            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else {
                throw new RuntimeException(t.getClass().getName() +
                    " is not a valid exception");
            }
        }

        return (java.util.List<java.lang.String>) ClpSerializer.translateOutput(returnObj);
    }

    @Override
    public java.util.Set<java.lang.String> findUniqueUserTaskNames(
        java.util.List<java.lang.String> executionIds)
        throws com.liferay.portal.kernel.exception.SystemException {
        Object returnObj = null;

        try {
            returnObj = _invokableLocalService.invokeMethod(_methodName6,
                    _methodParameterTypes6,
                    new Object[] { ClpSerializer.translateInput(executionIds) });
        } catch (Throwable t) {
            t = ClpSerializer.translateThrowable(t);

            if (t instanceof com.liferay.portal.kernel.exception.SystemException) {
                throw (com.liferay.portal.kernel.exception.SystemException) t;
            }

            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else {
                throw new RuntimeException(t.getClass().getName() +
                    " is not a valid exception");
            }
        }

        return (java.util.Set<java.lang.String>) ClpSerializer.translateOutput(returnObj);
    }

    @Override
    public java.util.Set findUniqueUserTaskAssignees(
        java.util.List<java.lang.String> executionIds)
        throws com.liferay.portal.kernel.exception.SystemException {
        Object returnObj = null;

        try {
            returnObj = _invokableLocalService.invokeMethod(_methodName7,
                    _methodParameterTypes7,
                    new Object[] { ClpSerializer.translateInput(executionIds) });
        } catch (Throwable t) {
            t = ClpSerializer.translateThrowable(t);

            if (t instanceof com.liferay.portal.kernel.exception.SystemException) {
                throw (com.liferay.portal.kernel.exception.SystemException) t;
            }

            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else {
                throw new RuntimeException(t.getClass().getName() +
                    " is not a valid exception");
            }
        }

        return (java.util.Set) ClpSerializer.translateOutput(returnObj);
    }

    @Override
    public java.util.List<java.lang.String> findTopLevelProcessInstances(
        java.lang.String taskName, java.lang.String assigneeUser,
        java.lang.String candidateRole)
        throws com.liferay.portal.kernel.exception.SystemException {
        Object returnObj = null;

        try {
            returnObj = _invokableLocalService.invokeMethod(_methodName8,
                    _methodParameterTypes8,
                    new Object[] {
                        ClpSerializer.translateInput(taskName),
                        
                    ClpSerializer.translateInput(assigneeUser),
                        
                    ClpSerializer.translateInput(candidateRole)
                    });
        } catch (Throwable t) {
            t = ClpSerializer.translateThrowable(t);

            if (t instanceof com.liferay.portal.kernel.exception.SystemException) {
                throw (com.liferay.portal.kernel.exception.SystemException) t;
            }

            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else {
                throw new RuntimeException(t.getClass().getName() +
                    " is not a valid exception");
            }
        }

        return (java.util.List<java.lang.String>) ClpSerializer.translateOutput(returnObj);
    }

    @Override
    public java.lang.String findTopLevelProcess(java.lang.String taskId)
        throws com.liferay.portal.kernel.exception.SystemException {
        Object returnObj = null;

        try {
            returnObj = _invokableLocalService.invokeMethod(_methodName9,
                    _methodParameterTypes9,
                    new Object[] { ClpSerializer.translateInput(taskId) });
        } catch (Throwable t) {
            t = ClpSerializer.translateThrowable(t);

            if (t instanceof com.liferay.portal.kernel.exception.SystemException) {
                throw (com.liferay.portal.kernel.exception.SystemException) t;
            }

            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else {
                throw new RuntimeException(t.getClass().getName() +
                    " is not a valid exception");
            }
        }

        return (java.lang.String) ClpSerializer.translateOutput(returnObj);
    }

    @Override
    public boolean suspendWorkflowInstance(long companyId,
        long workflowInstanceId)
        throws com.liferay.portal.kernel.workflow.WorkflowException {
        Object returnObj = null;

        try {
            returnObj = _invokableLocalService.invokeMethod(_methodName10,
                    _methodParameterTypes10,
                    new Object[] { companyId, workflowInstanceId });
        } catch (Throwable t) {
            t = ClpSerializer.translateThrowable(t);

            if (t instanceof com.liferay.portal.kernel.workflow.WorkflowException) {
                throw (com.liferay.portal.kernel.workflow.WorkflowException) t;
            }

            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else {
                throw new RuntimeException(t.getClass().getName() +
                    " is not a valid exception");
            }
        }

        return ((Boolean) returnObj).booleanValue();
    }

    @Override
    public boolean resumeWorkflowInstance(long companyId,
        long workflowInstanceId)
        throws com.liferay.portal.kernel.workflow.WorkflowException {
        Object returnObj = null;

        try {
            returnObj = _invokableLocalService.invokeMethod(_methodName11,
                    _methodParameterTypes11,
                    new Object[] { companyId, workflowInstanceId });
        } catch (Throwable t) {
            t = ClpSerializer.translateThrowable(t);

            if (t instanceof com.liferay.portal.kernel.workflow.WorkflowException) {
                throw (com.liferay.portal.kernel.workflow.WorkflowException) t;
            }

            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else {
                throw new RuntimeException(t.getClass().getName() +
                    " is not a valid exception");
            }
        }

        return ((Boolean) returnObj).booleanValue();
    }

    @Override
    public boolean stopWorkflowInstance(long companyId, long userId,
        long workflowInstanceId, java.lang.String comment)
        throws com.liferay.portal.kernel.workflow.WorkflowException {
        Object returnObj = null;

        try {
            returnObj = _invokableLocalService.invokeMethod(_methodName12,
                    _methodParameterTypes12,
                    new Object[] {
                        companyId,
                        
                    userId,
                        
                    workflowInstanceId,
                        
                    ClpSerializer.translateInput(comment)
                    });
        } catch (Throwable t) {
            t = ClpSerializer.translateThrowable(t);

            if (t instanceof com.liferay.portal.kernel.workflow.WorkflowException) {
                throw (com.liferay.portal.kernel.workflow.WorkflowException) t;
            }

            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else {
                throw new RuntimeException(t.getClass().getName() +
                    " is not a valid exception");
            }
        }

        return ((Boolean) returnObj).booleanValue();
    }

    @Override
    public void addWorkflowInstanceComment(long companyId, long userId,
        long workflowInstanceId, long workflowTaskId, int logType,
        java.lang.String comment)
        throws com.liferay.portal.kernel.workflow.WorkflowException {
        try {
            _invokableLocalService.invokeMethod(_methodName13,
                _methodParameterTypes13,
                new Object[] {
                    companyId,
                    
                userId,
                    
                workflowInstanceId,
                    
                workflowTaskId,
                    
                logType,
                    
                ClpSerializer.translateInput(comment)
                });
        } catch (Throwable t) {
            t = ClpSerializer.translateThrowable(t);

            if (t instanceof com.liferay.portal.kernel.workflow.WorkflowException) {
                throw (com.liferay.portal.kernel.workflow.WorkflowException) t;
            }

            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else {
                throw new RuntimeException(t.getClass().getName() +
                    " is not a valid exception");
            }
        }
    }

    @Override
    public java.util.List<java.lang.String> findHistoricActivityByName(
        java.lang.String topProcessInstanceId, java.lang.String activityName)
        throws com.liferay.portal.kernel.exception.SystemException {
        Object returnObj = null;

        try {
            returnObj = _invokableLocalService.invokeMethod(_methodName14,
                    _methodParameterTypes14,
                    new Object[] {
                        ClpSerializer.translateInput(topProcessInstanceId),
                        
                    ClpSerializer.translateInput(activityName)
                    });
        } catch (Throwable t) {
            t = ClpSerializer.translateThrowable(t);

            if (t instanceof com.liferay.portal.kernel.exception.SystemException) {
                throw (com.liferay.portal.kernel.exception.SystemException) t;
            }

            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else {
                throw new RuntimeException(t.getClass().getName() +
                    " is not a valid exception");
            }
        }

        return (java.util.List<java.lang.String>) ClpSerializer.translateOutput(returnObj);
    }
}
