/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.app.controller;

import org.hibernate.validator.constraints.NotBlank;

/**
 * @author Daniel Bernstein
 */
public class AccountEditForm {
    @NotBlank(message = "You must specify an organization.")
    private String orgName;

    private String department;

    @NotBlank(message = "You must specify an account name.")
    private String acctName;

    public String getAcctName() {
        return acctName;
    }

    public void setAcctName(String acctName) {
        this.acctName = acctName;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

}
