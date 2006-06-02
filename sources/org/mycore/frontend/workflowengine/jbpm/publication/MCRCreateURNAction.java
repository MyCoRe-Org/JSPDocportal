package org.mycore.frontend.workflowengine.jbpm.publication;

import org.jbpm.context.exe.ContextInstance;
import org.jbpm.graph.exe.ExecutionContext;
import org.mycore.common.MCRException;
import org.mycore.frontend.workflowengine.jbpm.MCRAbstractAction;
import org.mycore.frontend.workflowengine.jbpm.MCRWorkflowConstants;
import org.mycore.frontend.workflowengine.jbpm.MCRWorkflowManager;
import org.mycore.frontend.workflowengine.jbpm.MCRWorkflowManagerFactory;
import org.mycore.frontend.workflowengine.strategies.MCRIdentifierStrategy;

public class MCRCreateURNAction extends MCRAbstractAction{
	private static final long serialVersionUID = 1L;

	private static MCRWorkflowManager WFM = MCRWorkflowManagerFactory.getImpl("publication");

	public void executeAction(ExecutionContext executionContext) throws MCRException {
		ContextInstance contextInstance;
		contextInstance = executionContext.getContextInstance();
		MCRIdentifierStrategy identifierStrategy = WFM.getIdentifierStrategy();
		String initiator = (String)contextInstance.getVariable(MCRWorkflowConstants.WFM_VAR_INITIATOR);
		String urn = (String)identifierStrategy.createNewIdentifier( initiator, WFM.getWorkflowProcessType());
		if(urn != null && !urn.equals("")){
			contextInstance.setVariable(MCRWorkflowConstants.WFM_VAR_RESERVATED_URN, urn);
			logger.error("create urn " + urn);
		}else{
			logger.error("could not create urn");
			throw new MCRException("could not create urn");
		}
	}

}
