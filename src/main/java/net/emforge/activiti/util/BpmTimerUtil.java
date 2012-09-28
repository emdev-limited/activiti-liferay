package net.emforge.activiti.util;

import java.util.Calendar;
import java.util.Date;

import org.springframework.stereotype.Service;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
/**
 * See {@link http://en.wikipedia.org/wiki/ISO_8601#Durations}
 * @author fav
 *
 */

@Service("bpmTimer")
public class BpmTimerUtil {
	
	private static Log _log = LogFactoryUtil.getLog(BpmTimerUtil.class);
	
	public static Date adjust(String offset, Date from) {
		//Incoming param should looks like +PT10H, so we need to parse it,
		//decide whether add value or subtract value and find out appropriate unit. Then
		//apply yyyy-MM-dd'T'HH:mm:ss pattern and return String
		if (offset == null || offset.isEmpty()) {
			throw new IllegalArgumentException("Offset is required");
		}
		boolean add = false;
		String sub = null;
		int offsetUnit = 0;
		int offsetValue = 0;
		int unitMultiplier = 1;
		if (offset.startsWith("-")) {
			add= false;
			sub = offset.substring(1);
		} else if (offset.startsWith("+")) {
			add= true;
			sub = offset.substring(1);
		} else {
			add= true;
			if (!offset.startsWith("P")) {
				_log.error(String.format("Wrong offset format [%s]", offset));
				throw new IllegalArgumentException("Wrong offset format");
			}
			sub = offset;
		}
		if (sub.startsWith("PT")) {
			if (offset.endsWith("S")) {
				offsetUnit = Calendar.SECOND;
			} else if (offset.endsWith("M")) {
				offsetUnit = Calendar.MINUTE;
			} else if (offset.endsWith("H")) {
				offsetUnit = Calendar.HOUR;
			} else {
				_log.error(String.format("Wrong offset format [%s]", offset));
				throw new IllegalArgumentException("Wrong offset format");
			}
			sub = sub.substring(2, sub.length() - 1);
			try {
				offsetValue = Integer.valueOf(sub);
			} catch (NumberFormatException e) {
				_log.error(String.format("Wrong offset format [%s]", offset));
				throw new IllegalArgumentException("Wrong offset format");
			}
		} else {
			//assume it starts with only P
			if (offset.endsWith("Y")) {
				offsetUnit = Calendar.YEAR;
			} else if (offset.endsWith("M")) {
				offsetUnit = Calendar.MONTH;
			} else if (offset.endsWith("W")) {
				offsetUnit = Calendar.HOUR;
				unitMultiplier = 168;
			} else if (offset.endsWith("D")) {
				offsetUnit = Calendar.HOUR;
				unitMultiplier = 24;
			} else {
				_log.error(String.format("Wrong offset format [%s]", offset));
				throw new IllegalArgumentException("Wrong offset format");
			}
			
			sub = sub.substring(1, sub.length() - 1);
			try {
				offsetValue = Integer.valueOf(sub);
			} catch (NumberFormatException e) {
				_log.error(String.format("Wrong offset format [%s]", offset));
				throw new IllegalArgumentException("Wrong offset format");
			}
		}
		
		offsetValue = unitMultiplier*offsetValue;
		
		Calendar cal = Calendar.getInstance();
		if (from != null) {
			cal.setTime(from);
		}
		if (!add) {
			offsetValue = -offsetValue;
		}
		cal.add(offsetUnit, offsetValue);
		
		return cal.getTime();
	}
	
	public static Date adjust(String offset) {
		return adjust(offset, null);
	}
}
