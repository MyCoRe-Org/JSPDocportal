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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRConstants;
import org.mycore.common.MCRException;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.MCRLabel;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.iiif.image.MCRIIIFImageUtil;
import org.mycore.iiif.image.impl.MCRIIIFImageImpl;
import org.mycore.iiif.image.impl.MCRIIIFImageNotFoundException;
import org.mycore.iiif.image.impl.MCRIIIFImageProvidingException;
import org.mycore.iiif.image.model.MCRIIIFImageInformation;
import org.mycore.iiif.image.model.MCRIIIFImageProfile;
import org.mycore.iiif.presentation.model.additional.MCRIIIFAnnotation;
import org.mycore.iiif.presentation.model.attributes.MCRDCMIType;
import org.mycore.iiif.presentation.model.attributes.MCRIIIFLDURI;
import org.mycore.iiif.presentation.model.attributes.MCRIIIFMetadata;
import org.mycore.iiif.presentation.model.attributes.MCRIIIFResource;
import org.mycore.iiif.presentation.model.attributes.MCRIIIFService;
import org.mycore.iiif.presentation.model.attributes.MCRIIIFViewingHint;
import org.mycore.iiif.presentation.model.basic.MCRIIIFCanvas;
import org.mycore.iiif.presentation.model.basic.MCRIIIFManifest;
import org.mycore.iiif.presentation.model.basic.MCRIIIFRange;
import org.mycore.iiif.presentation.model.basic.MCRIIIFSequence;
import org.mycore.mets.model.Mets;
import org.mycore.mets.model.files.FileGrp;
import org.mycore.mets.model.sections.DmdSec;
import org.mycore.mets.model.struct.LogicalDiv;
import org.mycore.mets.model.struct.LogicalStructMap;
import org.mycore.mets.model.struct.MDTYPE;
import org.mycore.mets.model.struct.MdWrap;
import org.mycore.mets.model.struct.PhysicalDiv;
import org.mycore.mets.model.struct.PhysicalStructMap;
import org.mycore.mets.model.struct.PhysicalSubDiv;

public class MCRJSPMetsMods2IIIFConverter {

    private static final Logger LOGGER = LogManager.getLogger();

    protected final Document metsDocument;

    protected final Mets mets;

    protected final String identifier;

    protected final FileGrp imageGrp;

    protected final Map<String, List<String>> logicalIdIdentifiersMap = new HashMap<>();

    protected final Map<String, PhysicalSubDiv> identifierPhysicalMap = new ConcurrentHashMap<>();

    protected final Map<PhysicalSubDiv, String> physicalIdentifierMap = new ConcurrentHashMap<>();

    protected final Map<String, PhysicalSubDiv> idPhysicalMetsMap = new ConcurrentHashMap<>();

    private final String idSeparator = MCRConfiguration2.getString("MCR.IIIFImage.Iview.IdentifierSeparator")
        .orElse("__");

    public MCRJSPMetsMods2IIIFConverter(Document metsDocument, String identifier) {
        this.metsDocument = metsDocument;
        this.mets = new Mets(metsDocument);
        this.identifier = identifier;

        FileGrp imageGrp = mets.getFileSec().getFileGroup("IMAGES");
        if (imageGrp == null) {
            throw new MCRException("Could not find a image file group in mets!");
        }
        this.imageGrp = imageGrp;

        this.mets.getPhysicalStructMap()
            .getDivContainer()
            .getChildren()
            .parallelStream()
            .forEach(physicalSubDiv -> {
                String id = getIIIFIdentifier(physicalSubDiv);
                identifierPhysicalMap.put(id, physicalSubDiv);
                physicalIdentifierMap.put(physicalSubDiv, id);
                idPhysicalMetsMap.put(physicalSubDiv.getId(), physicalSubDiv);
            });

        mets.getStructLink().getSmLinks().forEach(smLink -> {
            String logicalId = smLink.getFrom();

            List<String> identifiers;
            if (this.logicalIdIdentifiersMap.containsKey(logicalId)) {
                identifiers = this.logicalIdIdentifiersMap.get(logicalId);
            } else {
                identifiers = new ArrayList<>();
                this.logicalIdIdentifiersMap.put(logicalId, identifiers);
            }

            identifiers.add(physicalIdentifierMap.get(idPhysicalMetsMap.get(smLink.getTo())));
        });

    }

