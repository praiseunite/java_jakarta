package com.globalbank.ejb;

import com.globalbank.entity.BankUser;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;

/**
 * Creates the two demo users the moment the application deploys, exactly as
 * {@link LedgerInitializer} creates the three demo accounts.
 *
 * <p>Same two annotations, same reason: {@code @Singleton} means one of these,
 * {@code @Startup} means now rather than later, and {@code @PostConstruct} is the
 * once-only setup hook.</p>
 *
 * <h2>The interesting half: making a password hash properly</h2>
 * <p>The passwords in this lab are the memorable kind — {@code secret} and
 * {@code manager} — and the hash column holds something like</p>
 * <pre>PBKDF2WithHmacSHA256:27500:R2xvYmFsQmFua1NhbHQwMQ==:OveTdHskM+EoBddciBrb2Jia/Ithv0oYizEoyzX+dUs=</pre>
 * <p>That value is <strong>not typed by hand and not copied out of this file's
 * comments</strong>. It cannot be, and that is the point. It is produced at
 * deploy time by {@link Pbkdf2PasswordHash#generate(char[])}, which picks a fresh
 * random salt every run — so this application intentionally ends up with a
 * different hash on every deployment, all of them correct.</p>
 *
 * <p>This is the piece most tutorials leave out. They show you the
 * {@code @DatabaseIdentityStoreDefinition} that <em>checks</em> hashes, and never
 * show you anything that <em>makes</em> one, which leaves you hand-crafting the
 * four-part string and getting the iteration count or the Base64 padding subtly
 * wrong. Generate it. Never assemble it.</p>
 *
 * <h2>Where the object comes from</h2>
 * <p>{@code Pbkdf2PasswordHash} is not a class you write — it is an interface in
 * Jakarta Security, and the container supplies the implementation. {@code @Inject}
 * is enough; there is nothing to construct and nothing to configure. The
 * parameters (algorithm, iteration count, key size) are the container's sensible
 * defaults, and they can be overridden with
 * {@code hashAlgorithmParameters} on the identity store if your security policy
 * demands different ones.</p>
 */
@Singleton
@Startup
public class UserInitializer {

    @PersistenceContext
    private EntityManager em;

    /**
     * The hasher. Injected, not built.
     *
     * <p>This is the same object {@code DatabaseIdentityStore} uses to check a
     * login, which is precisely why generating with it is safe: the format is
     * correct by construction rather than by agreement.</p>
     */
    @Inject
    private Pbkdf2PasswordHash hasher;

    @PostConstruct
    public void seed() {
        // Say what the hashing parameters are rather than assuming somebody else
        // said. The container normally initialises an injected Pbkdf2PasswordHash
        // with its documented defaults, and an empty parameter map means exactly
        // those defaults — so this line changes nothing today. It is here because
        // "it works because the container happened to do it" is a worse thing to
        // depend on than "it works because this line said so", and because it is
        // where you would put a stronger policy later:
        //
        //     hasher.initialize(Map.of("Pbkdf2Iterations", "210000"));
        //
        // Raising the iteration count is the one-line answer to "our hashes are
        // weaker than they should be", which is a question every application gets
        // asked eventually.
        hasher.initialize(new java.util.HashMap<>());

        // Same idempotence guard as LedgerInitializer. Against ExampleDS the
        // table has just been created so this is always zero, but the check costs
        // one query and protects against ever double-seeding a real database.
        Long existing = em.createQuery("SELECT COUNT(u) FROM BankUser u", Long.class)
                          .getSingleResult();
        if (existing > 0) {
            System.out.println("[Users] " + existing + " user(s) already present - not seeding");
            return;
        }

        addUser("ada@globalbank.test", "secret", "CUSTOMER");
        addUser("manager@globalbank.test", "manager", "BANK_MANAGER");

        System.out.println("[Users] seeded 2 users: ada@globalbank.test (CUSTOMER), "
                + "manager@globalbank.test (BANK_MANAGER)");
    }

    /**
     * Hashes one password and writes one row. The password is in this source file
     * because it is a demo — see the warning below.
     *
     * <p><strong>The one thing not to copy from this class.</strong> A real
     * application must never hold a password in its source. This one does because
     * the point is to get you logged in thirty seconds after deploying, and a
     * hard-coded demo password is the only way to do that offline. In production
     * the user supplies the password and the application hashes it on the way to
     * the database — same {@code generate} call, different source of the
     * characters.</p>
     *
     * @param username  the login name
     * @param password  the plain password, hashed here and then forgotten
     * @param groupName the role, as {@code @RolesAllowed} will spell it
     */
    private void addUser(String username, String password, String groupName) {
        // generate() takes a char[], not a String, on purpose: a String sits in
        // memory until the garbage collector happens to reclaim it, and a char[]
        // can be overwritten the moment you are done with it.
        String hash = hasher.generate(password.toCharArray());
        em.persist(new BankUser(username, hash, groupName));

        // Note which of the three values is printed. The password is not, and the
        // hash is not. Logging is the most common way credentials leak.
        System.out.println("[Users] created " + username + " in group " + groupName);
    }
}
