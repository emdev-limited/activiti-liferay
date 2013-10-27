package net.emforge.activiti;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ImplicitListeners;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.db.DbSqlSessionFactory;
import org.activiti.engine.impl.db.IbatisVariableTypeHandler;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.impl.util.ReflectUtil;
import org.activiti.engine.impl.variable.VariableType;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.builder.xml.XMLMapperEntityResolver;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.apache.ibatis.type.JdbcType;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

/**
 * Custom {@link SpringProcessEngineConfiguration} implementation to load own classloader and database configurations.
 *
 * @author akakunin
 * @author Oliver Teichmann, PRODYNA AG
 *
 */
public class LiferayProcessEngineConfiguration extends SpringProcessEngineConfiguration
	implements ImplicitListeners
{
	private static Log _log = LogFactoryUtil.getLog(LiferayProcessEngineConfiguration.class);
	
	public static final String EXT_MYBATIS_MAPPING_FILE = "activiti-liferay.ibatis.mem.conf.xml";
	
	public Map<String, List<TaskListener>> implicitUserTaskListeners = new HashMap<String, List<TaskListener>>(0);
	
	public LiferayProcessEngineConfiguration() {
		//replace taskService with own implementation 
//		taskService = new LiferayTaskServiceImpl();
		classLoader = new DatabaseClassloader(this.getClass().getClassLoader());
	}
	
	@Override
	public void initDatabaseType() {
		// add mapping for HSQL Database - map it into H2 (should work)
		databaseTypeMappings.setProperty("HSQL Database Engine","h2");
		super.initDatabaseType();
	}
	
	protected InputStream getExtMyBatisXmlConfigurationSteam() {
		return ReflectUtil.getResourceAsStream(EXT_MYBATIS_MAPPING_FILE);
	}
	
//	@Override
//	protected Collection<? extends Deployer> getDefaultDeployers() {
//		List<Deployer> defaultDeployers = new ArrayList<Deployer>();
//
//	    BpmnDeployer bpmnDeployer = new BpmnDeployer();
//	    bpmnDeployer.setExpressionManager(expressionManager);
//	    bpmnDeployer.setIdGenerator(idGenerator);
//	    
//	    if (bpmnParseFactory == null) {
//	      bpmnParseFactory = new DefaultBpmnParseFactory();
//	    }
//	    
//	    if (activityBehaviorFactory == null) {
//	      DefaultActivityBehaviorFactory defaultActivityBehaviorFactory = new DefaultActivityBehaviorFactory();
//	      defaultActivityBehaviorFactory.setExpressionManager(expressionManager);
//	      activityBehaviorFactory = defaultActivityBehaviorFactory;
//	    }
//	    
//	    if (listenerFactory == null) {
//	      DefaultListenerFactory defaultListenerFactory = new DefaultListenerFactory();
//	      defaultListenerFactory.setExpressionManager(expressionManager);
//	      listenerFactory = defaultListenerFactory;
//	    }
//	    
//	    BpmnParser bpmnParser = new LiferayBpmnParser();
//	    bpmnParser.setExpressionManager(expressionManager);
//	    bpmnParser.setBpmnParseFactory(bpmnParseFactory);
//	    bpmnParser.setActivityBehaviorFactory(activityBehaviorFactory);
//	    bpmnParser.setListenerFactory(listenerFactory);
//	    
//	    List<BpmnParseHandler> parseHandlers = new ArrayList<BpmnParseHandler>();
//	    if(getPreBpmnParseHandlers() != null) {
//	      parseHandlers.addAll(getPreBpmnParseHandlers());
//	    }
//	    parseHandlers.addAll(getDefaultBpmnParseHandlers());
//	    if(getPostBpmnParseHandlers() != null) {
//	      parseHandlers.addAll(getPostBpmnParseHandlers());
//	    }
//	    
//	    BpmnParseHandlers bpmnParseHandlers = new BpmnParseHandlers();
//	    bpmnParseHandlers.addHandlers(parseHandlers);
//	    bpmnParser.setBpmnParserHandlers(bpmnParseHandlers);
//	    
//	    bpmnDeployer.setBpmnParser(bpmnParser);
//	    
//	    defaultDeployers.add(bpmnDeployer);
//	    return defaultDeployers;
//	}
	
	protected void initSessionFactories() {
		super.initSessionFactories();
		//TODO init custom model manager
	}
	
	protected void initSqlSessionFactory() {
	    if (sqlSessionFactory==null) {
	      InputStream inputStream = null;
	      try {
	        inputStream = getMyBatisXmlConfigurationSteam();

	        // update the jdbc parameters to the configured ones...
	        Environment environment = new Environment("default", transactionFactory, dataSource);
	        Reader reader = new InputStreamReader(inputStream);
	        Properties properties = new Properties();
	        properties.put("prefix", databaseTablePrefix);
	        if(databaseType != null) {
	          properties.put("limitBefore" , DbSqlSessionFactory.databaseSpecificLimitBeforeStatements.get(databaseType));
	          properties.put("limitAfter" , DbSqlSessionFactory.databaseSpecificLimitAfterStatements.get(databaseType));
	          properties.put("limitBetween" , DbSqlSessionFactory.databaseSpecificLimitBetweenStatements.get(databaseType));
	          properties.put("orderBy" , DbSqlSessionFactory.databaseSpecificOrderByStatements.get(databaseType));
	        }
	        XMLConfigBuilder parser = new XMLConfigBuilder(reader,"", properties);
	        Configuration configuration = parser.getConfiguration();
	        configuration.setEnvironment(environment);
	        configuration.getTypeHandlerRegistry().register(VariableType.class, JdbcType.VARCHAR, new IbatisVariableTypeHandler());
	        configuration = parser.parse();

	        try {
				parseAdditionalMappers(configuration, getExtMyBatisXmlConfigurationSteam(), "", properties);
			} catch (Exception e) {
				_log.error(e, e);
			}
	        
	        sqlSessionFactory = new DefaultSqlSessionFactory(configuration);

	      } catch (Exception e) {
	        throw new ActivitiException("Error while building ibatis SqlSessionFactory: " + e.getMessage(), e);
	      } finally {
	        IoUtil.closeSilently(inputStream);
	      }
	    }
	  }
	
	protected void parseAdditionalMappers(Configuration configuration, InputStream inputStream, String environment, Properties props) throws Exception {
		if (inputStream == null) {
			return;
		}
		XPathParser parser = new XPathParser(inputStream, true, props, new XMLMapperEntityResolver());
		XNode parent = parser.evalNode("/configuration").evalNode("mappers");
		for (XNode child : parent.getChildren()) {
			String resource = child.getStringAttribute("resource");
			ErrorContext.instance().resource(resource);
            InputStream is = Resources.getResourceAsStream(resource);
            XMLMapperBuilder mapperParser = new XMLMapperBuilder(is, configuration, resource, configuration.getSqlFragments());
            mapperParser.parse();
		}
	}

	public List<TaskListener> getImplicitUserTaskListenersFor(String taskEventName) 
	{
		List<TaskListener> lst = implicitUserTaskListeners.get(taskEventName);
		return lst;
	}

	public Map<String, List<TaskListener>> getImplicitUserTaskListeners() {
		return implicitUserTaskListeners;
	}

	public void setImplicitUserTaskListeners(
			Map<String, List<TaskListener>> implicitUserTaskListeners) {
		this.implicitUserTaskListeners = implicitUserTaskListeners;
	}
}
