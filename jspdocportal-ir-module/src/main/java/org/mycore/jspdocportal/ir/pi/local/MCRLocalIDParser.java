package org.mycore.jspdocportal.ir.pi.local;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mycore.pi.MCRPIParser;

public class MCRLocalIDParser implements MCRPIParser<MCRLocalID> {

    private static final String ID_REGEX = "([a-zA-Z]+)\\/([a-zA-Z]+)([0-9]+)([xX]?)";

    private static final Pattern ID_PATTERN = Pattern.compile(ID_REGEX);

    @Override
    public Optional<MCRLocalID> parse(String input) {
        Matcher m = ID_PATTERN.matcher(input);
        if (m.matches() && m.groupCount() >= 3) {
            String project = m.group(1);
            String prefix = m.group(2);
            long num = Long.parseLong(m.group(3));
            String check = "";
            if (m.groupCount() == 4) {
                check = m.group(4);
            }

            return Optional.of(new MCRLocalID(project, prefix, num, check));
        } else {
            return Optional.empty();

        }
    }

    public static void main(String[] args) {
        MCRLocalIDParser parser = new MCRLocalIDParser();
        Optional<MCRLocalID> x = parser.parse("rosdok/id123x");
        if (x.isPresent()) {
            System.out.println(x.get().asString());
        }
    }

}
