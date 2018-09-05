/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.app.controller;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.duracloud.account.app.controller.GroupsForm.Action;
import org.duracloud.account.db.model.DuracloudGroup;
import org.duracloud.account.db.model.DuracloudUser;
import org.duracloud.account.db.util.DuracloudGroupService;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.validation.BindingResult;

/**
 * @author Daniel Bernstein
 * Date: Nov 12, 2011
 */
public class AccountGroupsControllerTest extends AmaControllerTestBase {

    private static final String TEST_GROUP_NAME = "test";
    private AccountGroupsController accountGroupsController;
    private DuracloudGroupService groupService;
    private Long accountId = AmaControllerTestBase.TEST_ACCOUNT_ID;

    @Before
    public void before() throws Exception {
        super.before();

        groupService =
            createMock(DuracloudGroupService.class);

        accountGroupsController = new AccountGroupsController();
        accountGroupsController.setAccountManagerService(accountManagerService);
        accountGroupsController.setUserService(userService);
        accountGroupsController.setDuracloudGroupService(groupService);
        result = null;
        setupGenericAccountAndUserServiceMocks(accountId);

    }

    private Set<DuracloudGroup> createGroups() {
        Long groupId = 2L;
        String groupName = DuracloudGroup.PREFIX + TEST_GROUP_NAME;
        DuracloudGroup group = createGroup(groupId, groupName, accountId);
        group.setUsers(new HashSet<DuracloudUser>());
        group.getUsers().add(createUser());
        Set<DuracloudGroup> set = new HashSet<DuracloudGroup>();
        set.add(group);
        return set;
    }

    @Test
    public void testGetGroups() throws Exception {
        expectGroupGroups(1);
        replayMocks();

        String view = this.accountGroupsController.getGroups(accountId, model);
        Assert.assertEquals(AccountGroupsController.GROUPS_VIEW_ID, view);
    }

    private void expectGroupGroups(int times) {
        EasyMock.expect(groupService.getGroups(accountId))
                .andReturn(createGroups())
                .times(times);
    }

    @Test
    public void testGetGroupsAddGroup() throws Exception {
        Long groupId = 3L;
        String groupName = DuracloudGroup.PREFIX + "group2";

        DuracloudGroup group = createGroup(groupId, groupName, accountId);
        EasyMock.expect(groupService.createGroup(
            DuracloudGroup.PREFIX + groupName, accountId)).andReturn(group);

        result = EasyMock.createMock("BindingResult", BindingResult.class);
        EasyMock.expect(result.hasFieldErrors()).andReturn(false);

        replayMocks();
        GroupsForm form = new GroupsForm();
        form.setAction(Action.ADD);
        form.setGroupName(groupName);

        String view = this.accountGroupsController.modifyGroups(accountId,
                                                                model,
                                                                form,
                                                                result);
        Assert.assertTrue(view.contains(groupName));
    }

    @Test
    public void testGetGroupsRemoveGroups() throws Exception {
        expectGroupGroups(1);
        DuracloudGroup group = createGroups().iterator().next();
        groupService.deleteGroup(group, accountId);
        EasyMock.expectLastCall().once();
        EasyMock.expect(groupService.getGroup(group.getName(), accountId))
                .andReturn(group)
                .once();
        replayMocks();
        GroupsForm form = new GroupsForm();
        form.setAction(Action.REMOVE);
        form.setGroupNames(new String[] {DuracloudGroup.PREFIX + TEST_GROUP_NAME});

        String view = this.accountGroupsController.modifyGroups(accountId,
                                                                model,
                                                                form,
                                                                result);
        Assert.assertEquals(AccountGroupsController.GROUPS_VIEW_ID, view);
    }

    private Object getModelAttribute(String name) {
        return model.asMap().get(name);
    }

    @Test
    public void testGetGroup() throws Exception {
        expectGroupGroups(1);
        replayMocks();
        String view =
            this.accountGroupsController.getGroup(accountId,
                                                  DuracloudGroup.PREFIX + TEST_GROUP_NAME,
                                                  model);
        Assert.assertEquals(AccountGroupsController.GROUP_VIEW_ID, view);
        Assert.assertNotNull(getModelAttribute(AccountGroupsController.GROUP_KEY));
        Assert.assertNotNull(getModelAttribute(AccountGroupsController.GROUP_USERS_KEY));
    }

    @Test
    public void testGetGroupEdit() throws Exception {
        expectGroupGroups(1);

        HttpServletRequest request =
            EasyMock.createMock(HttpServletRequest.class);
        HttpSession session = EasyMock.createMock(HttpSession.class);
        EasyMock.expect(request.getSession()).andReturn(session).once();

        session.removeAttribute(AccountGroupsController.GROUP_USERS_KEY);
        EasyMock.expectLastCall().once();

        EasyMock.expect(session.getAttribute(AccountGroupsController.GROUP_USERS_KEY))
                .andReturn(null)
                .once();

        session.setAttribute(EasyMock.anyObject(String.class),
                             (Collection<DuracloudUser>) EasyMock.anyObject());
        EasyMock.expectLastCall().once();

        EasyMock.replay(request, session);

        replayMocks();
        String view =
            this.accountGroupsController.editGroup(accountId,
                                                   DuracloudGroup.PREFIX + TEST_GROUP_NAME,
                                                   request,
                                                   model);
        Assert.assertEquals(AccountGroupsController.GROUP_EDIT_VIEW_ID, view);
        Assert.assertNotNull(getModelAttribute(AccountGroupsController.GROUP_KEY));
        Assert.assertNotNull(getModelAttribute(AccountGroupsController.GROUP_USERS_KEY));
        Assert.assertNotNull(getModelAttribute(AccountGroupsController.AVAILABLE_USERS_KEY));

    }

