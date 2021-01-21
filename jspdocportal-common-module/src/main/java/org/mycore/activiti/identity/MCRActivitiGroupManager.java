package org.mycore.activiti.identity;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.identity.Group;
import org.activiti.engine.impl.GroupQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.persistence.entity.GroupEntity;
import org.activiti.engine.impl.persistence.entity.GroupEntityManager;
import org.apache.commons.lang.StringUtils;
import org.mycore.frontend.jsp.MCRHibernateTransactionWrapper;
import org.mycore.user2.MCRRole;
import org.mycore.user2.MCRRoleManager;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserManager;

/**
 * MCRActivitiGroupManager
 * 
 * to match Activiti User Management and MyCoRe User Management
 * 
 * implementation based upon
 * http://developer4life.blogspot.de/2012/02/activiti-authentication
 * -and-identity.html
 * 
 * @author Robert Stephan
 * 
 */
public class MCRActivitiGroupManager extends GroupEntityManager {

    public MCRActivitiGroupManager() {

    }

    @Override
    public Group createNewGroup(String groupId) {
        throw new ActivitiException("The MycoRe Activiti group manager doesn't support creating a new group");
    }

    @Override
    public void insertGroup(Group group) {
        throw new ActivitiException("The MycoRe Activiti group manager doesn't support inserting a new group");
    }

    @Override
    public void updateGroup(Group updatedGroup) {
        throw new ActivitiException("The MycoRe Activiti group manager doesn't support updating a new group");
    }

    @Override
    public void deleteGroup(String groupId) {
        throw new ActivitiException("The MycoRe Activiti group manager doesn't support deleting a new group");
    }

    @Override
    public long findGroupCountByQueryCriteria(GroupQueryImpl query) {
        return findGroupByQueryCriteria(query, null).size();
    }

    @Override
    public List<Group> findGroupByQueryCriteria(GroupQueryImpl query, Page page) {
        List<Group> groupList = new ArrayList<Group>();
        try (MCRHibernateTransactionWrapper mtw = new MCRHibernateTransactionWrapper()) {
            GroupQueryImpl groupQuery = (GroupQueryImpl) query;
            if (StringUtils.isNotEmpty(groupQuery.getId())) {
                GroupEntity singleGroup = findGroupById(groupQuery.getId());
                groupList.add(singleGroup);
                return groupList;
            } else if (StringUtils.isNotEmpty(groupQuery.getName())) {
                throw new ActivitiException(
                        "The MycoRe Activiti group manager doesn't support querying by group name - use group id instead");
            } else if (StringUtils.isNotEmpty(groupQuery.getUserId())) {
                return findGroupsByUser(groupQuery.getUserId());
            } else {
                // return all groups
                for (MCRRole mcrRole : MCRRoleManager.listSystemRoles()) {
                    Group g = new GroupEntity(mcrRole.getName());
                    if (mcrRole.getLabel() != null) {
                        g.setName(mcrRole.getLabel().getText());
                    }
                    g.setType("fromMyCoRe");
                }
                return groupList;
            }
            // you can add other search criteria that will allow extended
            // support using the Activiti engine API
        }
    }

    public GroupEntity findGroupById(String activitiGroupID) {
        try (MCRHibernateTransactionWrapper mtw = new MCRHibernateTransactionWrapper()) {
            MCRRole mcrRole = MCRRoleManager.getRole(activitiGroupID);

            // we do not support external MyCoRe Roles
            // if(mcrRole==null){
            // mcrRole = MCRRoleManager.getExternalRole(activitiGroupID);
            // }

            if (mcrRole != null) {
                GroupEntity g = new GroupEntity(activitiGroupID);
                g.setName(mcrRole.getLabel().getText());
                g.setType("fromMyCoRe");
                return g;
            }
        }
        return null;
    }

    /**
     * This method retrieves all groups for a given user
     * 
     * needs to be implemented
     * 
     */
    @Override
    public List<Group> findGroupsByUser(String userLogin) {
        List<Group> groupList = new ArrayList<Group>();
        try (MCRHibernateTransactionWrapper mtw = new MCRHibernateTransactionWrapper()) {
            MCRUser mcrUser = MCRUserManager.getUser(userLogin);
            if (mcrUser != null) {
                for (String roleID : mcrUser.getSystemRoleIDs()) {
                    Group g = new GroupEntity(roleID);
                    MCRRole mcrRole = MCRRoleManager.getRole(roleID);
                    if (mcrRole != null) {
                        g.setName(mcrRole.getLabel().getText());
                    }
                    g.setType("fromMyCoRe");
                    groupList.add(g);
                }
            }
        }
        return groupList;
    }

}
