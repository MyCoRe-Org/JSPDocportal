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

import org.mycore.datamodel.metadata.MCRBase;
import org.mycore.pi.MCRPIGenerator;
import org.mycore.pi.MCRPIManager;
import org.mycore.pi.MCRPIRegistrationInfo;
import org.mycore.pi.exceptions.MCRPersistentIdentifierException;

public class MCRLocalIDGenerator extends MCRPIGenerator<MCRLocalID> {

    // initialize minimal ID to avoid conflicts on initial import
    // LTS 2017: 251 / LTS 2021: 3301
    private static int MIN_ID = 3101;

    @Override
    public MCRLocalID generate(MCRBase mcrBase, String additional) throws MCRPersistentIdentifierException {
        MCRLocalID id = new MCRLocalID(mcrBase.getId().getProjectId(), "id",
            getNextCount(mcrBase.getId().getProjectId(), "id"));
        return id;
    }

    // _________________________________________________________________________________________________
    //
    // TODO TO Migrate to 2018 LTS just delete everything below and extend from
    // MCRCountingDNBURNGenerator
    // _________________________________________________________________________________________________
    private static final Map<String, AtomicInteger> PATTERN_COUNT_MAP = new HashMap<>();

    public final synchronized int getNextCount(String projectID, String idPrefix) {
        String pattern = projectID + "/" + idPrefix + "([0-9]+)";
        AtomicInteger count = PATTERN_COUNT_MAP.computeIfAbsent(pattern, (pattern_) -> {
            Pattern regExpPattern = Pattern.compile(pattern_);
            Predicate<String> matching = regExpPattern.asPredicate();

            List<MCRPIRegistrationInfo> list = MCRPIManager.getInstance().getList(MCRLocalID.TYPE, -1,
                -1);

            Comparator<Integer> integerComparator = Integer::compareTo;
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
                }).filter(Objects::nonNull).sorted(integerComparator.reversed()).findFirst().map(n -> n + 1);
            return new AtomicInteger(Math.max(highestNumber.orElse(1), MIN_ID));
        });

        return count.getAndIncrement();
    }
}