    public MCRIIIFManifest convert() {
        MCRIIIFManifest manifest = new MCRIIIFManifest();

        // root chapter ^= manifest metadata
        LogicalStructMap logicalStructMap = (LogicalStructMap) mets.getStructMap("LOGICAL");
        LogicalDiv divContainer = logicalStructMap.getDivContainer();
        List<MCRIIIFMetadata> metadata = extractMedataFromLogicalDiv(mets, divContainer);
        manifest.metadata = metadata;
        manifest.setId(this.identifier);

        PhysicalStructMap physicalStructMap = (PhysicalStructMap) mets.getStructMap("PHYSICAL");
        PhysicalDiv physicalDivContainer = physicalStructMap.getDivContainer();
        String id = physicalDivContainer.getId();

        MCRIIIFSequence sequence = new MCRIIIFSequence(id);

        List<PhysicalSubDiv> children = physicalDivContainer.getChildren();
        MCRIIIFImageImpl imageImpl = MCRIIIFImageImpl.obtainInstance(getImageImplName());
        MCRIIIFImageProfile profile = imageImpl.getProfile();
        profile.setId(MCRIIIFImageUtil.getProfileLink(imageImpl));
        sequence.canvases = children.stream().map(physicalSubDiv -> {
            String order = physicalSubDiv.asElement().getAttributeValue("ORDER");
            String orderLabel = physicalSubDiv.getOrderLabel();
            //String contentIds = physicalSubDiv.getContentIds();
            String label = Stream.of(order, orderLabel)
                .filter(o -> o != null && !o.isEmpty())
                .collect(Collectors.joining(" - "));
            label = ("".equals(label)) ? physicalSubDiv.getId() : label;

            String identifier = this.physicalIdentifierMap.get(physicalSubDiv);
            try {
                MCRIIIFImageInformation information = imageImpl.getInformation(
                    metsDocument.getRootElement().getAttributeValue("OBJID").replace("/", "_")
                        + idSeparator + physicalSubDiv.getId());
                MCRIIIFCanvas canvas = new MCRIIIFCanvas(identifier, label, information.width, information.height);

                MCRIIIFAnnotation annotation = new MCRIIIFAnnotation(identifier, canvas);
                canvas.images.add(annotation);

                MCRIIIFResource resource = new MCRIIIFResource(information.getId(), MCRDCMIType.IMAGE);
                resource.setWidth(information.width);
                resource.setHeight(information.height);

                MCRIIIFService service = new MCRIIIFService(information.getId(), profile.getContext());
                service.profile = MCRIIIFImageProfile.IIIF_PROFILE_2_0;
                resource.setService(service);

                annotation.setResource(resource);

                return canvas;
            } catch (MCRIIIFImageNotFoundException | MCRIIIFImageProvidingException e) {
                throw new MCRException("Error while providing ImageInfo for " + identifier, e);
            } catch (MCRAccessException e) {
                LOGGER.warn("User has no access to {}", identifier);
                return null;
            }
        }).filter(Objects::nonNull)
            .collect(Collectors.toList());

        manifest.sequences.add(sequence);

        List<MCRIIIFRange> complete = new ArrayList<>();
        processDivContainer(complete, divContainer);
        manifest.structures.addAll(complete);
        manifest.setLabel(logicalStructMap.getDivContainer().getLabel());

        //link to frontpage - use "homepage" in IIIF presentation 3.0
        MCRIIIFLDURI seealsoHTML = new MCRIIIFLDURI(MCRConfiguration2.getStringOrThrow("MCR.Identifier.PURL.BaseURL")
            + metsDocument.getRootElement().getAttributeValue("OBJID"), null, "text/html");
        seealsoHTML.setContext(null);
        manifest.seeAlso = new ArrayList<>(List.of(seealsoHTML));

        // does not work because we removed the mets:note elements with additional attributes: 
        //  XPathExpression<Attribute> xpathProvider = XPathFactory.instance().compile(
        //     ".//mets:agent[@OTHERROLE='PROVIDER']/mets:note/@ubr:uri", Filters.attribute(), null,
        //     MCRConstants.METS_NAMESPACE, Namespace.getNamespace("ubr", "http://ub.uni-rostock.de"));

        XPathExpression<Attribute> xpathProvider = XPathFactory.instance().compile(
            ".//mets:dmdSec[@ID='DMDLOG_0000']//mods:classification[@displayLabel='provider']/@valueURI",
            Filters.attribute(), null, MCRConstants.METS_NAMESPACE, MCRConstants.MODS_NAMESPACE);
        Attribute attrProvider = xpathProvider.evaluateFirst(metsDocument);
        if (attrProvider != null) {
            String providerID = attrProvider.getValue();
            providerID = providerID.substring(providerID.lastIndexOf('#') + 1);
            MCRCategory categ = MCRCategoryDAOFactory.obtainInstance()
                .getCategory(new MCRCategoryID("provider", providerID), 0);
            Optional<MCRLabel> oLabel = categ.getLabel("x-logo");
            if (oLabel.isPresent()) {
                MCRIIIFResource logo = new MCRIIIFResource(
                    MCRFrontendUtil.getBaseURL() + StringUtils.removeStart(oLabel.get().getText(), "/"),
                    MCRDCMIType.IMAGE);
                manifest.setLogo(logo);
            }
        }
        return manifest;
    }

