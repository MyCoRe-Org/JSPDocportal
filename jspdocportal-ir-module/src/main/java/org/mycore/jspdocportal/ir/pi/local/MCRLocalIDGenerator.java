package org.mycore.jspdocportal.ir.pi.local;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom2.Element;
import org.mycore.common.MCRConstants;
import org.mycore.datamodel.metadata.MCRBase;
import org.mycore.datamodel.metadata.MCRMetaXML;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.pi.MCRPIGenerator;
import org.mycore.pi.MCRPIManager;
import org.mycore.pi.MCRPIParser;
import org.mycore.pi.MCRPIRegistrationInfo;
import org.mycore.pi.exceptions.MCRPersistentIdentifierException;

public class MCRLocalIDGenerator extends MCRPIGenerator<MCRLocalID> {

    // initialize minimal ID to avoid conflicts on initial import
    // LTS 2017: 251 / LTS 2021: 3301 / LTS 2022: 4001
    private static int MIN_ID = 4001;

    private static final String PROP_RECORDIDENTIFIER_SOURCE = "Source";

    @Override
    public MCRLocalID generate(MCRBase mcrBase, String additional) throws MCRPersistentIdentifierException {
        MCRObject mcrObj = (MCRObject) mcrBase;
        String source = getProperties().getOrDefault(PROP_RECORDIDENTIFIER_SOURCE, "DE-28");

        MCRMetaXML mcrMODS = (MCRMetaXML) mcrObj.getMetadata().findFirst("def.modsContainer").get();
        Element eMods = (Element) mcrMODS.getContent().stream().filter(x -> x.getClass().equals(Element.class))
            .findFirst().get();

        Optional<Element> eRecordID = eMods.getChild("recordInfo", MCRConstants.MODS_NAMESPACE)
            .getChildren("recordIdentifier", MCRConstants.MODS_NAMESPACE).stream()
            .filter(x -> source.equals(x.getAttributeValue("source")))
            .findFirst();
        if (eRecordID.isPresent()) {
            MCRPIParser<MCRLocalID> parser = MCRPIManager.getInstance().getParserForType(MCRLocalID.TYPE);
            Optional<MCRLocalID> optID = parser.parse(eRecordID.get().getText());
            while (true) {
                if (optID.isPresent()) {
                    MCRLocalID id = optID.get();
                    //TODO replace with MCRPIManager.getInstance().getInfo(id, MCRLocalID.TYPE);
                    Optional<MCRPIRegistrationInfo> optInfo = MCRPIManager.getInstance().getInfo(id.asString(),
                        MCRLocalID.TYPE);
                    if (optInfo.isPresent() && !optInfo.get().getMycoreID().equals(mcrBase.getId().toString())) {
                        optID = parser.parse(id.asString() + "_");
                    } else {
                        return id;
                    }
                }
                else {
                    throw new MCRPersistentIdentifierException("Could not generate ID for " + eRecordID.get());
                }
            }
        } else {
            return new MCRLocalID(mcrBase.getId().getProjectId(), "id",
                getNextCount(mcrBase.getId().getProjectId(), "id"));
        }
    }

    private static final Map<String, AtomicInteger> PATTERN_COUNT_MAP = new HashMap<>();

    //* implementation adopted from MCRCountingDNBURNGenerator
    private final synchronized int getNextCount(String projectID, String idPrefix) {
        String pattern = projectID + "/" + idPrefix + "([0-9]+)";

        AtomicInteger count = PATTERN_COUNT_MAP.computeIfAbsent(pattern, (pattern_) -> {
            Pattern regExpPattern = Pattern.compile(pattern_);
            Predicate<String> matching = regExpPattern.asPredicate();

            List<MCRPIRegistrationInfo> list = MCRPIManager.getInstance().getList(MCRLocalID.TYPE, -1,
                -1);

            Optional<Integer> highestNumber = list.stream().map(MCRPIRegistrationInfo::getIdentifier).filter(matching)
                .map(pi -> {
                    // extract the number of the PI
                    Matcher matcher = regExpPattern.matcher(pi);
                    if (matcher.find() && matcher.groupCount() == 1) {
                        String group = matcher.group(1);
                        return Integer.parseInt(group, 10);
                    } else {
                        return null;
                    }
                }).filter(Objects::nonNull)
                .min(Comparator.reverseOrder())
                .map(n -> n + 1);

            return new AtomicInteger(Math.max(highestNumber.orElse(1), MIN_ID));
        });

        return count.getAndIncrement();
    }

}
