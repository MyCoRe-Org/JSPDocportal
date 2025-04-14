package org.mycore.jspdocportal.common.bpmn.identity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.GroupQuery;
import org.camunda.bpm.engine.identity.NativeUserQuery;
import org.camunda.bpm.engine.identity.Tenant;
import org.camunda.bpm.engine.identity.TenantQuery;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.identity.UserQuery;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.identity.ReadOnlyIdentityProvider;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.mycore.jspdocportal.common.bpmn.identity.model.MCRMyCoReIDMGroup;
import org.mycore.jspdocportal.common.bpmn.identity.model.MCRMyCoReIDMUser;
import org.mycore.jspdocportal.common.bpmn.identity.query.MCRMyCoReIDMGroupQuery;
import org.mycore.jspdocportal.common.bpmn.identity.query.MCRMyCoReIDMTenantQuery;
import org.mycore.jspdocportal.common.bpmn.identity.query.MCRMyCoReIDMUserQuery;
import org.mycore.user2.MCRRole;
import org.mycore.user2.MCRRoleManager;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserManager;

public class MCRMyCoreIDMProvider implements ReadOnlyIdentityProvider {

    // User ////////////////////////////////////////////

    @Override
    public User findUserById(String userId) {
        MCRUser mcrUser = MCRUserManager.getUser(userId);
        if (mcrUser != null) {
            return new MCRMyCoReIDMUser(mcrUser);
        }
        return null;
    }

    @Override
    public UserQuery createUserQuery() {
        return new MCRMyCoReIDMUserQuery(Context.getProcessEngineConfiguration().getCommandExecutorTxRequired());
    }

    @Override
    public UserQuery createUserQuery(CommandContext commandContext) {
        return new MCRMyCoReIDMUserQuery();
    }

    @Override
    public NativeUserQuery createNativeUserQuery() {
        throw new BadUserRequestException("not supported");
    }

    public long findUserCountByQueryCriteria(MCRMyCoReIDMUserQuery query) {
        return findUserByQueryCriteria(query).size();
    }

    public List<User> findUserByQueryCriteria(MCRMyCoReIDMUserQuery query) {

        if (query.getId() != null) {
            MCRUser mcrUser = MCRUserManager.getUser(query.getId());
            if (mcrUser != null) {
                return Arrays.asList(new MCRMyCoReIDMUser(mcrUser));
            }
        }

        //implement if necessary
        //if(query.getFirstName() != null)
        //if(query.getLastName() != null)
        // if(query.getEmail() != null)

        if (query.getGroupId() != null) {
            List<User> users = new ArrayList<>();

            List<MCRUser> mcrUsers = MCRUserManager.listUsers("*", null, null, null);
            mcrUsers.removeIf(x -> !x.isUserInRole(query.getGroupId()));
            for (MCRUser u : mcrUsers) {
                users.add(new MCRMyCoReIDMUser(u));
            }
            return users;
        }

        return Collections.emptyList();
    }

    @Override
    public boolean checkPassword(String userId, String password) {
        if (userId == null || password == null || userId.isEmpty() || password.isEmpty()) {
            return false;
        }
        User user = findUserById(userId);
        if (user == null) {
            return false;
        }
        return user.getPassword().equals(password);
    }

    // Group //////////////////////////////////////////

    @Override
    public Group findGroupById(String groupId) {
        if (groupId != null) {
            MCRRole mcrRole = MCRRoleManager.getRole(groupId);
            return new MCRMyCoReIDMGroup(mcrRole);
        }
        return null;
    }

    @Override
    public GroupQuery createGroupQuery() {
        return new MCRMyCoReIDMGroupQuery(Context.getProcessEngineConfiguration().getCommandExecutorTxRequired());
    }

    @Override
    public GroupQuery createGroupQuery(CommandContext commandContext) {
        return new MCRMyCoReIDMGroupQuery();
    }

    public long findGroupCountByQueryCriteria(MCRMyCoReIDMGroupQuery query) {
        return findGroupByQueryCriteria(query).size();
    }

    public List<Group> findGroupByQueryCriteria(MCRMyCoReIDMGroupQuery query) {

        if (query.getId() != null) {
            MCRRole mcrRole = MCRRoleManager.getRole(query.getId());
            return Arrays.asList(new MCRMyCoReIDMGroup(mcrRole));
        }
        if (query.getUserId() != null) {
            List<Group> groups = new ArrayList<>();
            for (String roleID : MCRUserManager.getUser(query.getUserId()).getSystemRoleIDs()) {
                groups.add(new MCRMyCoReIDMGroup(MCRRoleManager.getRole(roleID)));
            }
            return groups;
        }

        return Collections.emptyList();
    }

    // Tenants - not supported

    @Override
    public Tenant findTenantById(String tenantId) {
        return null;
    }

    @Override
    public TenantQuery createTenantQuery() {
        return new MCRMyCoReIDMTenantQuery(Context.getProcessEngineConfiguration().getCommandExecutorTxRequired());
    }

    @Override
    public TenantQuery createTenantQuery(CommandContext commandContext) {
        return new MCRMyCoReIDMTenantQuery();
    }

    @Override
    public void flush() {

    }

    @Override
    public void close() {

    }
}
