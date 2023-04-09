package org.slowcoders.hyperql.jpa;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import org.slowcoders.hyperql.LeafProperty;
import org.slowcoders.hyperql.RestTemplate;
import org.slowcoders.hyperql.HyperSelect;
import org.slowcoders.hyperql.js.JsType;

import javax.persistence.Id;

public class JpaPropertyFilter extends BeanPropertyWriter {
    private static final String JQL_RESULT_MAPPING_KEY = RestTemplate.Response.JpaFilter.JQL_RESULT_MAPPING_KEY;
    private static final String JQL_INCLUDE_ID = "hql-include-id";

    private final BeanPropertyWriter writer;
    private final boolean isId;
    private final boolean isLeaf;

    public JpaPropertyFilter(BeanPropertyWriter writer) {
        super(writer);
        this.writer = writer;
        this.isId = writer.getAnnotation(Id.class) != null;
        this.isLeaf = writer.getAnnotation(LeafProperty.class) != null ||
                JsType.of(writer.getType().getRawClass()).isPrimitive();
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

        HyperSelect.ResultMap mapping = (HyperSelect.ResultMap) prov.getAttribute(JQL_RESULT_MAPPING_KEY);
        if (mapping != null) {
            boolean include_id = prov.getAttribute(JQL_INCLUDE_ID) != null && (Boolean) prov.getAttribute(JQL_INCLUDE_ID);
            String p_name = this.getName();
            Object column = mapping.get(p_name);
            if (column == null) {
                if (this.isId) {
                    if (!include_id && !mapping.isIdSelected()) return;
                }
                else if (!isLeaf || !mapping.isAllLeafSelected()) {
                    return;
                }
            }
            else if (!isLeaf) {
                prov.setAttribute(JQL_RESULT_MAPPING_KEY, column);
                Boolean id_required = (this.getType().getContentType() != null);
                if (id_required != include_id) {
                    prov.setAttribute(JQL_INCLUDE_ID, id_required);
                }
                writer.serializeAsField(bean, gen, prov);
                if (id_required != include_id) {
                    prov.setAttribute(JQL_INCLUDE_ID, include_id);
                }
                prov.setAttribute(JQL_RESULT_MAPPING_KEY, mapping);
                return;
            }
        }
        writer.serializeAsField(bean, gen, prov);
    }
}