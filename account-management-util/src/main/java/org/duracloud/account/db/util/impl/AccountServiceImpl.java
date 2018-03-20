/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.util.impl;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.duracloud.account.config.AmaEndpoint;
import org.duracloud.account.db.model.AccountInfo;
import org.duracloud.account.db.model.AccountRights;
import org.duracloud.account.db.model.DuracloudUser;
import org.duracloud.account.db.model.StorageProviderAccount;
import org.duracloud.account.db.model.UserInvitation;
import org.duracloud.account.db.repo.DuracloudRepoMgr;
import org.duracloud.account.db.repo.DuracloudRightsRepo;
import org.duracloud.account.db.util.AccountService;
import org.duracloud.account.db.util.error.DuracloudProviderAccountNotAvailableException;
import org.duracloud.account.db.util.error.UnsentEmailException;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.notification.Emailer;
import org.duracloud.storage.domain.StorageProviderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author "Daniel Bernstein (dbernstein@duraspace.org)"
 */
public class AccountServiceImpl implements AccountService {
    private Logger log = LoggerFactory.getLogger(AccountServiceImpl.class);
    // The AccountInfo member is a read-cache. All 'getter' come from it, and
    // writes go to both it and the persistence layer.
    private AccountInfo account;
    private DuracloudRepoMgr repoMgr;
    private AmaEndpoint amaEndpoint;

    /**
     * @param acct
     */
    public AccountServiceImpl(AmaEndpoint amaEndpoint,
                              AccountInfo acct,
                              DuracloudRepoMgr repoMgr) {
        this.amaEndpoint = amaEndpoint;
        this.account = acct;
        this.repoMgr = repoMgr;
    }

    @Override
    public Set<DuracloudUser> getUsers() {
        DuracloudRightsRepo rightsRepo = repoMgr.getRightsRepo();

        Set<DuracloudUser> users = new HashSet<DuracloudUser>();
        List<AccountRights> rights =
            rightsRepo.findByAccountId(account.getId());

        for (AccountRights right : rights) {
            DuracloudUser user = right.getUser();
            user.getAccountRights().size();  // lazy load the user's accout rights
            users.add(user);
        }

        return users;
    }

    @Override
    public Long getAccountId() {
        return account.getId();
    }

    @Override
    public AccountInfo retrieveAccountInfo() {
        return account;
    }

    @Override
    public StorageProviderAccount getPrimaryStorageProvider() {
        return retrieveAccountInfo().getPrimaryStorageProviderAccount();
    }

    @Override
    public Set<StorageProviderAccount> getSecondaryStorageProviders() {
        return retrieveAccountInfo().getSecondaryStorageProviderAccounts();
    }

    @Override
    public void addStorageProvider(StorageProviderType storageProviderType) {
        log.info("Adding storage provider of type {} to account {}",
                 storageProviderType, account.getSubdomain());

        StorageProviderAccount storageProviderAccount = new StorageProviderAccount();
        storageProviderAccount.setProviderType(storageProviderType);

        AccountInfo account = retrieveAccountInfo();
        account.getSecondaryStorageProviderAccounts().add(storageProviderAccount);
        saveAccountInfo(account);
    }

    @Override
    public void removeStorageProvider(Long storageProviderId) {
        log.info("Removing storage provider with ID {} from account {}",
                 storageProviderId, account.getSubdomain());

        StorageProviderAccount storageProviderAccount =
            repoMgr.getStorageProviderAccountRepo().findOne(storageProviderId);
        AccountInfo accountInfo = retrieveAccountInfo();
        if (accountInfo.getSecondaryStorageProviderAccounts()
                       .remove(storageProviderAccount)) {
            saveAccountInfo(accountInfo);
            repoMgr.getStorageProviderAccountRepo().delete(storageProviderId);
        } else {
            throw new DuracloudProviderAccountNotAvailableException(
                "The storage provider account with ID " + storageProviderId +
                " is not associated with account with id " + account.getId() +
                " as a secondary storage provider.");
        }
    }

    private void saveAccountInfo(AccountInfo accountInfo) {
        this.repoMgr.getAccountRepo().save(accountInfo);
    }

