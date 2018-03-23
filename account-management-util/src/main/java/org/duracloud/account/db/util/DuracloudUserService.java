/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.util;

import org.duracloud.account.db.model.DuracloudUser;
import org.duracloud.account.db.model.Role;
import org.duracloud.account.db.model.UserInvitation;
import org.duracloud.account.db.util.error.DBNotFoundException;
import org.duracloud.account.db.util.error.InvalidPasswordException;
import org.duracloud.account.db.util.error.InvalidRedemptionCodeException;
import org.duracloud.account.db.util.error.InvalidUsernameException;
import org.duracloud.account.db.util.error.UnsentEmailException;
import org.duracloud.account.db.util.error.UserAlreadyExistsException;
import org.springframework.security.access.annotation.Secured;

/**
 * This interface defines the contract for loading, storing, and managing
 * DuracloudUser entities.
 *
 * @author Andrew Woods Date: Oct 8, 2010
 */
public interface DuracloudUserService {

    /**
     * This method throws an exception is the username is non-unique or
     * it is not a valid username.
     *
     * @param username sought
     * @return true if not already used
     */
    @Secured({"role:ROLE_ANONYMOUS, scope:ANY"})
    public void checkUsername(String username)
        throws InvalidUsernameException, UserAlreadyExistsException;

    /**
     * This method creates and persists a new user.
     *
     * @param username         of new user
     * @param password         of new user
     * @param firstName        of new user
     * @param lastName         of new user
     * @param email            of new user
     * @param securityQuestion of new user
     * @param securityAnswer   of new user
     * @return DuracloudUser
     * @throws UserAlreadyExistsException
     * @throws InvalidUsernameException
     */
    @Secured({"role:ROLE_ANONYMOUS, scope:ANY"})
    public DuracloudUser createNewUser(String username,
                                       String password,
                                       String firstName,
                                       String lastName,
                                       String email,
                                       String securityQuestion,
                                       String securityAnswer)
        throws UserAlreadyExistsException,
        InvalidUsernameException;

    /**
     * This method sets the roles of a user in an account.
     * <p/>
     * Note that this method only sets a user to new roles. To remove a user
     * from an account, use revokeAllRights().
     *
     * @return true if an update was performed.
     */
    @Secured({"role:ROLE_ADMIN, scope:SELF_ACCT_PEER_UPDATE"})
    public boolean setUserRights(Long acctId, Long userId, Role... roles);

    /**
     * This method sets the roles of a user in an account.
     * <p/>
     * Note that this method only sets a user to new roles. To remove a user
     * from an account, use revokeAllRights().
     *
     * @return true if an update was performed.
     */
    @Secured({"role:ROLE_ADMIN, scope:SELF_ACCT"})
    public boolean addUserToAccount(Long acctId, Long userId)
        throws DBNotFoundException;

    /**
     * This method removes all rights to an account for a given user.
     *
     * @param acctId on which rights will be revoked
     * @param userId of user whose rights will be revoked
     */
    @Secured({"role:ROLE_ADMIN, scope:SELF_ACCT_PEER"})
    public void revokeUserRights(Long acctId, Long userId);

    /**
     * This method changes the password of the user with the arg userId from
     * the oldPassword to the newPassword.
     *
     * @param userId             of user who is seeking a password change
     * @param oldPassword
     * @param oldPasswordEncoded flag noting if the password is hashed
     * @param newPassword
     * @throws InvalidPasswordException
     */
    @Secured({"role:ROLE_USER, scope:SELF_ID"})
    public void changePassword(Long userId,
                               String oldPassword,
                               boolean oldPasswordEncoded,
                               String newPassword)
        throws DBNotFoundException, InvalidPasswordException;

    /**
     * @param userId
     * @param oldPassword
     * @param oldPasswordEncoded
     * @param newPassword
     * @throws InvalidPasswordException
     */
    @Secured({"role:ROLE_ANONYMOUS, scope:ANY"})
    public void changePasswordInternal(Long userId,
                                       String oldPassword,
                                       boolean oldPasswordEncoded,
                                       String newPassword)
        throws DBNotFoundException, InvalidPasswordException;

    /**
     * This method generates a random password, replaces the existing password
     * with the randomly generated one, and sends an email to the address on
     * file for the given username.
     *
     * @param username         of user who forgot their password
     * @param securityQuestion of user who forgot their password
     * @param securityAnswer   of user who forgot their password
     * @throws InvalidPasswordException
     * @throws UnsentEmailException
     */
    @Secured({"role:ROLE_ANONYMOUS, scope:ANY"})
    public void forgotPassword(String username,
                               String securityQuestion,
                               String securityAnswer)
        throws DBNotFoundException, InvalidPasswordException, UnsentEmailException;

    /**
     * Returns a user password change invitation
     *
     * @param redemptionCode
     * @return
     */
    @Secured({"role:ROLE_ANONYMOUS, scope:ANY"})
    UserInvitation retrievePassordChangeInvitation(String redemptionCode)
        throws DBNotFoundException;

    /**
     * This method loads a DuracloudUser from the persistence layer and
     * populate its rights info.
     *
     * @param username of user to load
     * @return DuracloudUser
     */
    @Secured({"role:ROLE_USER, scope:SELF_NAME"})
    public DuracloudUser loadDuracloudUserByUsername(String username)
        throws DBNotFoundException;

    @Secured({"role:ROLE_ANONYMOUS, scope:ANY"})
    public DuracloudUser loadDuracloudUserByUsernameInternal(String username)
        throws DBNotFoundException;

    @Secured({"role:ROLE_USER, scope:ANY"})
    public DuracloudUser loadDuracloudUserByIdInternal(Long userId)
        throws DBNotFoundException;

    /**
     * This method redeems an invitation to add this user to a DuraCloud
     * account.
     *
     * @param userId         the id of the user which will be added to the account
     *                       indicated in the invitation
     * @param redemptionCode code which was sent to the user as part of the invitation to
     *                       become part of an account
     * @return the account id associated with the newly redeemed invitation.
     */
    @Secured({"role:ROLE_USER, scope:SELF_ID"})
    public Long redeemAccountInvitation(Long userId, String redemptionCode)
        throws InvalidRedemptionCodeException;

    @Secured({"role:ROLE_ANONYMOUS, scope:ANY"})
    public void redeemPasswordChangeRequest(Long userId, String redemptionCode)
        throws InvalidRedemptionCodeException;

    /**
     * This method persists the arg user details.
     *
     * @param userId                  of user
     * @param firstName               of user
     * @param lastName                of user
     * @param email                   of user
     * @param securityQuestion        of user
     * @param securityAnswer          of user
     * @param allowableIPAddressRange
     */
    @Secured({"role:ROLE_USER, scope:SELF_ID"})
    public void storeUserDetails(Long userId,
                                 String firstName,
                                 String lastName,
                                 String email,
                                 String securityQuestion,
                                 String securityAnswer,
                                 String allowableIPAddressRange)
        throws DBNotFoundException;

}
