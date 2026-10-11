package com.globalbank.ejb;

import com.globalbank.entity.Account;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.math.BigDecimal;

/**
 * Puts three demo accounts into the database the moment the application deploys.
 *
 * <p>Two annotations carry the whole idea:</p>
 * <ul>
 *   <li>{@code @Singleton} — exactly one of these exists.</li>
 *   <li>{@code @Startup} — the container creates it immediately when the
 *       application deploys, instead of waiting for the first caller. Without
 *       this, the seeding would not happen until something asked for the bean,
 *       which is the opposite of what we want.</li>
 * </ul>
 *
 * <p>{@code @PostConstruct} is the lifecycle callback: the container calls it
 * once, right after it has injected everything the bean needs and before the
 * bean is used. "Setup that must happen exactly once" belongs here.</p>
 */
@Singleton
@Startup
public class LedgerInitializer {

    /**
     * The container hands us an EntityManager already bound to the persistence
     * unit named in persistence.xml. We never build one, never close one, and
     * never pass it around — the container owns its whole life.
     */
    @PersistenceContext
    private EntityManager em;

    @PostConstruct
    public void seed() {
        // Idempotent on purpose: if this application is ever redeployed against
        // a database that still holds data (a real one, not ExampleDS), we must
        // not insert a second copy of the demo accounts.
        Long existing = em.createQuery("SELECT COUNT(a) FROM Account a", Long.class)
                          .getSingleResult();
        if (existing > 0) {
            System.out.println("[Ledger] " + existing + " account(s) already present - not seeding");
            return;
        }

        em.persist(new Account(1001, "Ada Lovelace", new BigDecimal("1500.00")));
        em.persist(new Account(1002, "Grace Hopper", new BigDecimal("250.00")));
        em.persist(new Account(1003, "Alan Turing", new BigDecimal("0.00")));

        // Notice there is no commit() here. @PostConstruct of a @Startup bean
        // runs inside a container-managed transaction, and the container commits
        // it for us when the method returns. That is what "container-managed"
        // means, and it is why the try-it pages could seed with plain SQL and no
        // transaction handling at all.
        System.out.println("[Ledger] seeded 3 accounts: 1001, 1002, 1003");
    }
}
