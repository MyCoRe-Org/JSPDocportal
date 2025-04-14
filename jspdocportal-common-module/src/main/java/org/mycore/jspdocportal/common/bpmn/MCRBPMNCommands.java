/*
 * $RCSfile$
 * $Revision$ $Date$
 *
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
 *
 * This program is free software; you can use it, redistribute it
 * and / or modify it under the terms of the GNU General Public License
 * (GPL) as published by the Free Software Foundation; either version 2
 * of the License or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program, in a file called gpl.txt or license.txt.
 * If not, write to the Free Software Foundation Inc.,
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307 USA
 */

package org.mycore.jspdocportal.common.bpmn;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.mycore.common.MCRException;
import org.mycore.frontend.cli.MCRAbstractCommands;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;

/**
 * This class provides a set of commands specific to JSPDocportal
 * 
 *  
 *  * @author Robert Stephan
 * 
 * @version $Revision$ $Date$
 */

@MCRCommandGroup(name = "JSPDocportal Acitiviti Commands")
public class MCRBPMNCommands extends MCRAbstractCommands {
    /** The logger */
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * The command deploys a process definition to the database from a given file
     * 
     * @param resource 
     *               the filename of a class resource with the jbpm-processdefinition
     */
    @MCRCommand(syntax = "deploy workflow processdefinition from resource {0}", help = "The command deploys a process definition to the database from a *.bpm20.xml file {0} available on classpath")
    public static final void deployProcessDefinition(String resource) throws MCRException {
        try {
            ProcessEngine processEngine = MCRBPMNMgr.getWorkflowProcessEngineConfiguration()
                    .setDatabaseSchemaUpdate(ProcessEngineConfigurationImpl.DB_SCHEMA_UPDATE_TRUE).buildProcessEngine();

            RepositoryService repositoryService = processEngine.getRepositoryService();
            repositoryService.createDeployment().addClasspathResource(resource).deploy();

            LOGGER.info("{} successfully deployed", resource);
        } catch (Exception e) {
            LOGGER.error("Error in deploying a workflow process definition", e);
            throw new MCRException("Error in deploying a workflow process definition", e);
        }
    }
}
