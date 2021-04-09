package org.mycore.jspdocportal.common.bpmn.identity;

import org.camunda.bpm.engine.impl.interceptor.Session;
import org.camunda.bpm.engine.impl.interceptor.SessionFactory;

public class MCRMyCoReIDMProviderFactory implements SessionFactory {

    public Class<?> getSessionType() {
        return MCRMyCoreIDMProvider.class;
    }

    public Session openSession() {
        return new MCRMyCoreIDMProvider();
    }

}
