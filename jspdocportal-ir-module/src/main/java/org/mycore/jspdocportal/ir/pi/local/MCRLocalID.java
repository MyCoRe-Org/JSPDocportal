package org.mycore.jspdocportal.ir.pi.local;

import java.util.Locale;

import org.mycore.pi.MCRPersistentIdentifier;

public class MCRLocalID implements MCRPersistentIdentifier {
    public static final String TYPE = "local_id";

    private String project;

    private String prefix;

    private long num;

    private String check = "";

    public MCRLocalID(String project, String prefix, long num) {
        this(project, prefix, num, "");
    }

    public MCRLocalID(String project, String prefix, long num, String check) {
        super();
        this.project = project;
        this.prefix = prefix;
        this.num = num;
        this.check = check;
    }

    @Override
    public String asString() {
        // TODO Auto-generated method stub
        return project + "/" + prefix + String.format(Locale.getDefault(), "%08d", num) + check;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public long getNum() {
        return num;
    }

    public void setNum(long num) {
        this.num = num;
    }

    public String getCheck() {
        return check;
    }

    public void setCheck(String check) {
        this.check = check;
    }

}
