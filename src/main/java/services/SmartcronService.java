package services;

import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;

import de.chrgroth.smartcron.Smartcrons;
import de.chrgroth.smartcron.api.Smartcron;
import de.chrgroth.smartcron.model.SmartcronMetadata;
import ninja.lifecycle.Dispose;
import ninja.lifecycle.Start;

@Singleton
public class SmartcronService {
    private static final Logger LOG = LoggerFactory.getLogger(SmartcronService.class);

    private Smartcrons smartcrons;

    @Start
    public void startup() {
        LOG.info("initializing smartcrons...");
        smartcrons = new Smartcrons("smartcron");
        LOG.info("started smartcrons service.");
    }

    // TODO save metric events after each execution (implement as part of smartcron)
    public void schedule(Smartcron smartcron) {
        LOG.info("schedulinging smartcron: " + smartcron);
        smartcrons.schedule(smartcron);
    }

    public void executeNowAndReschedule(Class<? extends Smartcron> type) {
        deactivate(type);
        activate(type);
    }

    public void activate(Class<? extends Smartcron> type) {
        LOG.info("activating smartcron instances of type " + type.getName());
        smartcrons.activate(type);
    }

    public void deactivate(Class<? extends Smartcron> type) {
        LOG.info("deactivating smartcron instances of type " + type.getName());
        smartcrons.deactivate(type);
    }

    public Set<SmartcronMetadata> metadata() {
        return smartcrons.getMetadata().stream().filter(m -> !m.getHistory().isEmpty()).collect(Collectors.toSet());
    }

    @Dispose
    public void shutdown() {
        LOG.info("stopping smartcrons...");
        smartcrons.shutdown();
        LOG.info("stopped smartcron service.");
    }
}
