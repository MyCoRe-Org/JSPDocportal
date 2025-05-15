package org.mycore.jspdocportal.diskcache.generator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.output.DOMOutputter;
import org.mycore.common.content.MCRPathContent;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAO;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.metadata.MCRExpandedObject;
import org.mycore.datamodel.metadata.MCRMetaEnrichedLinkID;
import org.mycore.datamodel.metadata.MCRMetaXML;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.libmeta.common.LibmetaProcessorException;
import org.mycore.libmeta.mets.METSQuery;
import org.mycore.libmeta.mets.METSXMLProcessor;
import org.mycore.libmeta.mets.model.Mets;
import org.mycore.libmeta.mets.model._enums.AgentROLE;
import org.mycore.libmeta.mets.model._enums.LOCTYPE;
import org.mycore.libmeta.mets.model._enums.MDTYPE;
import org.mycore.libmeta.mets.model.div.Fptr;
import org.mycore.libmeta.mets.model.div.Mptr;
import org.mycore.libmeta.mets.model.filesec.File;
import org.mycore.libmeta.mets.model.filesec.FileGrp;
import org.mycore.libmeta.mets.model.filesec.file.FLocat;
import org.mycore.libmeta.mets.model.mdsec.AmdSec;
import org.mycore.libmeta.mets.model.mdsec.MdSec;
import org.mycore.libmeta.mets.model.mdsec.MdWrap;
import org.mycore.libmeta.mets.model.mdsec.XMLData;
import org.mycore.libmeta.mets.model.metshdr.Agent;
import org.mycore.libmeta.mets.model.metshdr.MetsHdr;
import org.mycore.libmeta.mets.model.structmap.Div;
import org.mycore.libmeta.mets.model.structmap.StructMap;
import org.mycore.libmeta.mods.MODSXMLProcessor;
import org.mycore.libmeta.mods.model.Mods;
import org.mycore.libmeta.mods.model._misc.types.StringPlusLanguagePlusAuthority;
import org.mycore.libmeta.mods.model._toplevel.Classification;
import org.mycore.libmeta.mods.model._toplevel.ITopLevelElement;
import org.mycore.libmeta.mods.model._toplevel.Identifier;
import org.mycore.libmeta.mods.model._toplevel.RecordInfo;
import org.mycore.libmeta.mods.model.recordinfo.RecordInfoNote;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

public class MCRDiskcacheDVMETSGenerator extends SimpleGenerator {

    private static final String NAMESPACE__ZVDD = "http://zvdd.de/namespace";

    private static final String NAMESPACE__MODS = "http://www.loc.gov/mods/v3";

    private static final String NAMESPACE__DFGVIEWER = "http://dfg-viewer.de/";

    private static final String FILEGROUP_NAME__THUMBS = "THUMBS";

    private static final String FILEGROUP_NAME__DEFAULT = "DEFAULT";

    private static final String STRUCTMAP_TYPE__PHYSICAL = "PHYSICAL";

    private static final QName QNAME_UBR_URI = new QName("http://ub.uni-rostock.de", "uri");

    private static final Logger LOGGER = LogManager.getLogger();

    private static final List<String> CC_TERMS = Arrays.asList("cc-by-nc-nd", "cc-by-nc-sa", "cc-by-nc", "cc-by-nd",
        "cc-by-sa", "cc-by", "cc0");

    private static final DateTimeFormatter ISO_FORMATTER = new DateTimeFormatterBuilder()
        .appendInstant(0).toFormatter(Locale.US);

    private static DocumentBuilderFactory facDBF;

    static {
        facDBF = DocumentBuilderFactory.newInstance();
        facDBF.setNamespaceAware(true);
    }

