/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.security.domain;

import java.util.StringTokenizer;

import org.duracloud.account.db.model.Role;

/**
 * This class defines a bean used to constrain the access rules over secured
 * method calls.
 *
 * @author Andrew Woods
 * Date: 4/1/11
 */
public class SecuredRule {

    private Role role;
    private Scope scope;

    private static final String delim = ",";
    private static final String prefixRole = "role:";
    private static final String prefixScope = "scope:";

    public SecuredRule(String rule) {
        if (null == rule) {
            doThrow("arg Rule may not be null.");
        }

        StringTokenizer tokenizer = new StringTokenizer(rule, delim);
        if (tokenizer.countTokens() != 2) {
            doThrow("There must be two tokens. " + rule);
        }

        String fullRole = tokenizer.nextToken().trim();
        String fullScope = tokenizer.nextToken().trim();

        if (!fullRole.startsWith(prefixRole)) {
            doThrow("Role must begin with " + prefixRole + ", " + rule);
        }

        if (!fullScope.startsWith(prefixScope)) {
            doThrow("Scope must begin with " + prefixScope + ", " + rule);
        }

        this.role = Role.valueOf(fullRole.substring(prefixRole.length()));
        this.scope = Scope.valueOf(fullScope.substring(prefixScope.length()));
    }

    public Role getRole() {
        return role;
    }

    public Scope getScope() {
        return scope;
    }

    private void doThrow(String msg) {
        throw new IllegalArgumentException("Invalid rule: " + msg);
    }

    public static enum Scope {
        // applies to any user or account
        ANY,
        // applies to calling user's own profile (by userId)
        SELF_ID,
        // applies to calling user's own profile (by username)
        SELF_NAME,
        // applies to accounts with which calling user is a member
        SELF_ACCT,
        // applies to accounts with which calling user is a member AND
        //  the peer involved has rights on the account that are less than
        //  or equal to the caller's rights.
        SELF_ACCT_PEER,
        // applies to accounts with which calling user is a member AND
        //  the peer involved has current and proposed rights on the account
        //  that are less than or equal to the caller's rights.
        SELF_ACCT_PEER_UPDATE;
    }

}
