package org.mycore.jspdocportal.common.bpmn.identity.model;

import org.camunda.bpm.engine.identity.Group;
import org.mycore.user2.MCRRole;

public class MCRMyCoReIDMGroup implements Group{
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String type = MCRMyCoReIDMGroup.class.getName();
    
    
    public MCRMyCoReIDMGroup(MCRRole mcrRole) {
        setId(mcrRole.getName());
        setName(mcrRole.getLabel().getText());
    }
    
    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
        
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
       this.name = name;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
        
    }

}
