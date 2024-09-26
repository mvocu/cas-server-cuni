package cz.cuni.cas.opensaml;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.model.support.pac4j.saml.Pac4jSamlClientProperties;
import org.opensaml.core.xml.schema.XSAny;
import se.litsec.eidas.opensaml.ext.RequestedAttributeTemplates;
import se.litsec.eidas.opensaml.ext.RequestedAttributes;

import java.util.List;

@Slf4j
public class NIAUtils {

    public static final String NIA_ATTRIBUTE_EMAIL = "http://www.stork.gov.eu/1.0/eMail";
    public static final String NIA_ATTRIBUTE_PHONE = "http://schemas.eidentita.cz/moris/2016/identity/claims/phonenumber";
    public static final String NIA_ATTRIBUTE_TRADRESAID = "http://schemas.eidentita.cz/moris/2016/identity/claims/tradresaid";
    public static final String NIA_ATTRIBUTE_LOA = "http://eidas.europa.eu/LoA";
    public static final String NIA_ATTRIBUTE_IDTYPE = "http://schemas.eidentita.cz/moris/2016/identity/claims/idtype";
    public static final String NIA_ATTRIBUTE_IDNUMBER = "http://schemas.eidentita.cz/moris/2016/identity/claims/idnumber";

    /**
     *
     * @param properties
     * @return List pf XAny XML elements that will be put into extensions
     */
    public static List<XSAny> buildNIARequestExtension(Pac4jSamlClientProperties properties) {
        RequestedAttributes requestedAttributesElement = OpenSAMLUtils.buildSAMLObject(RequestedAttributes.class);
        XSAny anyElement = (XSAny) OpenSAMLUtils.getBuilder(XSAny.TYPE_NAME).buildObject(requestedAttributesElement.getElementQName());

        /* eIDAS attributes */
        anyElement.getUnknownXMLObjects().add(RequestedAttributeTemplates.PERSON_IDENTIFIER(true, true));
        anyElement.getUnknownXMLObjects().add(RequestedAttributeTemplates.CURRENT_GIVEN_NAME(true, true));
        anyElement.getUnknownXMLObjects().add(RequestedAttributeTemplates.CURRENT_FAMILY_NAME(true, true));
        anyElement.getUnknownXMLObjects().add(RequestedAttributeTemplates.DATE_OF_BIRTH(true, false));
        anyElement.getUnknownXMLObjects().add(RequestedAttributeTemplates.CURRENT_ADDRESS(true, false));
        anyElement.getUnknownXMLObjects().add(RequestedAttributeTemplates.GENDER(true, false));
        /* NIA attributes */
        anyElement.getUnknownXMLObjects().add(RequestedAttributeTemplates.create(NIA_ATTRIBUTE_EMAIL, "email", null, false));
        anyElement.getUnknownXMLObjects().add(RequestedAttributeTemplates.create(NIA_ATTRIBUTE_PHONE, "phone", null, false));
        anyElement.getUnknownXMLObjects().add(RequestedAttributeTemplates.create(NIA_ATTRIBUTE_TRADRESAID, "TRAdresaID", null, false));
        anyElement.getUnknownXMLObjects().add(RequestedAttributeTemplates.create(NIA_ATTRIBUTE_LOA, "loa", null, false));
        anyElement.getUnknownXMLObjects().add(RequestedAttributeTemplates.create(NIA_ATTRIBUTE_IDTYPE, "idtype", null, false));
        anyElement.getUnknownXMLObjects().add(RequestedAttributeTemplates.create(NIA_ATTRIBUTE_IDNUMBER, "idnumber", null, false));

        return List.of(anyElement);
    }
}
