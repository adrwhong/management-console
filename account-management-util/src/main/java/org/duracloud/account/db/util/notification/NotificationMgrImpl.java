/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.util.notification;

import org.duracloud.account.config.McConfig;
import org.duracloud.notification.Emailer;
import org.duracloud.notification.NotificationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andrew Woods
 * Date: 3/17/11
 */
public class NotificationMgrImpl implements NotificationMgr {

    private final Logger log = LoggerFactory.getLogger(NotificationMgrImpl.class);

    private NotificationFactory factory;
    private McConfig mcConfig;
    private NotificationMgrConfig mgrConfig;

    public NotificationMgrImpl(NotificationFactory factory,
                               McConfig mcConfig) {
        this.factory = factory;
        this.mcConfig = mcConfig;

        mgrConfig =
            new NotificationMgrConfig(mcConfig.getNotificationFromAddress(),
                                      mcConfig.getNotificationUser(),
                                      mcConfig.getNotificationPass(),
                                      mcConfig.getNotificationAdminAddress(),
                                      mcConfig.getNotificationType(),
                                      mcConfig.getNotificationHost(),
                                      mcConfig.getNotificationPort());

        if (mcConfig.getNotificationType().equals("smtp")) {
          factory.initialize(mcConfig.getNotificationUser(),
                           mcConfig.getNotificationPass(),
                           mcConfig.getNotificationHost(),
                           mcConfig.getNotificationPort());  
        } else {
          factory.initialize(mcConfig.getNotificationUser(),
                            mcConfig.getNotificationPass());
        {}
    }

    @Override
    public Emailer getEmailer() {
        return factory.getEmailer(mcConfig.getNotificationFromAddress());
    }

    @Override
    public NotificationMgrConfig getConfig() {
        return mgrConfig;
    }
}