    @Override
    public void accept(String id, Path p) {
        if (MCRObjectID.isValid(id)) {
            MCRExpandedObject mcrObj = MCRMetadataManager.retrieveMCRExpandedObject(MCRObjectID.getInstance(id));
            Optional<MCRMetaEnrichedLinkID> optDerLink = mcrObj.getStructure().getDerivates().stream()
                .filter(x -> x.getClassifications().contains(MCRCategoryID.ofString("derivate_types:REPOS_METS")))
                .findFirst();

            Optional<MCRMetaXML> optMetaMODS = mcrObj.getMetadata().getMetadataElement("def.modsContainer").stream()
                .filter(x -> "imported".equals(x.getType())).findFirst().map(x -> (MCRMetaXML) x);

            if (optMetaMODS.isPresent() && optDerLink.isPresent()) {
                Element eMODS = getFirstContentElement(optMetaMODS.get());
                DOMOutputter out = new DOMOutputter();

                try {
                    Mods mods = MODSXMLProcessor.getInstance().unmarshal(out.output(eMODS));

                    MCRMetaEnrichedLinkID derLink = optDerLink.get();
                    derLink.getXLinkHref();
                    MCRPath metsFile = MCRPath.getPath(derLink.getXLinkHref(), derLink.getMainDoc());
                    if (Files.exists(metsFile)) {

                        try {
                            //TODO use XML source: unmarshal(MCRPathContent.getSource())
                            byte[] metsXML = new MCRPathContent(metsFile).asByteArray();
                            Mets mets = METSXMLProcessor.getInstance()
                                .unmarshal(new String(metsXML, StandardCharsets.UTF_8));
                            Files.deleteIfExists(p);
                            process(mets, mods, p);
                        } catch (IOException e) {
                            LOGGER.error(e);
                        }
                    }

                } catch (LibmetaProcessorException | JDOMException e) {
                    LOGGER.error(e);
                }
            }
        }
    }

    protected Gson getGson() {
        return new GsonBuilder()
            .setPrettyPrinting()
            .create();
    }

    private void process(Mets mets, Mods mods, Path pMetsOut) {
        try {
            updateHeader(mets);
            updateAMDSectionForDFGViewer(mets, mods);
            createPresentationFileGroups(mets);
            updateAltoFileGrp(mets);
            addPDFDownloadForCompleteDocument(mets);
            addTeaserFileGroup(mets);
            updateDMDSec(mets, mods);
            removePicaDMDSec(mets);
            updateMetsPtr(mets);

            METSXMLProcessor.getInstance().marshal(mets, pMetsOut);
        } catch (LibmetaProcessorException e) {
            LOGGER.error(e);
        }
    }

    private void createPresentationFileGroups(Mets mets) {
        //delete all fileGroups - keep ALTO
        mets.getFileSec().getFileGrp().removeIf(fg -> !"ALTO".equals(fg.getID()));
        StructMap smPhys = METSQuery.findStructMap(mets, STRUCTMAP_TYPE__PHYSICAL);
        if (smPhys != null) {
            // delete all existing filePointers - keep ALTO;
            smPhys.getDiv().getFptr().clear();
            for (Div d : smPhys.getDiv().getDiv()) {
                d.getFptr().removeIf(fptr -> !fptr.getFILEID().startsWith("ALTO_"));
            }

            String iiifBaseURL = MCRFrontendUtil.getBaseURL() + "api/iiif/image/v2";
            String[] fgNames = { FILEGROUP_NAME__DEFAULT, FILEGROUP_NAME__THUMBS };
            Map<String, String> iiifSizes = Map.of(FILEGROUP_NAME__DEFAULT, "max", FILEGROUP_NAME__THUMBS, "!256,256");
            for (String fgName : fgNames) {
                fgName = fgName.trim();
                FileGrp fg = METSQuery.findOrCreateFileGrpOfUse(mets, fgName);

                for (Div divPhys : smPhys.getDiv().getDiv()) {
                    String fileID = divPhys.getID().replace("phys_", fgName + "_file_");
                    Fptr fptr = new Fptr();

                    fptr.setFILEID(fileID);
                    divPhys.getFptr().add(fptr);

                    File f = new File();
                    f.setID(fileID);
                    f.setMIMETYPE("image/jpeg");
                    fg.getFile().add(f);

                    FLocat flocat = new FLocat();
                    flocat.setLOCTYPE(LOCTYPE.URL);
                    // https://rosdok.uni-rostock.de/iiif/image-api/rosdok_ppn642329060__phys_0008/full/max/0/native.pdf
                    flocat.setXlinkHref(iiifBaseURL + "/" + retrieveIIIFID(divPhys)
                        + "/full/" + iiifSizes.get(fgName) + "/0/default.jpg");
                    f.getFLocat().add(flocat);
                }
            }
        }
    }

