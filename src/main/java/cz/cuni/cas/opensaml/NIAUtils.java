package cz.cuni.cas.opensaml;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.model.support.pac4j.saml.Pac4jSamlClientProperties;
import org.opensaml.core.xml.schema.XSAny;
import se.litsec.eidas.opensaml.ext.RequestedAttributeTemplates;
import se.litsec.eidas.opensaml.ext.RequestedAttributes;

import java.util.List;

@Slf4j
public class NIAUtils {

    public interface NIAConstants {

        public static final String STORK_ATTRIBUTE_NS = "http://www.stork.gov.eu/1.0/";

        public static final String NIA_ATTRIBUTE_NS = "http://schemas.eidentita.cz/moris/2016/identity/claims/";
    }

    /**
     *
     * @param properties
     * @return List pf XAny XML elements that will be put into extensions
     */
    public static List<XSAny> buildNIARequestExtension(Pac4jSamlClientProperties properties) {
        RequestedAttributes requestedAttributesElement = OpenSAMLUtils.buildSAMLObject(RequestedAttributes.class);
        requestedAttributesElement.getRequestedAttributes().add(RequestedAttributeTemplates.PERSON_IDENTIFIER(true, true));
        requestedAttributesElement.getRequestedAttributes().add(RequestedAttributeTemplates.CURRENT_GIVEN_NAME(true, true));
        requestedAttributesElement.getRequestedAttributes().add(RequestedAttributeTemplates.CURRENT_FAMILY_NAME(true, true));
        requestedAttributesElement.getRequestedAttributes().add(RequestedAttributeTemplates.DATE_OF_BIRTH(true, true));
        requestedAttributesElement.getRequestedAttributes().add(RequestedAttributeTemplates.CURRENT_ADDRESS(true, true));
        // convert element to requested type
        XSAny anyElement = (XSAny) OpenSAMLUtils.getBuilder(XSAny.TYPE_NAME).buildObject(requestedAttributesElement.getElementQName());
        LOGGER.debug("Adding list of requested attributes for NIA: [{}]", requestedAttributesElement.getRequestedAttributes());
        anyElement.getUnknownXMLObjects().addAll(requestedAttributesElement.getRequestedAttributes());
        return List.of(anyElement);
    }
}
