package com.globalbank.ejb;

import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Singleton;

/**
 * Hands out the next account number.
 *
 * <p>Why a {@code @Singleton}? Because there must be exactly ONE counter in the
 * whole application. Two copies would hand out the same number twice.</p>
 *
 * <p>Why {@code @Lock(LockType.WRITE)}? Because {@code ++next} is not one
 * operation — it is "read next, add one, write it back". If two requests did
 * that at the same moment, both could read 2000 and both write 2001, and two
 * customers would get the same account number. By default a singleton is
 * WRITE-locked, but writing it out loud is how you show a reader that you meant
 * it, and it is what the exam asks about.</p>
 *
 * <p>Rule of thumb: READING methods can be {@code @Lock(LockType.READ)} so
 * several callers run at once; anything that CHANGES state needs WRITE.</p>
 */
@Singleton
@Lock(LockType.WRITE)
public class AccountNumberGenerator {

    /** Account numbers we hand out start just above the seeded ones (1001-1003). */
    private int next = 2000;

    public int nextAccountNumber() {
        return ++next;
    }
}
