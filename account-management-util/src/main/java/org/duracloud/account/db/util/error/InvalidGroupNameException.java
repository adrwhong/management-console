/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.util.error;

import org.duracloud.common.error.DuraCloudCheckedException;

/**
 * @author dbernstein
 */
public class InvalidGroupNameException extends DuraCloudCheckedException {

    public InvalidGroupNameException(String name) {
        super("Invalid group name: " + name);
    }

}
