package cz.cuni.cas.opensaml;

import javax.xml.namespace.QName;

import lombok.extern.slf4j.Slf4j;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.XMLObjectBuilder;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.SignableSAMLObject;
import org.w3c.dom.Element;

import net.shibboleth.utilities.java.support.security.impl.RandomIdentifierGenerationStrategy;
import net.shibboleth.utilities.java.support.xml.SerializeSupport;

@Slf4j
public class OpenSAMLUtils {
	private static RandomIdentifierGenerationStrategy secureRandomIdGenerator;

	static {
		secureRandomIdGenerator = new RandomIdentifierGenerationStrategy();

	}

	public static <T> T buildSAMLObject(final Class<T> clazz) {
		T object = null;
		try {
			XMLObjectBuilderFactory builderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();
			QName defaultElementName = (QName) clazz.getDeclaredField("DEFAULT_ELEMENT_NAME").get(null);
			object = (T) builderFactory.getBuilder(defaultElementName).buildObject(defaultElementName);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Could not create SAML object");
		} catch (NoSuchFieldException e) {
			throw new IllegalArgumentException("Could not create SAML object");
		}

		return object;
	}

    public static <T extends XMLObject> XMLObjectBuilder<T> getBuilder(QName elementName) {
        return (XMLObjectBuilder<T>) XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(elementName);
    }

	public static String generateSecureRandomId() {
		return secureRandomIdGenerator.generateIdentifier();
	}

	public static void logSAMLObject(final XMLObject object) {
		Element element = null;

		if (object instanceof SignableSAMLObject && ((SignableSAMLObject) object).isSigned()
				&& object.getDOM() != null) {
			element = object.getDOM();
		} else {
			try {
				Marshaller out = XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(object);
				out.marshall(object);
				element = object.getDOM();

			} catch (MarshallingException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
		String xmlString = SerializeSupport.prettyPrintXML(element);

		LOGGER.info(xmlString);

	}}
