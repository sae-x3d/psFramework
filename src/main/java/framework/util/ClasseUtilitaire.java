package framework.util;

import framework.annotations.Controller;
import framework.annotations.UrlMapping;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ClasseUtilitaire {

    public static Map<String, Map<String, Method>> getUrlMappingMap(String basePackage) {
        ConfigurationBuilder config = new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(basePackage))
                .setScanners(Scanners.TypesAnnotated);

        Reflections reflections = new Reflections(config);
        Set<Class<?>> controllerClasses = reflections.getTypesAnnotatedWith(Controller.class);

        Map<String, Map<String, Method>> controllerMap = new HashMap<>();

        for (Class<?> clazz : controllerClasses) {
            Controller controllerAnn = clazz.getAnnotation(Controller.class);
            if (controllerAnn == null) {
                continue;
            }
            String controllerName = controllerAnn.value();

            Map<String, Method> methodMap = new HashMap<>();
            for (Method method : clazz.getDeclaredMethods()) {
                UrlMapping urlAnn = method.getAnnotation(UrlMapping.class);
                if (urlAnn != null) {
                    String urlPattern = urlAnn.value();
                    methodMap.put(urlPattern, method);
                }
            }
            if (!methodMap.isEmpty()) {
                controllerMap.put(controllerName, methodMap);
            }
        }

        return controllerMap;
    }
}
