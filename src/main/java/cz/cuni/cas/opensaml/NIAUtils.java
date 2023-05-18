package cz.cuni.cas.opensaml;

import org.apereo.cas.configuration.model.support.pac4j.saml.Pac4jSamlClientProperties;
import org.opensaml.core.xml.schema.XSAny;
import org.opensaml.core.xml.schema.impl.XSAnyBuilder;
import org.opensaml.saml.saml2.core.Attribute;
import se.litsec.eidas.opensaml.ext.RequestedAttribute;
import se.litsec.eidas.opensaml.ext.RequestedAttributeTemplates;
import se.litsec.eidas.opensaml.ext.RequestedAttributes;

import java.util.List;
import java.util.stream.Collectors;

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
        return List.of(requestedAttributesElement).stream()
                .map(element -> {
                    XSAny anyElement = (XSAny) OpenSAMLUtils.getBuilder(XSAny.TYPE_NAME).buildObject(element.getElementQName());
                    anyElement.setDOM(element.getDOM());
                    return anyElement;
                }).collect(Collectors.toList());

    }
}
