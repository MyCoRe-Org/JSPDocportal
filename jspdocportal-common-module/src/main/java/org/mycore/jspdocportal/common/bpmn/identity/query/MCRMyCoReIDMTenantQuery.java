package org.mycore.jspdocportal.common.bpmn.identity.query;

import java.util.List;

import org.camunda.bpm.engine.identity.Tenant;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.TenantQueryImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;

/**
 * Tenants are not support, 
 * so this implementation only returns default values
 * 
 * @author Robert Stephan
 *
 */
public class MCRMyCoReIDMTenantQuery extends TenantQueryImpl {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    public MCRMyCoReIDMTenantQuery() {
        super();
    }

    public MCRMyCoReIDMTenantQuery(CommandExecutor commandExecutor) {
        super(commandExecutor);
    }

    @Override
    public long executeCount(CommandContext commandContext) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public List<Tenant> executeList(CommandContext commandContext, Page page) {
        // TODO Auto-generated method stub
        return null;
    }

}
