package org.mycore.jspdocportal.ir.oai;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRException;
import org.mycore.common.MCRSystemUserInformation;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mcr.cronjob.MCRCronjob;
import org.mycore.solr.MCRSolrCoreManager;
import org.mycore.solr.auth.MCRSolrAuthenticationLevel;
import org.mycore.solr.auth.MCRSolrAuthenticationManager;
import org.mycore.util.concurrent.MCRFixedUserCallable;

/**
 * Cronjob for setting the servflag oai-ready 
 * on published objects after a configured time period (7 days)";
 * 
 * @author Robert Stephan
 *
 */
public class MCRSetStatePublishedStableCronjob extends MCRCronjob {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String FILTER_QUERY_OAI_READY =
        "(+category:doctype\\:epub +state:published +modified:[* TO NOW-7DAYS/DAY])";

    private void makeObjectOAIReady(MCRObjectID mcrObjectID) {
        if (MCRMetadataManager.exists(mcrObjectID)) {
            final MCRObject mcrObject = MCRMetadataManager.retrieveMCRObject(mcrObjectID);
            mcrObject.getService().setState("published_stable");
            LOGGER.info("Set state 'published_stable' for {}", mcrObjectID);
            try {
                MCRMetadataManager.update(mcrObject);
            } catch (MCRAccessException e) {
                throw new MCRException("Error while setting oai-ready in " + mcrObjectID.toString() + "!", e);
            }
        }
    }

    @Override
    public void runJob() {
        try {
            new MCRFixedUserCallable<Boolean>(() -> {
                if (MCRConfiguration2.getString("MCR.Solr.ServerURL").isPresent()) {
                    final SolrClient solrClient = MCRSolrCoreManager.getMainSolrClient();
                    final ModifiableSolrParams params = new ModifiableSolrParams();

                    params.set("start", 0);
                    params.set("rows", Integer.MAX_VALUE - 1);
                    params.set("fl", "id");
                    params.set("q", FILTER_QUERY_OAI_READY);
                    params.set("fq", "objectKind:mycoreobject");

                    try {

                        QueryRequest queryRequest = new QueryRequest(params);
                        MCRSolrAuthenticationManager.obtainInstance().applyAuthentication(queryRequest,
                            MCRSolrAuthenticationLevel.SEARCH);
                        final QueryResponse response = queryRequest.process(solrClient);
                        response.getResults().stream()
                            .map(result -> (String) result.get("id"))
                            .map(MCRObjectID::getInstance)
                            .forEach(this::makeObjectOAIReady);

                    } catch (SolrServerException | IOException e) {
                        LOGGER.error("Error while searching documents, which are oai-ready!", e);
                    }
                }
                return true;
            }, MCRSystemUserInformation.SYSTEM_USER).call();
        } catch (Exception e) {
            LOGGER.error("Errow while calling cron job", e);
        }
    }

    @Override
    public String getDescription() {
        return "Cronjob for setting the servflag oai-ready after a configured time period (7 days)";
    }
}