    @Override
    public void changePrimaryStorageProvider(Long storageProviderId) {
        log.info("Changing primary storage provider to {} from account {}",
                 storageProviderId, account.getSubdomain());

        AccountInfo accountInfo = retrieveAccountInfo();
        Set<StorageProviderAccount> secondaryAccounts = accountInfo.getSecondaryStorageProviderAccounts();
        for (StorageProviderAccount secondary : secondaryAccounts) {
            if (secondary.getId().equals(storageProviderId)) {
                secondaryAccounts.remove(secondary);
                secondaryAccounts.add(accountInfo.getPrimaryStorageProviderAccount());
                accountInfo.setPrimaryStorageProviderAccount(secondary);
                accountInfo.setSecondaryStorageProviderAccounts(secondaryAccounts);
                saveAccountInfo(accountInfo);
                return;
            }
        }

        throw new DuracloudProviderAccountNotAvailableException(
            "The storage provider account with ID " + storageProviderId +
            " is not associated with account with id " + account.getId() +
            " as a secondary storage provider.");
    }

    @Override
    public void storeAccountInfo(String acctName,
                                 String orgName,
                                 String department) {
        String[] logInfo =
            {account.getSubdomain(), acctName, orgName, department};
        log.info("Updating info for account {}. Account Name: {}, " +
                 "Org Name: {}, Department: {}", logInfo);

        account.setAcctName(acctName);
        account.setOrgName(orgName);
        account.setDepartment(department);
        repoMgr.getAccountRepo().save(account);
    }

    @Override
    public void storeAccountStatus(AccountInfo.AccountStatus status) {
        log.info("Updating account status to {} for account {}",
                 status.name(), account.getSubdomain());

        account.setStatus(status);
        repoMgr.getAccountRepo().save(account);
    }

    @Override
    public String getSubdomain() {
        return account.getSubdomain();
    }

    @Override
    public UserInvitation inviteUser(String emailAddress,
                                     String adminUsername,
                                     Emailer emailer) {
        log.info("Inviting user at address {} to account {}",
                 emailAddress, account.getSubdomain());

        ChecksumUtil cksumUtil = new ChecksumUtil(ChecksumUtil.Algorithm.MD5);

        String code = emailAddress + System.currentTimeMillis();
        String redemptionCode = cksumUtil.generateChecksum(code);

        int expirationDays = 14;
        UserInvitation userInvitation = new UserInvitation(null,
                                                           account,
                                                           account.getAcctName(),
                                                           account.getOrgName(),
                                                           account.getDepartment(),
                                                           account.getSubdomain(),
                                                           adminUsername,
                                                           emailAddress,
                                                           expirationDays,
                                                           redemptionCode);
        repoMgr.getUserInvitationRepo().save(userInvitation);
        sendEmail(userInvitation, emailer);

        return userInvitation;
    }

    private void sendEmail(UserInvitation invitation, Emailer emailer) {
        try {
            InvitationMessageFormatter formatter = new InvitationMessageFormatter(invitation, amaEndpoint);
            emailer.send(formatter.getSubject(),
                         formatter.getBody(),
                         invitation.getUserEmail());

        } catch (Exception e) {
            String msg =
                "Error: Unable to send email to: " + invitation.getUserEmail();

            log.error(msg, e);
            throw new UnsentEmailException(msg, e);
        }
    }

    @Override
    public Set<UserInvitation> getPendingInvitations() {
        List<UserInvitation> invitations =
            repoMgr.getUserInvitationRepo().findByAccountId(account.getId());

        Date now = new Date();
        Set<UserInvitation> pendingInvitations = new HashSet<UserInvitation>();

        for (UserInvitation ui : invitations) {
            if (ui.getExpirationDate().before(now)) {
                repoMgr.getUserInvitationRepo().delete(ui.getId());
            } else {
                pendingInvitations.add(ui);
            }
        }

        return pendingInvitations;
    }

    @Override
    public void deleteUserInvitation(Long invitationId) {
        log.info("Deleting user invitation with id {} from account {}",
                 invitationId, account.getSubdomain());

        repoMgr.getUserInvitationRepo().delete(invitationId);
    }

}