package org.apereo.cas.support.pac4j.authentication.clients;

import lombok.extern.slf4j.Slf4j;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;

import com.github.benmanes.caffeine.cache.Cache;
import org.apereo.cas.configuration.model.support.pac4j.Pac4jBaseClientProperties;
import org.apereo.cas.configuration.model.support.pac4j.saml.Pac4jSamlClientProperties;
import org.jooq.lambda.Unchecked;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.XMLObjectBuilder;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.schema.XSAny;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.saml2.core.Attribute;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.config.SAML2Configuration;
import org.pac4j.saml.store.SAMLMessageStoreFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectProvider;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.xml.namespace.QName;

/**
 * This is {@link DefaultDelegatedClientFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class DefaultDelegatedClientFactory extends BaseDelegatedClientFactory implements DisposableBean {

    public DefaultDelegatedClientFactory(
        final CasConfigurationProperties casProperties,
        final Collection<DelegatedClientFactoryCustomizer> customizers,
        final CasSSLContext casSSLContext,
        final ObjectProvider<SAMLMessageStoreFactory> samlMessageStoreFactory,
        final Cache<String, Collection<IndirectClient>> clientsCache) {
        super(casProperties, customizers, casSSLContext, samlMessageStoreFactory, clientsCache);
    }

    @Override
    protected Collection<IndirectClient> loadClients() {
        return buildAllIdentityProviders(casProperties);
    }

    @Override
    public void destroy() {
        Optional.ofNullable(getCachedClients())
            .stream()
            .filter(client -> client instanceof SAML2Client)
            .map(SAML2Client.class::cast)
            .forEach(Unchecked.consumer(SAML2Client::destroy));
    }

    @Override
    protected void configureClient(IndirectClient client, Pac4jBaseClientProperties clientProperties, CasConfigurationProperties givenProperties) {
        super.configureClient(client, clientProperties, givenProperties);
        if(clientProperties instanceof Pac4jSamlClientProperties && client instanceof SAML2Client) {
            SAML2Client samlClient = (SAML2Client) client;
            Pac4jSamlClientProperties samlProperties = (Pac4jSamlClientProperties) clientProperties;
            if(samlProperties.getExtensions().contains("nia")) {
                LOGGER.debug("Configuring NIA extension for SAML2 client [{}]", samlClient.getName());
                SAML2Configuration cfg = samlClient.getConfiguration();
                cfg.setAuthnRequestExtensions(() -> buildNIARequestExtension(samlProperties));
            }
        }
    }

    /**
     *
     * @param properties
     * @return List pf XAny XML elements that will be put into extensions
     */
    protected List<XSAny> buildNIARequestExtension(Pac4jSamlClientProperties properties) {
        RequestedAttributes requestedAttributesElement = createSamlObject(RequestedAttributes.class,
                RequestedAttributes.DEFAULT_ELEMENT_NAME);

        final List<String> requestedAttributes = List.of("PersonIdentifier", "CurrentGivenName", "CurrentFamilyName");

        // Also see the RequestedAttributeTemplates class ...

        for (String attr : requestedAttributes) {
            RequestedAttribute reqAttr = createSamlObject(RequestedAttribute.class, RequestedAttribute.DEFAULT_ELEMENT_NAME);
            reqAttr.setName(attr);
            reqAttr.setNameFormat(Attribute.URI_REFERENCE);
            reqAttr.setIsRequired(true);
            requestedAttributesElement.getRequestedAttributes().add(reqAttr);
        }

        return List.of((XSAny) requestedAttributesElement);
    }

    private interface EidasConstants {

        /**
         * The eIDAS SAML extension XML Namespace.
         */
        public static final String EIDAS_NS = "http://eidas.europa.eu/saml-extensions";

        /**
         * The eIDAS SAML extension QName prefix.
         */
        public static final String EIDAS_PREFIX = "eidas";

        /**
         * The eIDAS Natural Persons attribute XML Namespace.
         */
        public static final String EIDAS_NP_NS = "http://eidas.europa.eu/attributes/naturalperson";

        /**
         * The eIDAS Natural Persons attribute QName prefix.
         */
        public static final String EIDAS_NP_PREFIX = "eidasnp";

        /**
         * The eIDAS metadata service list XML namespace.
         */
        public static final String EIDAS_SERVICELIST_NS = "http://eidas.europa.eu/metadata/servicelist";

        /**
         * The eIDAS metadata service list namespace prefix.
         */
        public static final String EIDAS_SERVICELIST_PREFIX = "ser";

        /**
         * The Authentication Context URI for the "Low" Level of Assurance.
         */
        public static final String EIDAS_LOA_LOW = "http://eidas.europa.eu/LoA/low";

        /**
         * The Authentication Context URI for the "Low" Level of Assurance where the eID scheme is not notified by the eIDAS
         * country.
         */
        public static final String EIDAS_LOA_LOW_NON_NOTIFIED = "http://eidas.europa.eu/NotNotified/LoA/low";

        /**
         * Included for backwards compatibility and interoperability. Some applications used this URI for not-notified low
         * before the URI was fully determined.
         */
        public static final String EIDAS_LOA_LOW_NON_NOTIFIED2 = "http://eidas.europa.eu/LoA/NotNotified/low";
        ;

        /**
         * The Authentication Context URI for the "Substantial" Level of Assurance.
         */
        public static final String EIDAS_LOA_SUBSTANTIAL = "http://eidas.europa.eu/LoA/substantial";

        /**
         * The Authentication Context URI for the "Substantial" Level of Assurance where the eID scheme is not notified by the
         * eIDAS country.
         */
        public static final String EIDAS_LOA_SUBSTANTIAL_NON_NOTIFIED = "http://eidas.europa.eu/NotNotified/LoA/substantial";

        /**
         * Included for backwards compatibility and interoperability. Some applications used this URI for not-notified
         * substantial before the URI was fully determined.
         */
        public static final String EIDAS_LOA_SUBSTANTIAL_NON_NOTIFIED2 = "http://eidas.europa.eu/LoA/NotNotified/substantial";

        /**
         * The Authentication Context URI for the "High" Level of Assurance.
         */
        public static final String EIDAS_LOA_HIGH = "http://eidas.europa.eu/LoA/high";

        /**
         * The Authentication Context URI for the "High" Level of Assurance where the eID scheme is not notified by the eIDAS
         * country.
         */
        public static final String EIDAS_LOA_HIGH_NON_NOTIFIED = "http://eidas.europa.eu/NotNotified/LoA/high";

        /**
         * Included for backwards compatibility and interoperability. Some applications used this URI for not-notified high
         * before the URI was fully determined.
         */
        public static final String EIDAS_LOA_HIGH_NON_NOTIFIED2 = "http://eidas.europa.eu/LoA/NotNotified/high";

        /**
         * Attribute name for the entity attribute representing an eIDAS protocol version, as described in section 3.4 of the
         * "eIDAS Message Format" specification.
         */
        public static final String EIDAS_PROTOCOL_VERSION_ATTRIBUTE_NAME = "http://eidas.europa.eu/entity-attributes/protocol-version";

        /**
         * Attribute name for the entity attribute representing an eIDAS application identifier, as described in section 3.4
         * of the "eIDAS Message Format" specification.
         */
        public static final String EIDAS_APPLICATION_IDENTIFIER_ATTRIBUTE_NAME = "http://eidas.europa.eu/entity-attributes/application-identifier";

    }

    private interface NIAConstants {

        public static final String STORK_ATTRIBUTE_NS = "http://www.stork.gov.eu/1.0/";

        public static final String NIA_ATTRIBUTE_NS = "http://schemas.eidentita.cz/moris/2016/identity/claims/";
    }

    private interface RequestedAttribute extends org.opensaml.saml.saml2.metadata.RequestedAttribute {

        /** Default element name. */
        public static final QName DEFAULT_ELEMENT_NAME = new QName(EidasConstants.EIDAS_NS,
                org.opensaml.saml.saml2.metadata.RequestedAttribute.DEFAULT_ELEMENT_LOCAL_NAME,
                EidasConstants.EIDAS_PREFIX);

        /** QName of the XSI type. */
        public static final QName TYPE_NAME = new QName(EidasConstants.EIDAS_NS,
                org.opensaml.saml.saml2.metadata.RequestedAttribute.TYPE_LOCAL_NAME,
                EidasConstants.EIDAS_PREFIX);

    }

    private interface RequestedAttributes extends SAMLObject {

        /** Element name, no namespace. */
        public static final String DEFAULT_ELEMENT_LOCAL_NAME = "RequestedAttributes";

        /** Default element name. */
        public static final QName DEFAULT_ELEMENT_NAME = new QName(EidasConstants.EIDAS_NS, DEFAULT_ELEMENT_LOCAL_NAME,
                EidasConstants.EIDAS_PREFIX);

        /** Local name of the XSI type. */
        public static final String TYPE_LOCAL_NAME = "RequestedAttributesType";

        /** QName of the XSI type. */
        public static final QName TYPE_NAME = new QName(EidasConstants.EIDAS_NS, TYPE_LOCAL_NAME,
                EidasConstants.EIDAS_PREFIX);

        /**
         * Returns a reference to the list of the requested attributes.
         *
         * @return an attribute list
         */
        public List<RequestedAttribute> getRequestedAttributes();

    }

    private static <T extends XMLObject> T createSamlObject(Class<T> clazz, QName elementName) {
    if (!XMLObject.class.isAssignableFrom(clazz)) {
      throw new RuntimeException(String.format("%s is not a XMLObject class", clazz.getName()));
    }
    XMLObjectBuilderFactory builderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();
    XMLObjectBuilder<? extends XMLObject> builder = builderFactory.getBuilder(elementName);
    if (builder == null) {
      // No builder registered for the given element name. Try creating a builder for the default element name.
      builder = builderFactory.getBuilder(getDefaultElementName(clazz));
    }
    Object object = builder.buildObject(elementName);
    return clazz.cast(object);
  }

    private static <T extends XMLObject> QName getDefaultElementName(Class<T> clazz) {
        try {
            return (QName) clazz.getDeclaredField("DEFAULT_ELEMENT_NAME").get(null);
        }
        catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

}