    private String retrieveIIIFID(Div divPhys) {
        String purl = divPhys.getCONTENTIDS().get(0).toString();
        String iiifID = purl.replace("//", "");
        iiifID = iiifID.substring(iiifID.indexOf('/') + 1);
        iiifID = iiifID.replace('/', '_').replace("_phys", "__phys");
        return iiifID;
    }

    private void updateAMDSectionForDFGViewer(Mets mets, Mods mods) {
        String provider = retrieveProvider(mets);
        String sponsor = retrieveSponsor(mets);

        // TODO RecordInfo ri = mods.filterContent(RecordInfo.class).get(0);
        //  und weiter mit: ri.filterContent(RecordInfoNOte.class).stream()...

        String license = retrieveLicense(mods);
        try {
            DocumentBuilder db = facDBF.newDocumentBuilder();
            Document document = db.newDocument();
            mets.getAmdSec().removeIf(x -> "AMD_DFGVIEWER".equals(x.getID()));
            AmdSec amdSec = new AmdSec();
            amdSec.setID("AMD_DFGVIEWER");
            mets.getAmdSec().add(0, amdSec);

            org.w3c.dom.Element eDvRights = document.createElementNS(NAMESPACE__DFGVIEWER, "dv:rights");
            if (provider != null) {
                insertProvider(provider, document, eDvRights);
            }
            if (sponsor != null) {
                insertSponsor(sponsor, document, eDvRights);
            }
            if (license != null) {
                org.w3c.dom.Element e = document.createElementNS(NAMESPACE__DFGVIEWER, "dv:license");
                e.setTextContent(license);
                eDvRights.appendChild(e);
            }
            if (provider != null) {
                insertRightsMD(amdSec, eDvRights);

                org.w3c.dom.Element eDvLinks = document.createElementNS(NAMESPACE__DFGVIEWER, "dv:links");
                insertCatalogLink(mods, provider, document, eDvLinks);

                insertPURL(mods, document, eDvLinks);
                insertDigiProvider(amdSec, eDvLinks);

                for (AmdSec amdSec2 : mets.getAmdSec()) {
                    if ("AMD_UBROSTOCK".equals(amdSec2.getID())) {
                        mets.getAmdSec().remove(amdSec2);
                        break;
                    }
                }
            }

            StructMap smLogical = METSQuery.findStructMap(mets, "LOGICAL");
            if (smLogical != null) {
                treeStreamOfDivs(smLogical.getDiv())
                    .filter(d -> d.getDMDID() != null && d.getDMDID().contains("DMDLOG_0000"))
                    .findFirst()
                    .ifPresent(d -> {
                        d.setADMID("AMD_DFGVIEWER");
                    });
            }

        } catch (ParserConfigurationException e) {
            // should not happen;
        }
    }

    private String retrieveSponsor(Mets mets) {
        String sponsorURI =
            mets.getMetsHdr().getAgent().stream().filter(x -> "SPONSOR".equals(x.getOTHERROLE())).findFirst()
                .<String>map(x -> x.getNote().get(0).getOtherAttributes().get(QNAME_UBR_URI))
                .orElse(null);
        return sponsorURI == null ? null : sponsorURI.substring(sponsorURI.lastIndexOf('#') + 1);
    }

    private String retrieveProvider(Mets mets) {
        String providerURI = mets.getMetsHdr().getAgent().stream()
            .filter(x -> "PROVIDER".equals(x.getOTHERROLE()))
            .findFirst()
            .<String>map(x -> x.getNote().get(0).getOtherAttributes().get(QNAME_UBR_URI))
            .orElse(null);
        return providerURI == null ? null : providerURI.substring(providerURI.lastIndexOf('#') + 1);
    }

