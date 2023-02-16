import java.util.*
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

byte[] run(final Object... args) {
    def rawPassword = args[0]
    def generatedSalt = args[1]
    def logger = args[2]
    def casApplicationContext = args[3]

    def encoder = new BCryptPasswordEncoder(10)
    //logger.debug("Encoding password [{}] with salt [{}]", rawPassword, generatedSalt)
    return encoder.encode(rawPassword)
}

Boolean matches(final Object... args) {
    def rawPassword = args[0]
    def encodedPassword = args[1]
    def logger = args[2]
    def casApplicationContext = args[3]

    def encoder = new BCryptPasswordEncoder(10)
    //logger.debug("Does match or not ? raw: [{}] encoded: [{}]", rawPassword, encodedPassword)
    return encoder.matches(rawPassword, encodedPassword)
}

