/*
 * */
package com.synectiks.process.server.inputs.converters;

import com.google.common.collect.Maps;
import com.synectiks.process.server.ConfigurationException;
import com.synectiks.process.server.inputs.converters.CsvConverter;

import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class CsvConverterTest {
    @Test
    public void testConfigHandling() throws ConfigurationException {
        Map<String, Object> configMap = Maps.newHashMap();
        assertConfigException(configMap);

        configMap.put("column_header", null);
        assertConfigException(configMap);

        configMap.put("column_header", "");
        assertConfigException(configMap);

        // the rest of the fields have defaults
        configMap.put("column_header", "field1");

        CsvConverter csvConverter = new CsvConverter(configMap);
        assertNotNull(csvConverter);
    }

    @Test
    public void testSuccessfulConversion() throws ConfigurationException {
        Map<String, Object> configMap = Maps.newHashMap();
        configMap.put("column_header", "f1,f2");
        CsvConverter csvConverter = new CsvConverter(configMap);
        @SuppressWarnings("unchecked")
        Map<String, String> result = (Map<String, String>) csvConverter.convert("\"content1\",\"cont\\\\ent3\"");
        assertEquals("content1", result.get("f1"));
        assertEquals("cont\\ent3", result.get("f2"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testEdgeCases() throws ConfigurationException {
        Map<String, Object> configMap = Maps.newHashMap();
        configMap.put("column_header", "f1,f2");
        CsvConverter csvConverter = new CsvConverter(configMap);
        String resultString = (String) csvConverter.convert("");
        assertEquals("", resultString);

        // too few fields
        Map<String, String> result = (Map<String, String>) csvConverter.convert("field1");
        assertNull("Too few fields in data doesn't work", result);

        // too many fields
        result = (Map<String, String>) csvConverter.convert("field1,field2,field3");
        assertNull("Too many fields in data doesn't work", result);

        // unclosed quote level
        result = (Map<String, String>) csvConverter.convert("field1,field2,\"field3");
        assertNull("Unbalanced quoting does not work", result);
    }

    private void assertConfigException(Map<String, Object> configMap) {
        assertThatExceptionOfType(ConfigurationException.class).isThrownBy(() -> new CsvConverter(configMap));
    }
}