    protected void processDivContainer(List<MCRIIIFRange> complete, LogicalDiv divContainer) {
        MCRIIIFRange range = new MCRIIIFRange(divContainer.getId());
        if (divContainer.getParent() == null) {
            range.setViewingHint(MCRIIIFViewingHint.TOP);
        }
        complete.add(range);
        range.setLabel((divContainer.getLabel() != null) ? divContainer.getLabel() : divContainer.getType());

        range.canvases.addAll(this.logicalIdIdentifiersMap.getOrDefault(divContainer.getId(), Collections.emptyList()));

        divContainer.getChildren()
            .forEach(div -> {
                processDivContainer(complete, div);
                range.ranges.add(div.getId());
            });

        range.metadata = extractMedataFromLogicalDiv(mets, divContainer);

    }

    protected String getImageImplName() {
        return "Iview";
    }

    protected String getIIIFIdentifier(PhysicalSubDiv subDiv) {
        return subDiv.getId();
    }

    protected List<MCRIIIFMetadata> extractMedataFromLogicalDiv(Mets mets, LogicalDiv divContainer) {
        String dmdId = divContainer.getDmdId();
        if (dmdId != null && !dmdId.isEmpty()) {
            dmdId = dmdId.split("\\s")[0];
            DmdSec dmdSec = mets.getDmdSecById(dmdId);
            if (dmdSec != null) {
                MdWrap mdWrap = dmdSec.getMdWrap();
                MDTYPE mdtype = mdWrap.getMdtype();
                MCRJSPMetsIIIFModsMetadataExtractor extractor;
                switch (mdtype) {
                    case MODS:
                        extractor = new MCRJSPMetsIIIFModsMetadataExtractor();
                        break;
                    default:
                        LOGGER.info("No extractor found for mdType: {}", mdtype);
                        return Collections.emptyList();
                }

                return extractor
                    .extractModsMetadata(mdWrap.asElement().getChild("xmlData", MCRConstants.METS_NAMESPACE));
            }
        }

        return Collections.emptyList();
    }

}
