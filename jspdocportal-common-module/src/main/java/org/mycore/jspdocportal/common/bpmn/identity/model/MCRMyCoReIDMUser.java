package org.mycore.jspdocportal.common.bpmn.identity.model;

import org.camunda.bpm.engine.identity.User;
import org.mycore.user2.MCRUser;

public class MCRMyCoReIDMUser implements User{
    private static final long serialVersionUID = 1L;

    private String id;
    private String firstName;
    private String lastName;
    private String eMail;
    private String password;
    
    public MCRMyCoReIDMUser(MCRUser mcrUser) {
        setId(mcrUser.getUserID());
        String name = mcrUser.getUserName();
        if(name.contains(" ")) {
            int pos = name.lastIndexOf(" ");
            setFirstName(name.substring(0, pos));
            setLastName(name.substring(pos +1 ));
        }
        else {
            setFirstName("");
            setLastName(name);
        }
        setEmail(mcrUser.getEMailAddress());
        setPassword(mcrUser.getPassword());
        
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
    public String getFirstName() {
       return firstName;
    }

    @Override
    public void setFirstName(String firstName) {
        this.firstName = firstName;
        
    }

    @Override
    public void setLastName(String lastName) {
        this.lastName = lastName;
        
    }

    @Override
    public String getLastName() {
       return lastName;
    }

    @Override
    public void setEmail(String email) {
        this.eMail = email;
        
    }

    @Override
    public String getEmail() {
        return eMail;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

}
