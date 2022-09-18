package org.slowcoders.jql.jdbc;

import lombok.SneakyThrows;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.FixedValue;
import org.slowcoders.jql.util.AttributeNameConverter;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.MethodIntrospector;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

public class DynamicMappingHelper extends RequestMappingHandlerMapping {
    private final RequestMappingHandlerMapping handlerMapping;
    private final DefaultListableBeanFactory beanFactory;

    public DynamicMappingHelper(RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
        this.beanFactory = (DefaultListableBeanFactory) handlerMapping.getApplicationContext().getAutowireCapableBeanFactory();
    }

    private void registerHandlerMethods(Object bean) {
        Class<?> beanType = bean.getClass();
        Map<Method, RequestMappingInfo> methods = MethodIntrospector.selectMethods(beanType, (MethodIntrospector.MetadataLookup<RequestMappingInfo>) (method) -> {
            try {
                return super.getMappingForMethod(method, beanType);
            } catch (Throwable var4) {
                throw new IllegalStateException("Invalid mapping on handler class [" + beanType.getName() + "]: " + method, var4);
            }
        });

        methods.forEach((method, mapping) -> {
            Method invocableMethod = AopUtils.selectInvocableMethod(method, beanType);
            handlerMapping.registerMapping(mapping, bean, invocableMethod);
        });
    }

    private Object registerBean(Class<?> beanType) {
        Object bean = beanFactory.autowire(beanType, 3, true);
        // 참고) bean.toString 으로 등록하여야만, springdoc swagger 에 등록됨.
        beanFactory.registerSingleton(bean.toString(), bean);
        return bean;
    }

    public void addRestController(String tableName) {
        Class<?> ctrlType = DynamicController.makeSubClass(tableName);
        Object controller = this.registerBean(ctrlType);
        this.registerHandlerMethods(controller);

    }

    public abstract static class DynamicController extends JdbcController<Object> {

        private JQLJdbcService service;

        public DynamicController(JQLJdbcService service) {
            super(null);
            this.service = service;
            super.setRepository(this.service.makeRepository(getTableName()));
        }

        protected abstract String getTableName();

        static String toClassName(String snakeCaseName) {
            String name = AttributeNameConverter.camelCaseConverter.toLogicalAttributeName(snakeCaseName);
            return Character.toUpperCase(name.charAt(0)) + name.substring(1);
        }

        @SneakyThrows
        static Class<?> makeSubClass(String name) {
            Class<?> bean = new ByteBuddy()
                    .subclass(DynamicController.class, ConstructorStrategy.Default.IMITATE_SUPER_CLASS)
                    .name("org.slowcoders.jql." + toClassName(name) + "JqlController")
                    .annotateType(AnnotationDescription.Builder
                            .ofType(RestController.class)
                            .build())
                    .annotateType(AnnotationDescription.Builder
                            .ofType(RequestMapping.class)
                            .defineArray("value", "/api/v2/data/" + name)
                            .build())

                    .defineMethod("getTableName", String.class, Modifier.PUBLIC)
                    .intercept(FixedValue.value(name))

                    .make()
                    .load(DynamicController.class.getClassLoader())
                    .getLoaded();
            return bean;
        }
    }
}
