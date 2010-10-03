package org.duracloud.account.util;

import java.util.List;

import org.duracloud.common.model.StorageProvider;
import org.duracloud.security.DuracloudUserDetailsService;

/**
 * An interface for manipulating account data.
 * 
 * @author "Daniel Bernstein (dbernstein@duracloud.org)"
 * 
 */
public interface AccountService {
	/**
	 * 
	 * @return
	 */
	public AccountDetail retrieveAccountDetail();
	/**
	 * 
	 * @param fullName
	 * @param email
	 * @param orgName
	 * @param department
	 */
	public void storeAccountInfo(String fullName, String email, String orgName,
			String department);

	/**
	 * 
	 * @return
	 */
	public PaymentInfo retrievePaymentInfo();
	
	/**
	 * 
	 * @param paymentInfo
	 */
	public void storePaymentInfo(PaymentInfo paymentInfo);
	
	/**
	 * 
	 * @param username
	 */
	public void sendPasswordReminder(String username);

	/**
	 * 
	 * @param oldPassword
	 * @param newPassword
	 */
	public void changePassword(String oldPassword, String newPassword);
	
	/**
	 * 
	 * @param subdomain
	 */
	public void storeSubdomain(String subdomain);

	/**
	 * 
	 * @return
	 */
	public String retrieveSubdomain();

	/**
	 * 
	 * @return
	 */
	public List<StorageProvider> retrieveStorageProviders();
	
	/**
	 * 
	 * @param storageProvider
	 */
	public void addStorageProvider(StorageProvider storageProvider);
	
	/**
	 * 
	 * @param storageProvider
	 */
	public void removeStorageProvider(StorageProvider storageProvider);
	
	/**
	 * 
	 * @param user
	 * @throws UsernameAlreadyExistsException
	 */
	public void addUser(DuracloudUser user) throws UsernameAlreadyExistsException;
	
	/**
	 * 
	 * @param user
	 */
	public void removeUser(String username);
	
	/**
	 * 
	 */
	public void revokeAdminPrivileges();
	
	/**
	 * 
	 */
	public void grantAdminPrivileges();
	
	/**
	 * 
	 * @return empty list
	 */
	public List<DuracloudUser> listUsers();
}
