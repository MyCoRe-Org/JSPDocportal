/*
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
 *
 * MyCoRe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyCoRe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MyCoRe.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mycore.jspdocportal.ir.iiif;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.MCRConstants;
import org.mycore.common.MCRException;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRPathContent;
import org.mycore.datamodel.metadata.MCRMetaEnrichedLinkID;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.iiif.presentation.impl.MCRIIIFPresentationImpl;
import org.mycore.iiif.presentation.model.basic.MCRIIIFManifest;
import org.mycore.jspdocportal.ir.pi.local.MCRLocalID;
import org.mycore.pi.MCRPIManager;
import org.mycore.pi.MCRPIRegistrationInfo;
import org.xml.sax.SAXException;

public class MCRJSPMetsIIIFPresentationImpl extends MCRIIIFPresentationImpl {

    private static final Logger LOGGER = LogManager.getLogger();

    public MCRJSPMetsIIIFPresentationImpl(String implName) {
        super(implName);
    }

    @Override
    public MCRIIIFManifest getManifest(String id) {
        try {
            Document metsDocument = getMets(id);
            metsDocument.getRootElement().setAttribute("schemaLocation",
                "http://www.loc.gov/METS/ http://www.loc.gov/standards/mets/mets.xsd http://www.w3.org/1999/xlink http://www.loc.gov/standards/xlink/xlink.xsd",
                MCRConstants.XSI_NAMESPACE);

            //temporary fixes
            //remove all <mets:note> elements
            XPathExpression<Element> xpathNote = XPathFactory.instance().compile(
                ".//mets:note", Filters.element(), null, MCRConstants.METS_NAMESPACE);
            for (Element e : xpathNote.evaluate(metsDocument)) {
                e.getParentElement().removeContent(e);
            }

            XPathExpression<Element> xpathFileGrp = XPathFactory.instance().compile(
                ".//mets:fileGrp[@ID='IMAGES']", Filters.element(), null, MCRConstants.METS_NAMESPACE);
            Element eFileGrp = xpathFileGrp.evaluateFirst(metsDocument);
            eFileGrp.setAttribute("USE", "IMAGES");

            XPathExpression<Element> xpathAmdSec = XPathFactory.instance().compile(
                ".//mets:amdSec", Filters.element(), null, MCRConstants.METS_NAMESPACE);
            List<Element> eAmdSecs = xpathAmdSec.evaluate(metsDocument);
            for (Element e : eAmdSecs) {
                if (e.getAttribute("ID") == null) {
                    e.setAttribute("ID", "AMDSEC_0" + eAmdSecs.indexOf(e));
                }
            }

            XPathExpression<Element> xpathStructMapPhysDiv = XPathFactory.instance().compile(
                ".//mets:structMap[@TYPE='PHYSICAL']/mets:div", Filters.element(), null, MCRConstants.METS_NAMESPACE);
            Element eStructMapPhysDiv = xpathStructMapPhysDiv.evaluateFirst(metsDocument);
            eStructMapPhysDiv.removeChildren("fptr", MCRConstants.METS_NAMESPACE);

            LOGGER.debug(() -> new XMLOutputter(Format.getPrettyFormat()).outputString(metsDocument));
            return getConverter(id, metsDocument).convert();
        } catch (IOException | JDOMException | SAXException e) {
            throw new MCRException(e);
        }
    }

    protected MCRJSPMetsMods2IIIFConverter getConverter(String id, Document metsDocument) {
        return new MCRJSPMetsMods2IIIFConverter(metsDocument, normalizeIdentifier(id));
    }

    private Document getMets(String id) throws IOException, JDOMException, SAXException {
        Optional<MCRPIRegistrationInfo> optRegInfo = MCRPIManager.getInstance().getInfo(normalizeIdentifier(id),
            MCRLocalID.TYPE);
        if (optRegInfo.isPresent()) {
            String mcrid = optRegInfo.get().getMycoreID();
            MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(mcrid));
            Optional<MCRMetaEnrichedLinkID> optMCRViewerDerLink = mcrObj.getStructure().getDerivates().stream()
                .filter(x -> x.getClassifications().stream()
                    .filter(c -> "REPOS_METS".equals(c.getID()))
                    .findFirst().isPresent())
                .findFirst();
            if (optMCRViewerDerLink.isPresent()) {
                MCRMetaEnrichedLinkID derLink = optMCRViewerDerLink.get();
                Path p = MCRPath.getPath(derLink.getXLinkHref(), derLink.getMainDoc());
                if (Files.exists(p)) {
                    MCRContent content = new MCRPathContent(p);
                    return content.asXML();
                }
            }
        }
        return null;

    }

    private String normalizeIdentifier(String id) {
        String normalizedIdentifier = URLDecoder
            .decode(URLDecoder.decode(id, StandardCharsets.UTF_8), StandardCharsets.UTF_8)
            .replace("..", "");
        if (!normalizedIdentifier.contains("/")) {
            normalizedIdentifier = normalizedIdentifier.replaceFirst("_", "/");
        }
        return normalizedIdentifier;
    }
}
