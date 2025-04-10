package org.mycore.jspdocportal.ir.bpmn.workflows.create_object_simple;

import org.mycore.jspdocportal.common.bpmn.workflows.create_object_simple.MCRAbstractWorkflowMgr;
import org.mycore.jspdocportal.common.bpmn.workflows.create_object_simple.MCRWorkflowMgr;

public class MCRWorkflowMgrEditHistbest extends MCRAbstractWorkflowMgr implements MCRWorkflowMgr {

    @Override
    protected String getDefaultMetadataXML(String mcrBase) {
        if (mcrBase.endsWith("_bundle")) {
            return """
                <metadata>
                  <def.modsContainer class="MCRMetaXML">
                    <modsContainer inherited="0" type="imported">
                      <mods:mods xmlns:mods="http://www.loc.gov/mods/v3" version="3.7">
                        <mods:titleInfo xml:lang="de" usage="primary">
                           <mods:title>Neues Bundle</mods:title>
                        </mods:titleInfo>
                        <mods:genre displayLabel="doctype" authorityURI="/classifications/doctype" valueURI="/classifications/doctype#histbest" />
                      </mods:mods>
                    </modsContainer>
                  </def.modsContainer>
                </metadata>
                """;

        } else {
            return """
                <metadata>
                  <def.modsContainer class="MCRMetaXML">
                    <modsContainer inherited="0" type="imported">
                      <mods:mods xmlns:mods="http://www.loc.gov/mods/v3" version="3.7">
                        <mods:titleInfo xml:lang="de" usage="primary">
                           <mods:title>Neues Dokument</mods:title>
                        </mods:titleInfo>
                        <mods:genre displayLabel="doctype" authorityURI="/classifications/doctype" valueURI="/classifications/doctype#histbest" />
                      </mods:mods>
                    </modsContainer>
                  </def.modsContainer>
                </metadata>
                """;
        }
    }

}
