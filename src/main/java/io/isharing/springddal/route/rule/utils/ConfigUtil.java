package io.isharing.springddal.route.rule.utils;

import java.util.Properties;

import io.isharing.springddal.route.exception.ConfigurationException;

public class ConfigUtil {

    public static String filter(String text) {
        return filter(text, System.getProperties());
    }

    public static String filter(String text, Properties properties) {
        StringBuilder s = new StringBuilder();
        int cur = 0;
        int textLen = text.length();
        int propStart = -1;
        int propStop = -1;
        String propName = null;
        String propValue = null;
        for (; cur < textLen; cur = propStop + 1) {
            propStart = text.indexOf("${", cur);
            if (propStart < 0) {
                break;
            }
            s.append(text.substring(cur, propStart));
            propStop = text.indexOf("}", propStart);
            if (propStop < 0) {
                throw new ConfigurationException("Unterminated property: " + text.substring(propStart));
            }
            propName = text.substring(propStart + 2, propStop);
            propValue = properties.getProperty(propName);
            if (propValue == null) {
                s.append("${").append(propName).append('}');
            } else {
                s.append(propValue);
            }
        }
        return s.append(text.substring(cur)).toString();
    }

}