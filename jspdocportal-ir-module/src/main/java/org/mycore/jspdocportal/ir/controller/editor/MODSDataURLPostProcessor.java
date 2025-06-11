package org.mycore.jspdocportal.ir.controller.editor;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.MCRConstants;
import org.mycore.datamodel.common.MCRDataURL;
import org.mycore.datamodel.common.MCRDataURLEncoding;
import org.mycore.frontend.xeditor.MCRPostProcessorXSL;

public class MODSDataURLPostProcessor extends MCRPostProcessorXSL {

    @Override
    public Document process(Document oldXML) throws IOException, JDOMException {
        final Document newXML = oldXML.clone();

        //
        final XPathExpression<Element> dataURLXPath = XPathFactory.instance().compile(".//mods:abstract[@altFormat]",
            Filters.element(), null, MCRConstants.MODS_NAMESPACE,
            MCRConstants.XLINK_NAMESPACE);

        for (Element e : dataURLXPath.evaluate(newXML)) {
            String content = "<html>" + e.getTextNormalize() + "</html>";
            content = content.replace("&nbsp;", "&#160;");
            String mimeType = Optional.ofNullable(e.getAttributeValue("contentType")).orElse("text/xml");
            MCRDataURL dataURL = new MCRDataURL(content.getBytes(StandardCharsets.UTF_8), MCRDataURLEncoding.BASE64,
                mimeType, StandardCharsets.UTF_8);
            e.setAttribute("altFormat", dataURL.toString());
            SAXBuilder sb = new SAXBuilder();
            Document doc = sb.build(new StringReader(content));
            StringBuffer sbText = new StringBuffer();
            doc.getDescendants(Filters.text()).forEach(t -> sbText.append(t.getValue()).append(' '));
            e.setText(StringUtils.normalizeSpace(sbText.toString()));
        }

        return super.process(newXML);
    }

}
