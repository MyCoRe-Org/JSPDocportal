package org.mycore.jspdocportal.common.bpmn.identity.query;

import java.util.List;

import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.UserQueryImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.mycore.jspdocportal.common.bpmn.identity.MCRMyCoreIDMProvider;

public class MCRMyCoReIDMUserQuery extends UserQueryImpl {

    private static final long serialVersionUID = 1L;

    public MCRMyCoReIDMUserQuery() {
        super();
    }

    public MCRMyCoReIDMUserQuery(CommandExecutor commandExecutor) {
        super(commandExecutor);
    }

    @Override
    public long executeCount(CommandContext commandContext) {
        final MCRMyCoreIDMProvider provider = getCustomIdentityProvider(commandContext);
        return provider.findUserCountByQueryCriteria(this);
    }

    @Override
    public List<User> executeList(CommandContext commandContext, Page page) {
        final MCRMyCoreIDMProvider provider = getCustomIdentityProvider(commandContext);
        return provider.findUserByQueryCriteria(this);
    }

    protected MCRMyCoreIDMProvider getCustomIdentityProvider(CommandContext commandContext) {
        return (MCRMyCoreIDMProvider) commandContext.getReadOnlyIdentityProvider();
    }
}