    @Test
    public void testSaveGroup() throws Exception {
        expectGroupGroups(1);

        HttpServletRequest request =
            EasyMock.createMock(HttpServletRequest.class);
        HttpSession session = EasyMock.createMock(HttpSession.class);
        EasyMock.expect(request.getSession()).andReturn(session).once();

        session.removeAttribute(AccountGroupsController.GROUP_USERS_KEY);
        EasyMock.expectLastCall().once();

        EasyMock.expect(session.getAttribute(AccountGroupsController.GROUP_USERS_KEY))
                .andReturn(null)
                .once();

        session.setAttribute(EasyMock.anyObject(String.class),
                             (Collection<DuracloudUser>) EasyMock.anyObject());
        EasyMock.expectLastCall().once();

        this.groupService
            .updateGroupUsers(EasyMock.anyObject(DuracloudGroup.class),
                              (Set<DuracloudUser>) EasyMock.anyObject(),
                              EasyMock.anyLong());
        EasyMock.expectLastCall().once();

        EasyMock.replay(request, session);

        replayMocks();

        GroupForm form = new GroupForm();
        form.setAction(GroupForm.Action.SAVE);

        String view =
            this.accountGroupsController.editGroup(accountId,
                                                   DuracloudGroup.PREFIX + TEST_GROUP_NAME,
                                                   form,
                                                   request,
                                                   model);
        Assert.assertTrue(view.contains(TEST_GROUP_NAME));
        Assert.assertFalse(view.endsWith("edit"));

        Assert.assertNotNull(getModelAttribute(AccountGroupsController.GROUP_KEY));
        Assert.assertNotNull(getModelAttribute(AccountGroupsController.GROUP_USERS_KEY));

    }

    @Test
    public void testAddRemoveUser() throws Exception {
        expectGroupGroups(2);
        HttpServletRequest request =
            EasyMock.createMock(HttpServletRequest.class);
        HttpSession session = EasyMock.createMock(HttpSession.class);
        EasyMock.expect(request.getSession()).andReturn(session).anyTimes();

        List<DuracloudUser> groupUsers =
            new LinkedList<DuracloudUser>(Arrays.asList(new DuracloudUser[] {createUser()}));

        EasyMock.expect(session.getAttribute(AccountGroupsController.GROUP_USERS_KEY))
                .andReturn(groupUsers)
                .once();

        EasyMock.expect(session.getAttribute(AccountGroupsController.GROUP_USERS_KEY))
                .andReturn(groupUsers)
                .once();

        session.setAttribute(EasyMock.anyObject(String.class),
                             (Collection<DuracloudUser>) EasyMock.anyObject());
        EasyMock.expectLastCall().once();

        EasyMock.replay(request, session);

        replayMocks();

        String testUsername = groupUsers.get(0).getUsername();

        GroupForm form = new GroupForm();
        form.setAction(GroupForm.Action.REMOVE);
        form.setGroupUsernames(new String[] {testUsername});
        String view =
            this.accountGroupsController.editGroup(accountId,
                                                   DuracloudGroup.PREFIX + TEST_GROUP_NAME,
                                                   form,
                                                   request,
                                                   model);

        Assert.assertEquals(AccountGroupsController.GROUP_EDIT_VIEW_ID, view);
        Assert.assertEquals(1,
                            getModelAttributeSize(AccountGroupsController.AVAILABLE_USERS_KEY));
        Assert.assertEquals(0,
                            getModelAttributeSize(AccountGroupsController.GROUP_USERS_KEY));

        form = new GroupForm();
        form.setAction(GroupForm.Action.ADD);
        form.setAvailableUsernames(new String[] {testUsername});

        this.accountGroupsController.editGroup(accountId,
                                               DuracloudGroup.PREFIX + TEST_GROUP_NAME,
                                               form,
                                               request,
                                               model);

        Assert.assertEquals(AccountGroupsController.GROUP_EDIT_VIEW_ID, view);
        Assert.assertEquals(0,
                            getModelAttributeSize(AccountGroupsController.AVAILABLE_USERS_KEY));
        Assert.assertEquals(1,
                            getModelAttributeSize(AccountGroupsController.GROUP_USERS_KEY));
    }

    @SuppressWarnings("unchecked")
    private int getModelAttributeSize(String name) {
        return ((Collection<? extends Object>) getModelAttribute(name)).size();
    }

    private DuracloudGroup createGroup(Long groupId, String groupName, Long accountId) {
        DuracloudGroup group = new DuracloudGroup();
        group.setId(groupId);
        group.setName(groupName);
        group.setAccount(createAccountInfo(accountId));
        return group;
    }
}
