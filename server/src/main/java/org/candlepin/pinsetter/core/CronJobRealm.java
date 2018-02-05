/**
 * Copyright (c) 2009 - 2018 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package org.candlepin.pinsetter.core;

import org.candlepin.common.config.Configuration;
import org.candlepin.model.JobCurator;

import org.quartz.JobKey;
import org.quartz.JobListener;
import org.quartz.SchedulerException;
import org.quartz.TriggerListener;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.spi.JobFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.inject.Inject;

/**
 * A JobRealm for jobs that are run on a regular schedule.
 */
public class CronJobRealm extends AbstractJobRealm {
    private static final Logger log = LoggerFactory.getLogger(CronJobRealm.class);

    protected static final String[] GROUPS = new String[] {
        JobType.CRON.getGroupName(),
        JobType.UTIL.getGroupName()
    };

    protected static final String[] DELETED_JOBS = new String[] {
        "StatisticHistoryTask",
        "ExportCleaner"
    };

    @Inject
    public CronJobRealm(Configuration config, JobCurator jobCurator, JobFactory jobFactory, JobListener
        jobListener, TriggerListener triggerListener, StdSchedulerFactory stdSchedulerFactory)
        throws InstantiationException {
        this.config = config;
        this.jobCurator = jobCurator;

        Properties props = config.subset("org.quartz").toProperties();
        configure(props, stdSchedulerFactory, jobFactory, jobListener, triggerListener);
        try {
            purgeDeprecatedJobs();
        }
        catch (SchedulerException e) {
            log.error("Could not create CronJobRealm", e);
            throw new InstantiationException("Could not create CronJobRealm");
        }
    }

    @Override
    public List<String> getRealmGroups() {
        return Arrays.asList(GROUPS);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    protected boolean isClustered() {
        boolean clustered = false;
        if (config.containsKey("org.quartz.jobStore.isClustered")) {
            clustered = config.getBoolean("org.quartz.jobStore.isClustered");
        }
        return clustered;
    }

    @Override
    public void unpause() throws SchedulerException {
        log.debug("restarting cron scheduler");
        try {
            scheduler.start();
        }
        catch (SchedulerException e) {
            log.error("There was a problem unpausing the cron scheduler", e);
            throw e;
        }
    }

    private void purgeDeprecatedJobs() throws SchedulerException {
        Set<JobKey> jobKeys = getJobKeys(JobType.CRON.getGroupName());
        /*
        * purge jobs that have been deleted from this version of Candlepin.
        * This is necessary as we might not even have the Class definition
        * at classpath, Hence any attempt at fetching the JobDetail by the
        * Scheduler or JobStatus by the JobCurator will fail.
        */
        for (JobKey jobKey : jobKeys) {
            for (String deletedJob : DELETED_JOBS) {
                if (jobKey.getName().contains(deletedJob)) {
                    scheduler.deleteJob(jobKey);
                    jobCurator.deleteJobNoStatusReturn(jobKey.getName());
                    break;
                }
            }
        }
    }
}