    private void insertCatalogLink(Mods mods, String provider, Document document, org.w3c.dom.Element eDvLinks) {
        MCRCategoryDAO categDAO = MCRCategoryDAOFactory.obtainInstance();

        RecordInfo ri = getMODSChildren(mods, RecordInfo.class).get(0);

        String ppn = ri.getContent().stream()
            .filter(RecordInfoNote.class::isInstance)
            .map(RecordInfoNote.class::cast)
            .filter(x -> "k10plus_ppn".equals(x.getType()))
            .findFirst()
            .get().getContent();

        MCRCategory catProvider = categDAO.getCategory(new MCRCategoryID("provider", provider), 0);
        catProvider.getLabel("x-catalog").ifPresent(l -> {
            String url = JsonParser.parseString(l.getText()).getAsJsonObject().get("opac").getAsString();
            org.w3c.dom.Element e2 = document.createElementNS(NAMESPACE__DFGVIEWER, "dv:reference");
            e2.setTextContent(url.replace("{0}", ppn));
            eDvLinks.appendChild(e2);
        });
    }

    private void insertDigiProvider(AmdSec amdSec, org.w3c.dom.Element eDvLinks) {
        XMLData xmlData = new XMLData();
        xmlData.getNodes().add(eDvLinks);
        MdWrap.Builder mdWrapBuilder2 = MdWrap.builder()
            .xmlData(xmlData)
            .MDTYPE(MDTYPE.OTHER)
            .MIMETYPE("text/xml")
            .OTHERMDTYPE("DVLINKS");

        MdSec digiprovMD = new MdSec();
        digiprovMD.setID("DIGIPROV");
        digiprovMD.setMdWrap(mdWrapBuilder2.build());
        amdSec.getDigiprovMD().add(digiprovMD);
    }

    private void insertPURL(Mods mods, Document document, org.w3c.dom.Element eDvLinks) {
        getMODSChildren(mods, Identifier.class).stream()
            .filter(i -> "purl".equals(i.getType())).findFirst().ifPresent(p -> {
                org.w3c.dom.Element e3 = document.createElementNS(NAMESPACE__DFGVIEWER, "dv:presentation");
                e3.setTextContent(p.getContent());
                eDvLinks.appendChild(e3);
            });
    }

    private void insertRightsMD(AmdSec amdSec, org.w3c.dom.Element eDvRights) {
        XMLData xmlData = new XMLData();
        xmlData.getNodes().add(eDvRights);
        MdWrap.Builder mdWrapBuilder = MdWrap.builder()
            .xmlData(xmlData)
            .MDTYPE(MDTYPE.OTHER)
            .MIMETYPE("text/xml")
            .OTHERMDTYPE("DVRIGHTS");

        MdSec rightsMD = new MdSec();
        rightsMD.setID("RIGHTS");
        rightsMD.setMdWrap(mdWrapBuilder.build());
        amdSec.getRightsMD().add(rightsMD);
    }

    private void insertSponsor(String sponsor, Document document, org.w3c.dom.Element eDvRights) {
        MCRCategoryDAO categDAO = MCRCategoryDAOFactory.obtainInstance();

        MCRCategory catSponsor = categDAO.getCategory(new MCRCategoryID("sponsor", sponsor), 0);
        catSponsor.getLabel("de").ifPresent(l -> {
            org.w3c.dom.Element e2 = document.createElementNS(NAMESPACE__DFGVIEWER, "dv:sponsor");
            e2.setTextContent(l.getText());
            eDvRights.appendChild(e2);
        });
        catSponsor.getLabel("x-dfg-viewer").ifPresent(l -> {
            String url = JsonParser.parseString(l.getText()).getAsJsonObject().get("logo_url").getAsString();
            org.w3c.dom.Element e2 = document.createElementNS(NAMESPACE__DFGVIEWER, "dv:sponsorLogo");
            e2.setTextContent(url);
            eDvRights.appendChild(e2);
        });
        catSponsor.getLabel("x-homepage").ifPresent(l -> {
            org.w3c.dom.Element e2 = document.createElementNS(NAMESPACE__DFGVIEWER, "dv:sponsorSiteURL");
            e2.setTextContent(l.getText());
            eDvRights.appendChild(e2);
        });
    }

