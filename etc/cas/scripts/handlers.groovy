def run(Object[] args) {
    def handlers = args[0]
    def transaction = args[1]
    def servicesManager = args[2]
    def logger = args[3]


    result = []
    for ( handler in handlers ) {
        handler.getName() in ['veda-prod', 'veda-proto', 'veda-test'] ?:  result.add(handler)
    }
    logger.info("Default authentication handlers: [{}]", result)
    return (Set) result
}

def supports(Object[] args) {
    def handlers = args[0]
    def transaction = args[1]
    def servicesManager = args[2]
    def logger = args[3]

    return true
}
