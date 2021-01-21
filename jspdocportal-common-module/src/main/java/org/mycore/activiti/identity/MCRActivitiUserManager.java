package org.mycore.activiti.identity;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.UserQueryImpl;
import org.activiti.engine.impl.persistence.entity.UserEntity;
import org.activiti.engine.impl.persistence.entity.UserEntityManager;
import org.apache.commons.lang.StringUtils;
import org.mycore.frontend.jsp.MCRHibernateTransactionWrapper;
import org.mycore.user2.MCRRole;
import org.mycore.user2.MCRRoleManager;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserManager;

/**
 * MCRAcitivitiUserManager
 * 
 * to match Activiti User Management and MyCoRe User Management
 * 
 * implementation based upon 
 * http://developer4life.blogspot.de/2012/02/activiti-authentication-and-identity.html
 * 
 * @author Robert Stephan
 * 
 */
public class MCRActivitiUserManager extends UserEntityManager {

    public MCRActivitiUserManager() {
    }

    @Override
    public User createNewUser(String userId) {
        throw new ActivitiException("The MycoRe Activiti user manager doesn't support creating a new user");
    }

    @Override
    public void insertUser(User user) {
        throw new ActivitiException("The MycoRe Activiti user manager doesn't support inserting a new user");
    }

    @Override
    public void updateUser(User updatedUser) {
        throw new ActivitiException("The MycoRe Activiti user manager doesn't support updating a user");
    }

    @Override
    public void deleteUser(String userId) {
        throw new ActivitiException("The MycoRe Activiti user manager doesn't support deleting a user");
    }

    /**
     * 
     * needs to be implemented
     */
    @Override
    public UserEntity findUserById(String userLogin) {
        try (MCRHibernateTransactionWrapper mtw = new MCRHibernateTransactionWrapper()) {
            MCRUser mcrUser = MCRUserManager.getUser(userLogin);
            String x = null;
            if (mcrUser != null) {
                UserEntity u = new UserEntity(userLogin);
                u.setEmail(mcrUser.getEMailAddress());
                u.setPassword(mcrUser.getPassword());
                x = mcrUser.getUserAttribute("lastname");
                if (x != null) {
                    u.setLastName(x);
                }
                x = mcrUser.getUserAttribute("nachname");
                if (x != null) {
                    u.setLastName(x);
                }
                x = mcrUser.getUserAttribute("firstname");
                if (x != null) {
                    u.setFirstName(x);
                }
                x = mcrUser.getUserAttribute("vorname");
                if (x != null) {
                    u.setFirstName(x);
                }
                return u;
            }
        }
        return null;
    }

    @Override
    public List<User> findUserByQueryCriteria(UserQueryImpl query, Page page) {

        List<User> userList = new ArrayList<User>();
        try (MCRHibernateTransactionWrapper mtw = new MCRHibernateTransactionWrapper()) {
            UserQueryImpl userQuery = (UserQueryImpl) query;

            if (StringUtils.isNotEmpty(userQuery.getId())) {

                userList.add(findUserById(userQuery.getId()));

                return userList;

            } else if (StringUtils.isNotEmpty(userQuery.getLastName())) {

                throw new ActivitiException(
                        "The MycoRe Activiti user manager doesn't support querying by user name - use user id instead");
            } else {
                //list all Users
                TreeSet<String> userIDs = new TreeSet<String>();
                for (MCRRole mcrRole : MCRRoleManager.listSystemRoles()) {
                    for (String s : MCRRoleManager.listUserIDs(mcrRole)) {
                        userIDs.add(s);
                    }
                }

                for (String userID : userIDs) {
                    UserEntity u = findUserById(userID);
                    if (u != null) {
                        userList.add(u);
                    }
                }
                return userList;

            }
        }
        // you can add other search criteria that will allow extended
        // support using the Activiti engine API
    }

    @Override
    public long findUserCountByQueryCriteria(UserQueryImpl query) {
        return findUserByQueryCriteria(query, null).size();
    }

    @Override
    public Boolean checkPassword(String userId, String password) {
        MCRUser mcrUser = MCRUserManager.checkPassword(userId, password);
        return mcrUser != null;
    }
}
