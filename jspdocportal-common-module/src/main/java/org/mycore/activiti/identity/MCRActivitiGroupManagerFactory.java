package org.mycore.activiti.identity;

import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.activiti.engine.impl.persistence.entity.GroupIdentityManager;

/**
 * MCRActivitiGroupManagerFactory
 * 
 * to match Activiti User Management and MyCoRe User Management
 * 
 * implementation based upon 
 * http://developer4life.blogspot.de/2012/02/activiti-authentication-and-identity.html
 * 
 * @author Robert Stephan
 * 
 */
public class MCRActivitiGroupManagerFactory implements SessionFactory {

    public MCRActivitiGroupManagerFactory() {

    }

    @Override
    public Class<?> getSessionType() {
        return GroupIdentityManager.class;
    }

    @Override
    public Session openSession() {
        return new MCRActivitiGroupManager();
    }
}