    private void insertProvider(String provider, Document document, org.w3c.dom.Element eDvRights) {
        MCRCategoryDAO categDAO = MCRCategoryDAOFactory.obtainInstance();
        MCRCategory catProvider = categDAO.getCategory(new MCRCategoryID("provider", provider), 0);
        catProvider.getLabel("de").ifPresent(l -> {
            org.w3c.dom.Element e2 = document.createElementNS(NAMESPACE__DFGVIEWER, "dv:owner");
            e2.setTextContent(l.getText());
            eDvRights.appendChild(e2);
        });

        catProvider.getLabel("x-dfg-viewer").ifPresent(l -> {
            String url = JsonParser.parseString(l.getText()).getAsJsonObject().get("logo_url").getAsString();
            org.w3c.dom.Element e2 = document.createElementNS(NAMESPACE__DFGVIEWER, "dv:ownerLogo");
            e2.setTextContent(url);
            eDvRights.appendChild(e2);
        });

        catProvider.getLabel("x-homepage").ifPresent(l -> {
            org.w3c.dom.Element e2 = document.createElementNS(NAMESPACE__DFGVIEWER, "dv:ownerSiteURL");
            e2.setTextContent(l.getText());
            eDvRights.appendChild(e2);
        });

        catProvider.getLabel("x-contact").ifPresent(l -> {
            org.w3c.dom.Element e2 = document.createElementNS(NAMESPACE__DFGVIEWER, "dv:ownerContact");
            e2.setTextContent(l.getText());
            eDvRights.appendChild(e2);
        });
    }

    private String retrieveLicense(Mods mods) {
        String license;

        String licenseWorkURI = getMODSChildren(mods, Classification.class).stream()
            .map(StringPlusLanguagePlusAuthority::getValueURI)
            .filter(uri -> uri.contains("/licenseinfo#work"))
            .findFirst().get();
        String licenseWork = licenseWorkURI == null ? null
            : licenseWorkURI.substring(licenseWorkURI.lastIndexOf('#')).replace("#work.", "");
        if ("publicdomain".equals(licenseWork)) {
            String licenseDigiURI = getMODSChildren(mods, Classification.class).stream()
                .map(StringPlusLanguagePlusAuthority::getValueURI)
                .filter(uri -> uri.contains("/licenseinfo#digitisedimages"))
                .findFirst().get();
            String licenseDigi = licenseDigiURI == null ? null
                : licenseDigiURI.substring(licenseDigiURI.lastIndexOf('#')).replace("#digitisedimages.", "");
            if ("norestrictions".equals(licenseDigi)) {
                license = "pdm";
            } else {
                license = retrieveLicenseFromTerm(licenseDigi);
            }
        } else {
            license = retrieveLicenseFromTerm(licenseWork);
        }
        return license;
    }

    private String retrieveLicenseFromTerm(String term) {
        for (String c : CC_TERMS) {
            if (term.contains(c)) {
                return c;
            }
        }
        return "reserved";
    }

    private void updateHeader(Mets mets) {
        MetsHdr metsHdr = mets.getMetsHdr();
        metsHdr.setLASTMODDATE(ISO_FORMATTER.format(Instant.now()));
        metsHdr.setRECORDSTATUS("PRESENTATION_DV");
        mets.setOBJID(mets.getOBJID().replace("/", "_"));

        // remove all editing agents
        List<Agent> agents = metsHdr.getAgent();
        agents.removeIf(a -> "SOFTWARE".equals(a.getOTHERTYPE()) && AgentROLE.EDITOR == a.getROLE());
    }

    private void updateAltoFileGrp(Mets mets) {
        FileGrp fgALTO = METSQuery.findFileGrpOfID(mets, "ALTO");
        if (fgALTO != null) {
            fgALTO.setUSE("FULLTEXT");
            for (File f : fgALTO.getFile()) {
                f.setSIZE(null);
                f.setCHECKSUM(null);
                f.setCHECKSUMTYPE(null);
                FLocat fl = f.getFLocat().get(0);
                fl.setLOCTYPE(LOCTYPE.URL);
                fl.setOTHERLOCTYPE(null);
                fl.setXlinkHref(
                    StringUtils.stripEnd(MCRFrontendUtil.getBaseURL(), "/")
                        + "/depot/" + mets.getOBJID().replace("/", "_") + "/" + fl.getXlinkHref());
            }
        }

    }

