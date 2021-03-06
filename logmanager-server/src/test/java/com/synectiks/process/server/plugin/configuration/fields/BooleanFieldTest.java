/*
 * */
package com.synectiks.process.server.plugin.configuration.fields;

import org.junit.Test;

import com.synectiks.process.server.plugin.configuration.fields.BooleanField;
import com.synectiks.process.server.plugin.configuration.fields.ConfigurationField;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class BooleanFieldTest {

    @Test
    public void testGetFieldType() throws Exception {
        BooleanField f = new BooleanField("name", "Name", false, "description");
        assertEquals(BooleanField.FIELD_TYPE, f.getFieldType());
    }

    @Test
    public void testGetName() throws Exception {
        BooleanField f = new BooleanField("name", "Name", false, "description");
        assertEquals("name", f.getName());
    }

    @Test
    public void testGetHumanName() throws Exception {
        BooleanField f = new BooleanField("name", "Name", false, "description");
        assertEquals("Name", f.getHumanName());
    }

    @Test
    public void testGetDescription() throws Exception {
        BooleanField f = new BooleanField("name", "Name", false, "description");
        assertEquals("description", f.getDescription());
    }

    @Test
    public void testGetDefaultValue() throws Exception {
        BooleanField f = new BooleanField("name", "Name", true, "description");
        assertEquals(true, f.getDefaultValue());
    }

    @Test
    public void testIsOptional() throws Exception {
        BooleanField f = new BooleanField("name", "Name", true, "description");
        assertEquals(ConfigurationField.Optional.OPTIONAL, f.isOptional());
    }

    @Test
    public void testGetAttributes() throws Exception {
        // Boolean field has no attributes.
        BooleanField f = new BooleanField("name", "Name", true, "description");
        assertNotNull(f.getAttributes());
    }

}
