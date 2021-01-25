package org.mycore.jspdocportal.common.bpmn.identity.query;

import java.util.List;

import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.impl.GroupQueryImpl;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.mycore.jspdocportal.common.bpmn.identity.MCRMyCoreIDMProvider;

public class MCRMyCoReIDMGroupQuery extends GroupQueryImpl{
    private static final long serialVersionUID = 1L;

    public MCRMyCoReIDMGroupQuery() {
        super();
    }

    public MCRMyCoReIDMGroupQuery(CommandExecutor commandExecutor) {
        super(commandExecutor);
    }

    @Override
    public long executeCount(CommandContext commandContext) {
        final MCRMyCoreIDMProvider provider = getCustomIdentityProvider(commandContext);
        return provider.findGroupCountByQueryCriteria(this);
    }

    @Override
    public List<Group> executeList(CommandContext commandContext, Page page) {
        final MCRMyCoreIDMProvider provider = getCustomIdentityProvider(commandContext);
        return provider.findGroupByQueryCriteria(this);
    }

    protected MCRMyCoreIDMProvider getCustomIdentityProvider(CommandContext commandContext) {
        return (MCRMyCoreIDMProvider) commandContext.getReadOnlyIdentityProvider();
    }
}