    private void addPDFDownloadForCompleteDocument(Mets mets) {
        String id = "DOWNLOAD_file_0000";
        FileGrp fgDownload = METSQuery.findOrCreateFileGrpOfUse(mets, "DOWNLOAD");
        File fDown = new File();
        fDown.setID(id);
        fDown.setMIMETYPE("text/html");
        fgDownload.getFile().add(0, fDown);

        FLocat fLocat = new FLocat();
        fLocat.setLOCTYPE(LOCTYPE.URL);

        // https://rosdok.uni-rostock.de/do/pdfdownload/recordIdentifier/rosdok_ppn1853780359
        //TODO use 'recordIdentifier:'
        fLocat.setXlinkHref(StringUtils.stripEnd(MCRFrontendUtil.getBaseURL(), "/")
            + "/do/pdfdownload/recordIdentifier/" + mets.getOBJID() + "/" + mets.getOBJID() + ".pdf");
        fDown.getFLocat().add(fLocat);

        Div physRoot = METSQuery.findStructMap(mets, STRUCTMAP_TYPE__PHYSICAL).getDiv();
        Fptr fptr = new Fptr();
        fptr.setFILEID(id);
        physRoot.getFptr().add(fptr);
    }

    private void addTeaserFileGroup(Mets mets) {
        /*
         * //use resolving link downloadURL = env.getProperty("goobi2mycore."
         * + creator + ".metswriter.coverimage.url"); try { // double encoded
         * since single encoded slash is problematic downloadURL =
         * downloadURL.replace("{0}",
         * URLEncoder.encode(URLEncoder.encode(mets.getOBJID(), "UTF-8"),
         * "UTF-8")); } catch (UnsupportedEncodingException e) { // won't not
         * happen }
         */

        // use IIIF API
        Optional<Div> teaserDiv = METSQuery.findStructMap(mets, STRUCTMAP_TYPE__PHYSICAL).getDiv().getDiv()
            .stream()
            .filter(d -> "START_PAGE".equals(d.getXlinkLabel()))
            .findFirst();

        if (teaserDiv.isPresent()) {
            String teaserURL = MCRFrontendUtil.getBaseURL() + "api/iiif/image/v2"
                + "/" + retrieveIIIFID(teaserDiv.get()) + "/full/!400,400/0/default.jpg";
            // create TEASER filegroup
            String id = "TEASER_file_0000";
            FileGrp fgDownload = METSQuery.findOrCreateFileGrpOfUse(mets, "TEASER");
            File fDown = new File();
            fDown.setID(id);
            fDown.setMIMETYPE("text/html");
            fgDownload.getFile().add(0, fDown);

            FLocat fLocat = new FLocat();
            fLocat.setLOCTYPE(LOCTYPE.URL);
            fLocat.setXlinkHref(teaserURL);
            fDown.getFLocat().add(fLocat);

            Div physRoot = METSQuery.findStructMap(mets, STRUCTMAP_TYPE__PHYSICAL).getDiv();
            Fptr fptr = new Fptr();
            fptr.setFILEID(id);
            physRoot.getFptr().add(fptr);
        }
    }

