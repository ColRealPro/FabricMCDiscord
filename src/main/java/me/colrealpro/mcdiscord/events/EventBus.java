package me.colrealpro.mcdiscord.events;

import me.colrealpro.mcdiscord.MCDiscord;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class EventBus {
    private static final Logger LOGGER = LoggerFactory.getLogger("EventBus");
    private final List<Object> registeredObjects = new ArrayList<>();
    private static EventBus instance;
    private boolean debugEnabled;

    private EventBus() {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
        .setUrls(ClasspathHelper.forPackage("me.colrealpro.mcdiscord"))
        .setScanners(new MethodAnnotationsScanner()));

        Set<Method> methods = reflections.getMethodsAnnotatedWith(EventHandler.class);
        for (Method method : methods) {
            try {
                register(method.getDeclaringClass().getDeclaredConstructor().newInstance());
                break; // prevent the same class from being registered multiple times -_-
            } catch (Exception e) {
                throw new RuntimeException("Failed to register event handler", e);
            }
        }

        this.debugEnabled = MCDiscord.config.getDirectConfig().getBoolean("Debug", false);
    }

    public static EventBus getInstance() {
        if (instance == null) {
            instance = new EventBus();
        }
        return instance;
    }

    public void register(Object object) {
        if (debugEnabled) {
            LOGGER.debug("Registering event handler {}", object.getClass().getSimpleName());
        }

        registeredObjects.add(object);
    }

    public void dispatch(CancellableEvent event) {
        if (debugEnabled) {
            LOGGER.debug("Dispatching event {} to {} listeners", event.getClass().getSimpleName(), registeredObjects.size());
        }

        for (Object obj : registeredObjects) {
            for (Method method : obj.getClass().getMethods()) {
                if (method.isAnnotationPresent(EventHandler.class) && method.getParameterCount() == 1
                && method.getParameters()[0].getType().isAssignableFrom(event.getClass())) {
                    try {
                        if (debugEnabled) {
                            LOGGER.debug("Dispatching event {} to {}", event.getClass().getSimpleName(), obj.getClass().getSimpleName());
                        }

                        method.invoke(obj, event);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to dispatch event", e);
                    }
                }
            }
        }
    }
}
