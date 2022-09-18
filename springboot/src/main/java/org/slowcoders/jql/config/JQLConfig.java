package org.slowcoders.jql.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import lombok.SneakyThrows;
import org.slowcoders.jql.jpa.JPAEntitySerializer;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.Entity;
import java.nio.charset.StandardCharsets;

@Configuration
public class JQLConfig {

    /**
     * Serialize 시 무한 루프 방지를 위한 Jackson 모듈
     * 참고) @JsonBackReference 와 @JsonIdentityInfo 대체
     */
    static class JQLModule extends Hibernate5Module {
        @Override
        public void setupModule(SetupContext context) {
            super.setupModule(context);
            context.addBeanSerializerModifier(new BeanSerializerModifier() {
                @Override
                public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc, JsonSerializer<?> serializer) {
                    Class<?> clazz = beanDesc.getBeanClass();
                    if (clazz.getAnnotation(Entity.class) != null) {
                        serializer = new JPAEntitySerializer(beanDesc, serializer);
                    }
                    return serializer;
                }
            });
        }
    }

    @Component
    @ConfigurationPropertiesBinding
    public static class StringToJsonNodeDeserializer implements Converter<String, ObjectNode> {

        private ObjectMapper om;

        public StringToJsonNodeDeserializer() {
            om = new ObjectMapper();
        }

        @SneakyThrows
        @Override
        public ObjectNode convert(String source) {
            if (source == null) return null;
            return (ObjectNode)om.readTree(source);
        }
    }



    @Component
    @ConfigurationPropertiesBinding
    public static class MultiPartFileToStringDeserializer implements Converter<MultipartFile, String> {

        public MultiPartFileToStringDeserializer() {
        }

        @SneakyThrows
        @Override
        public String convert(MultipartFile source) {
            if (source == null) return null;
            byte[] bytes = source.getBytes();
            String s = new String(bytes, StandardCharsets.UTF_8);
            return s;
        }
    }

    @Bean
    public Module datatypeHibernateModule() {
        Hibernate5Module hm = new JQLModule();

        hm.configure(Hibernate5Module.Feature.WRITE_MISSING_ENTITIES_AS_NULL, true);
        hm.configure(Hibernate5Module.Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS, true);
        return hm;
    }

//    @Bean
//    public JQLService jqlService(DataSource dataSource, TransactionTemplate transactionTemplate,
//                                 MappingJackson2HttpMessageConverter jsonConverter,
//                                 ConversionService conversionService,
//                                 RequestMappingHandlerMapping handlerMapping,
//                                 EntityManager entityManager,
//                                 EntityManagerFactory entityManagerFactory) throws Exception {
//        JQLService service = new JQLService(dataSource, transactionTemplate, jsonConverter, conversionService,
//                handlerMapping, entityManager, entityManagerFactory);
//        return service;
//    }
}