    //TODO trennen
    private void updateDMDSec(Mets mets, Mods mods) {
        mets.getDmdSec().stream()
            .filter(dmdSec -> "DMDLOG_0000".equals(dmdSec.getID()))
            .forEach(dmdSec -> {
                dmdSec.setCREATED(null);
                dmdSec.getMdWrap().setMDTYPEVERSION(null);

                try {
                    Document docMods = MODSXMLProcessor.getInstance().marshalToDOM(mods);
                    if (docMods != null) {
                        org.w3c.dom.Element eMods = docMods.getDocumentElement();
                        // mods:note[@type='titlewordindex']  -> <mods:extension><zvdd:Wrap>
                        NodeList nl = eMods.getElementsByTagNameNS(NAMESPACE__MODS, "note");
                        for (int i = 0; i < nl.getLength(); i++) {
                            org.w3c.dom.Element e = (org.w3c.dom.Element) nl.item(i);
                            if ("titlewordindex".equals(e.getAttribute("type"))) {
                                String titlewordindex = e.getTextContent();
                                eMods.removeChild(e);

                                org.w3c.dom.Element eExtension = null;
                                NodeList nlExt =
                                    eMods.getElementsByTagNameNS(NAMESPACE__MODS, "mods:extension");
                                for (int j = 0; i < nlExt.getLength(); j++) {
                                    if ("zvdd"
                                        .equals(((org.w3c.dom.Element) nl.item(j)).getAttribute("displayLabel"))) {
                                        eExtension = (org.w3c.dom.Element) nl.item(j);
                                    }
                                }
                                if (eExtension == null) {
                                    eExtension =
                                        docMods.createElementNS(NAMESPACE__MODS, "mods:extension");
                                    eExtension.setAttribute("displayLabel", "zvdd");
                                    eMods.appendChild(eExtension);
                                }
                                eExtension.appendChild(docMods.createComment(
                                    "We have not found a valid and documented ZVDD Namespace URI !!!"));
                                org.w3c.dom.Element eZVDDWrap =
                                    docMods.createElementNS(NAMESPACE__ZVDD, "zvdd:zvddWrap");
                                eExtension.appendChild(eZVDDWrap);
                                org.w3c.dom.Element eZVDDTitleWord =
                                    docMods.createElementNS(NAMESPACE__ZVDD, "zvdd:titleWord");
                                eZVDDWrap.appendChild(eZVDDTitleWord);
                                eZVDDTitleWord.setTextContent(titlewordindex);
                            }
                        }
                        dmdSec.getMdWrap().getXmlData().getNodes().clear();
                        dmdSec.getMdWrap().getXmlData().getNodes().add(eMods.cloneNode(true));
                    }
                } catch (Exception e) {
                    //ignore
                }
            });
    }

    private void removePicaDMDSec(Mets mets) {
        StructMap smLogical = METSQuery.findStructMap(mets, "LOGICAL");
        if (smLogical != null) {
            treeStreamOfDivs(smLogical.getDiv())
                .filter(d -> d.getDMDID() != null
                    && d.getDMDID().contains("DMDLOG_0000_PICA"))
                .forEach(d -> {
                    d.setDMDID(d.getDMDID().replace("DMDLOG_0000_PICA", "").replaceAll("\\s+", " ").trim());
                });
        }

        mets.getDmdSec().removeIf(dmdSec -> "DMDLOG_0000_PICA".equals(dmdSec.getID()));
    }

    //TODO which is the "static" URL for our DV-METS files
    private void updateMetsPtr(Mets mets) {
        StructMap smLogical = METSQuery.findStructMap(mets, "LOGICAL");
        treeStreamOfDivs(smLogical.getDiv())
            .filter(d -> !d.getMptr().isEmpty())
            .forEach(div -> {
                for (Mptr mptr : div.getMptr()) {
                    String recordIdentifier = mptr.getXlinkHref().replace("/", "_");
                    mptr.setXlinkHref(StringUtils.stripEnd(MCRFrontendUtil.getBaseURL(), "/")
                        + "/generated/recordIdentifier:" + recordIdentifier + "_dv.mets.xml");
                    mptr.setOTHERLOCTYPE(null);
                    mptr.setLOCTYPE(LOCTYPE.URL);
                }
            });
    }

    //TODO move to LibMeta
    private Stream<Div> treeStreamOfDivs(Div d) {
        return Stream.concat(Stream.of(d), d.getDiv().stream().flatMap(this::treeStreamOfDivs));
    }

    //TODO in 2024.06 this should be replaced with MCRMetaXML.getFirstContentElement()
    private Element getFirstContentElement(MCRMetaXML mcrMetaXML) {
        return mcrMetaXML.getContent().stream().filter(Element.class::isInstance).map(Element.class::cast).findFirst()
            .orElse(null);
    }

    //TODO move to LibMeta
    public static <T extends ITopLevelElement> List<T> getMODSChildren(Mods mods, Class<T> type) {
        return mods.getContent().stream().filter(type::isInstance).map(type::cast).toList();
    }
}
