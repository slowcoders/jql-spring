package org.eipgrid.jql.jpa;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.eipgrid.jql.JqlQuery;
import org.eipgrid.jql.schema.QResultMapping;

import javax.persistence.Entity;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class JqlPropertyWriter extends BeanPropertyWriter {
    private static final String JQL_STACK_KEY = "jql-entity-stack";
    private static final String JQL_RESULT_MAPPING_KEY = "jql-result-mapping";
    private final BeanPropertyWriter writer;

    public JqlPropertyWriter(BeanPropertyWriter writer) {
        super(writer);
        this.writer = writer;
    }

    @Override
    public void serializeAsElement(Object bean, JsonGenerator gen,
                                   SerializerProvider prov) throws Exception {
        super.serializeAsElement(bean, gen, prov);
    }

    @Override
    public void serializeAsField(Object bean,
                                 JsonGenerator gen,
                                 SerializerProvider prov) throws Exception {
        JavaType valueType = writer.getType();
        if (valueType.getContentType() != null) {
            valueType = valueType.getContentType();
        }

        QResultMapping stack = (QResultMapping) prov.getAttribute(JQL_STACK_KEY);
        if (stack == null) {
            if (bean instanceof JqlQuery.Response) {
                prov.setAttribute(JQL_RESULT_MAPPING_KEY, ((JqlQuery.Response)bean).getResultMapping());
            }
            stack = (QResultMapping) prov.getAttribute(JQL_RESULT_MAPPING_KEY);
            prov.setAttribute(JQL_STACK_KEY, stack);
        } else if (valueType.getRawClass().getAnnotation(Entity.class) != null) {
            QResultMapping child = stack.getChildMapping(this.getName());
            if (child == null) return;
            prov.setAttribute(JQL_STACK_KEY, child);
            super.serializeAsField(bean, gen, prov);
            prov.setAttribute(JQL_STACK_KEY, stack);
            return;
        }
        super.serializeAsField(bean, gen, prov);
    }

    public boolean isEmpty(SerializerProvider provider, Object value) {
        return value == null || getStack(provider).contains(value);
    }

    private Stack<Object> getStack(SerializerProvider provider) {
        Stack<Object> stack = (Stack<Object>) provider.getAttribute(JQL_STACK_KEY);
        if (stack == null) {
            stack = new Stack<>();
            provider.setAttribute(JQL_STACK_KEY, stack);
        }
        return stack;
    }

}