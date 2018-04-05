package org.softwire.training.checks;

import com.codahale.metrics.health.HealthCheck;
import org.softwire.training.rawImageStore.RawImageDao;

public class S3Check extends HealthCheck {

    private final RawImageDao rawImageDao;

    public S3Check(RawImageDao rawImageDao) {
        this.rawImageDao = rawImageDao;
    }

    @Override
    protected Result check() {
        rawImageDao.ping();
        return Result.healthy();
    }
}
