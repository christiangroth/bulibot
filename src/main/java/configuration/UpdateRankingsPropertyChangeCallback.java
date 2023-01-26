package configuration;

import java.time.LocalDateTime;

import com.google.inject.Inject;

import de.chrgroth.smartcron.api.Smartcron;
import de.chrgroth.smartcron.api.SmartcronExecutionContext;
import services.BulibotService;

public class UpdateRankingsPropertyChangeCallback implements Smartcron {

    @Inject
    private BulibotService bulibotService;

    @Override
    public boolean executionHistory() {
        return false;
    }

    @Override
    public LocalDateTime run(SmartcronExecutionContext context) {

        // update rankings
        bulibotService.updateRankings();

        // just run once
        return null;
    }
}
