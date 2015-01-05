package net.emforge.activiti;

import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;

import org.activiti.engine.impl.util.ReflectUtil;
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

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

/**
 * Custom {@link SpringProcessEngineConfiguration} implementation to load own classloader and database configurations.
 *
 * @author akakunin
 * @author Oliver Teichmann, PRODYNA AG
 * @author Dmitry Farafonov
 *
 */
public class LiferayProcessEngineConfiguration extends SpringProcessEngineConfiguration {
	private static Log _log = LogFactoryUtil.getLog(LiferayProcessEngineConfiguration.class);

	public static final String EXT_MYBATIS_MAPPING_FILE = "activiti-liferay.ibatis.mem.conf.xml";

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

	protected void initSessionFactories() {
		super.initSessionFactories();
		//TODO init custom model manager
	}

	protected Configuration initMybatisConfiguration(Environment environment, Reader reader, Properties properties) {
		XMLConfigBuilder parser = new XMLConfigBuilder(reader,"", properties);
		Configuration configuration = parser.getConfiguration();
		configuration.setEnvironment(environment);

		try {
			parseAdditionalMappers(configuration, getExtMyBatisXmlConfigurationSteam(), "", properties);
		} catch (Exception e) {
			_log.error(e, e);
		}

		initMybatisTypeHandlers(configuration);
		initCustomMybatisMappers(configuration);

		configuration = parseMybatisConfiguration(configuration, parser);
		return configuration;
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
}
