package org.mycore.jspdocportal.ir.pi.local;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mycore.pi.MCRPIParser;

public class MCRLocalIDParser implements MCRPIParser<MCRLocalID> {

    private static final Pattern ID_PATTERN = Pattern.compile(
        "^(?<project>[a-zA-Z]+)[\\/_]"
            + "(?:(?<prefix>ppn)(?<num>[0-9]{8,9})(?<check>[0-9xX](?:[_]*))"
            + "|(?:(?<prefix2>(?!ppn)[a-zA-Z]*|[a-zA-Z]*[0-9]{4}-)(?<num2>[0-9]*)(?<check2>[_]*)))$");

    @Override
    public Optional<MCRLocalID> parse(String input) {
        Matcher m = ID_PATTERN.matcher(input);
        if (m.find()) {
            if (m.group("prefix2") == null || m.group("prefix2").isEmpty()) {
                return Optional.of(new MCRLocalID(m.group("project"), m.group("prefix"), Long.parseLong(m.group("num")),
                    m.group("check")));
            } else {
                return Optional
                    .of(new MCRLocalID(m.group("project"), m.group("prefix2"), Long.parseLong(m.group("num2")),
                        m.group("check2")));
            }
        } else {
            return Optional.empty();
        }

    }

    @SuppressWarnings("PMD.SystemPrintln")
    public static void main(String[] args) {
        MCRLocalIDParser parser = new MCRLocalIDParser();
        Optional<MCRLocalID> x = parser.parse("rosdok/id123x");
        if (x.isPresent()) {
            System.out.println(x.get().asString());
        }
    }

}
