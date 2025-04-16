package org.mycore.jspdocportal.common.taglibs;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.jspdocportal.common.navigation.model.NavigationItem;
import org.mycore.jspdocportal.common.navigation.model.NavigationObject;

import jakarta.servlet.jsp.JspContext;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.PageContext;

/**
 * <p>
 * Appends a JavaBean to the PAGE_SCOPE, that can be traversed with JSTL tags.
 * </p>
 * <p> The name of the variable is passed by the attribute "var" </p>
 * <p>
 * Also it requires the attribute "id". The id is used to retrieve the
 * navigation from the navigation.xml.
 * </p>
 * <p>
 * The attribute "expanded" just points out, if the whole navigation tree should
 * be passed to the bean or just the first level.
 * </p>
 */ 
 
/* Example usage:
 * 
 * <pre>
 * @code{
 * <mcr:customNavigation id="left" expanded="true" var="navigation"/>
 * <ul>
 * <c:forEach var="n1" items="${navigation}">
 * <li>
 * <a href="${n1.href }" target="_self">${n1.label }</a> <c:if
 * test="${n1.active }">
 * <ul>
 * <li>
 * <c:forEach var="n2" items="${n1.children }"> <a href="${n2.href }"
 * target="_self" >${n2.label }</a> </c:forEach>
 * </ul>
 * </c:if></li>
 * </c:forEach>
 * </ul>
 * }
 * </pre>
 * @author Robert Stephan, Christian Windolf
 * 
 */
public class MCRCustomNavigationTag extends MCRAbstractNavigationTag {
    private static final Logger LOGGER = LogManager.getLogger();

    private String var;

    @Override
    public void doTag() throws JspException, IOException {
        JspContext context = getJspContext();
        init(id);

        if (nav == null) {
            if (path == null || path.length == 0) {
                LOGGER.error("No navigation item found for navigation: {}, path: {}", id, currentPath);
            } else {
                LOGGER.error("No navigation item found for navigation: {}, path: {}, item: {}", id, currentPath, 
                    path[0]);
            }
            return;
        }

        if (nav.getChildren().isEmpty()) {
            return;
        }
        context.setAttribute(var, getNavigation(nav), PageContext.PAGE_SCOPE);

    }

    /**
     * Puts all navigation variables into a list of {@link NavigationVariables}.
     * If the {@link MCRAbstractNavigationTag#path} points to a child of e, it
     * retrieves also its children. The index is used to determine, on which
     * level of the path this method is working
     * 
     * @param e
     * @param index
     * @return
     */
    private List<NavigationVariables> getNavigation(NavigationObject e, int index) {
        List<NavigationVariables> navigation = new LinkedList<>();
        List<NavigationItem> peList = printableItems(e);
        for (NavigationItem el : peList) {
            NavigationVariables n = new NavigationVariables();
            String id = el.getId();
            n.setId(id);
            n.setLabel(retrieveI18N(el.getI18n()));
            //n.setHref(baseURL + "nav?path=" + el.getAttribute("_path"));
            n.setHref(MCRFrontendUtil.getBaseURL() + el.getHref());
            if (index >= path.length) {
                n.setActive(false);
            } else {
                n.setActive(path[index].equals(id));
            }

            if (index < path.length) {
                if (expanded || path[index].equals(id)) {
                    if (index < path.length - 1) {
                        n.setChildren(getNavigation(el, index + 1));
                    } else {
                        n.setChildren(getNavigation(el, index));
                    }
                }
            }

            navigation.add(n);
        }
        return navigation;

    }

    private List<NavigationVariables> getNavigation(NavigationObject e) {
        return getNavigation(e, 0);
    }

    /**
     * The variable that is used to bind the {@link NavigationVariables} to the
     * PAGE_SCOPE
     * 
     * @param var
     */
    public void setVar(String var) {
        this.var = var;
    }

    /**
     * Holds all navigation variables for the JSP to build the navigation
     * 
     * @author Christian Windolf
     * 
     */
    public static class NavigationVariables {
        private String label;
        private String href;
        private List<NavigationVariables> children;
        private String id;
        private boolean active;

        /**
         * The localized link label for this item.
         * 
         * @return
         */
        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        /**
         * The URL that links to the target
         * 
         * @return
         */
        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }

        /**
         * If this navigation item has children they are stored here
         * 
         * @return may be null, if it has no children.
         */
        public List<NavigationVariables> getChildren() {
            return children;
        }

        public void setChildren(List<NavigationVariables> children) {
            this.children = children;
        }

        public boolean isActive() {
            return active;
        }

        /**
         * 
         * @return true, if
         *         {@link NavigationVariables#getChildren()} ist
         *         not null or empty
         */
        public boolean isExpanded() {
            if (children == null) {
                return false;
            }
            return !children.isEmpty();
        }

        public void setId(String id) {
            this.id = id;
        }

        /**
         * The id of this navigation element
         * 
         * @return
         */
        public String getId() {
            return this.id;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

    }

}
