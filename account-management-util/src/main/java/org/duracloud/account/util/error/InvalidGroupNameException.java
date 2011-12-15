package org.duracloud.account.util.error;

import org.duracloud.common.error.DuraCloudCheckedException;

/**
 * 
 * @author dbernstein
 *
 */
public class InvalidGroupNameException extends DuraCloudCheckedException {

    public InvalidGroupNameException(String name) {
        super("Invalid group name: " + name);
    }

}
