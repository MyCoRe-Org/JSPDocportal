package org.mycore.jspdocportal.common.bpmn.identity;

import org.camunda.bpm.engine.impl.interceptor.Session;
import org.camunda.bpm.engine.impl.interceptor.SessionFactory;

public class MCRMyCoReIDMProviderFactory implements SessionFactory {

    @Override
    public Class<?> getSessionType() {
        return MCRMyCoreIDMProvider.class;
    }

    @Override
    public Session openSession() {
        return new MCRMyCoreIDMProvider();
    }

}
