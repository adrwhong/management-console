/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.app.controller;

import java.util.HashSet;
import java.util.Set;

import org.duracloud.account.db.model.DuracloudUser;
import org.duracloud.account.db.model.Role;
import org.duracloud.account.db.util.RootAccountManagerService;
import org.duracloud.account.db.util.error.AccountNotFoundException;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Daniel Bernstein
 * Date: Feb 29, 2012
 */
public class UsersControllerTest extends AmaControllerTestBase {
    private UsersController usersController;
    private RootAccountManagerService rootAccountManagerService;

    @Before
    public void before() throws Exception {
        super.before();

        setupGenericAccountAndUserServiceMocks(TEST_ACCOUNT_ID);
        rootAccountManagerService =
            createMock(RootAccountManagerService.class);
        usersController = new UsersController();
        usersController.setRootAccountManagerService(rootAccountManagerService);
        usersController.setAccountManagerService(this.accountManagerService);
        usersController.setUserService(this.userService);
    }

    /**
     * Test method for org.duracloud.account.app.controller.usersController
     *
     * @throws AccountNotFoundException
     */
    @Test
    public void testGet() throws AccountNotFoundException {
        EasyMock.expect(rootAccountManagerService.listAllUsers(null)).andReturn(new HashSet<DuracloudUser>());
        replayMocks();
        usersController.get();
    }

    @Test
    public void testReset() throws Exception {
        this.rootAccountManagerService.resetUsersPassword(0L);
        EasyMock.expectLastCall();
        addFlashAttribute();

        replayMocks();
        usersController.resetUsersPassword(0L, redirectAttributes);
    }

    @Test
    public void testDelete() throws Exception {
        this.rootAccountManagerService.deleteUser(0L);
        EasyMock.expectLastCall();
        addFlashAttribute();
        replayMocks();
        usersController.deleteUser(0L, redirectAttributes);
    }

    @Test
    public void testRevoke() throws Exception {
        Long userId = TEST_USER_ID;
        Long accountId = TEST_ACCOUNT_ID;
        this.userService.revokeUserRights(accountId, userId);
        EasyMock.expectLastCall();
        addFlashAttribute();
        replayMocks();
        usersController.revokeUserRightsFromAccount(userId, accountId, redirectAttributes);
    }

    @Test
    public void testChange() throws Exception {
        Long userId = TEST_USER_ID;
        Long accountId = TEST_ACCOUNT_ID;
        Role role = Role.ROLE_ADMIN;
        Set<Role> roles = role.getRoleHierarchy();
        EasyMock.expect(this.userService.setUserRights(accountId, userId, roles.toArray(new Role[0]))).andReturn(true);
        AccountUserEditForm f = new AccountUserEditForm();
        f.setAccountId(accountId);
        f.setRole(role.name());
        EasyMock.expectLastCall();
        addFlashAttribute();
        setupNoBindingResultErrors();
        replayMocks();
        usersController.changeUserRole(userId, f, result, redirectAttributes);
    }

}
