package net.emforge.activiti.util;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.UnicodeProperties;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Util class for reading non-ascii encoded property values
 */
public class UnicodePropsUtil {

    private static final Log _log = LogFactoryUtil.getLog(UnicodePropsUtil.class);

    private static UnicodeProperties props;

    /**
     * Get property from portal-ext.properties file
     * @param key - property key
     * @return string value of the property
     */
    public static String get(final String key) {
        if (props == null) {
            props = load();
        }
        return props.getProperty(key);
    }

    private static UnicodeProperties load() {
        UnicodeProperties properties = new UnicodeProperties();
        try {
            properties.load(
                    readFile(com.liferay.portal.kernel.util.PropsUtil
                            .get(PropsKeys.LIFERAY_HOME) + File.separator + "portal-ext.properties"));
        } catch (IOException e) {
            _log.error("error loading custom props");
        }
        return properties;
    }

    private static String readFile(final String file) throws IOException {
    	BufferedReader reader = null;
    	StringBuilder sb = null;
    	try {
    		reader = new BufferedReader(new FileReader(file));
            String line;
            sb = new StringBuilder();

            while((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append(System.getProperty("line.separator"));
            }
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
        
        return sb == null ? StringPool.BLANK : sb.toString();
    }
}