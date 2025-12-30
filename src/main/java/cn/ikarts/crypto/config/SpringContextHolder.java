package cn.ikarts.crypto.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SpringContextHolder implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) {
        SpringContextHolder.applicationContext = context;
    }

    public static ApplicationContext getContext() {
        if (applicationContext == null) {
            throw new IllegalStateException("Spring ApplicationContext 未初始化");
        }
        return applicationContext;
    }

    public static <T> Map<String, T> getBeansOfType(Class<T> type) {
        return getContext().getBeansOfType(type);
    }
}
