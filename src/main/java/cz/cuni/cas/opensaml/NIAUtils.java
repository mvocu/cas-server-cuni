package cz.cuni.cas.opensaml;

import org.apereo.cas.configuration.model.support.pac4j.saml.Pac4jSamlClientProperties;
import org.opensaml.core.xml.schema.XSAny;
import org.opensaml.saml.saml2.core.Attribute;
import se.litsec.eidas.opensaml.ext.RequestedAttribute;
import se.litsec.eidas.opensaml.ext.RequestedAttributeTemplates;
import se.litsec.eidas.opensaml.ext.RequestedAttributes;

import java.util.List;

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
        requestedAttributesElement.getRequestedAttributes().add(RequestedAttributeTemplates.PERSON_IDENTIFIER());
        requestedAttributesElement.getRequestedAttributes().add(RequestedAttributeTemplates.CURRENT_GIVEN_NAME());
        requestedAttributesElement.getRequestedAttributes().add(RequestedAttributeTemplates.CURRENT_FAMILY_NAME());
        requestedAttributesElement.getRequestedAttributes().add(RequestedAttributeTemplates.DATE_OF_BIRTH());
        requestedAttributesElement.getRequestedAttributes().add(RequestedAttributeTemplates.CURRENT_ADDRESS());
        return List.of((XSAny) requestedAttributesElement);
    }

}
