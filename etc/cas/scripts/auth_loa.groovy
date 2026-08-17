def run(Object[] args) {
    def attributeName = args[0]
    def attributeValues = args[1]
    def logger = args[2]
    def registeredService = args[3]
    def attributes = args[4]
    
    logger.debug("XXX attribute definition name: ${attributeName}, values: ${attributeValues}, authnContextClass: ${attributes['authnContextClass']}")

    return attributeValues
}

