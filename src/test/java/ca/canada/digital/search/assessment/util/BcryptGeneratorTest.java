package ca.canada.digital.search.assessment.util;

import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.*;

public class BcryptGeneratorTest {
    @Test
    public void testHashAndVerifyPassword() {
        // 1) Define your plaintext password (you can also parameterize this)
        String plainPassword = "s0m3P@5w0rd!";

        // 2) Generate the BCrypt hash with a cost factor of 12
        String bcryptHash = BCrypt.hashpw(plainPassword, BCrypt.gensalt());

        // 3) Ensure the hash is not null and has the expected format
        assertNotNull(bcryptHash, "BCrypt hash should not be null");
        assertTrue(bcryptHash.startsWith("$2"), "BCrypt hash should start with $2");
        assertEquals(60, bcryptHash.length(), "BCrypt hash should be 60 characters long");

        // 4) Verify that the plaintext password matches the generated hash
        assertTrue(BCrypt.checkpw(plainPassword, bcryptHash),
                "The plaintext password should match the generated BCrypt hash");
    }
}
