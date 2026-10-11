package com.globalbank.web;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * One transfer request, straight off the web form.
 *
 * <p>This class contains <strong>no validation code at all</strong>. It contains
 * a set of labels saying what a valid request looks like, and that is the entire
 * idea behind Jakarta Bean Validation: you declare the rule once, on the field,
 * and every layer of the application enforces it — the web page before the form
 * is submitted, the EJB container before the business method runs, and JPA before
 * the row is written.</p>
 *
 * <p>Read the annotations as English:</p>
 * <ul>
 *   <li>{@code @Min(1000) @Max(9999)} — an account number here is four digits.</li>
 *   <li>{@code @NotNull} — the field must be present. (Note: {@code ""} would
 *       pass, because an empty string is not null.)</li>
 *   <li>{@code @NotBlank} — for text: present, and not just spaces.</li>
 *   <li>{@code @DecimalMin("0.01")} — money must be positive. {@code @Positive}
 *       would do the same job for a number; {@code @DecimalMin} is used here so
 *       the message can name the actual smallest amount.</li>
 *   <li>{@code @Digits(integer = 10, fraction = 2)} — at most two decimal places,
 *       because there is no such thing as half a penny.</li>
 * </ul>
 *
 * <p>The interesting one is {@link #isAccountsDiffer()} at the bottom: a rule
 * that involves <em>two</em> fields at once, which no single-field annotation can
 * express.</p>
 */
public class TransferRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Min(value = 1000, message = "Account numbers have at least 4 digits")
    @Max(value = 9999, message = "Account numbers have at most 4 digits")
    private int fromAccount;

    @Min(value = 1000, message = "Account numbers have at least 4 digits")
    @Max(value = 9999, message = "Account numbers have at most 4 digits")
    private int toAccount;

    @NotNull(message = "Enter an amount")
    @DecimalMin(value = "0.01", message = "The smallest transfer is 0.01")
    @Digits(integer = 10, fraction = 2, message = "Money has at most 2 decimal places")
    private BigDecimal amount = new BigDecimal("10.00");

    /** A free-text note. Not used by the bank — it shows @NotBlank and @Size. */
    @NotBlank(message = "Every transfer needs a reference")
    @Size(max = 40, message = "Keep the reference under 40 characters")
    private String reference = "web transfer";

    /**
     * JSF creates this object with {@code new}, so a no-argument constructor is
     * required. (That is a web-framework requirement, not a validation one.)
     */
    public TransferRequest() {
    }

    // ------------------------------------------------------------------
    // The cross-field rule
    // ------------------------------------------------------------------

    /**
     * A rule that mentions two fields: money cannot be sent from an account to
     * itself.
     *
     * <p>There is no {@code @NotSame} annotation, so this is how you express it:
     * a method returning {@code boolean}, named as a property, marked
     * {@code @AssertTrue}. The engine calls it and, if it returns false, raises
     * the message below.</p>
     *
     * <p><strong>Look at the first line before you read the rest of the
     * condition.</strong> A brand-new form has both fields at zero, which means
     * {@code fromAccount == toAccount} is true and the rule would fail — so the
     * page would greet a visitor with "you cannot transfer from an account to
     * itself" before they had typed anything. Guarding against the untouched
     * form is part of writing the rule, not a way of cheating it.</p>
     */
    @AssertTrue(message = "Money cannot be sent from an account to itself")
    public boolean isAccountsDiffer() {
        return fromAccount == 0 || toAccount == 0 || fromAccount != toAccount;
    }

    // ------------------------------------------------------------------
    // Getters and setters — the bridge JSF uses to move values between the
    // HTML form and this object. A getter that returned something different
    // from what the setter stored would break the page in a way that is very
    // hard to see, so keep them boring.
    // ------------------------------------------------------------------

    public int getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(int fromAccount) {
        this.fromAccount = fromAccount;
    }

    public int getToAccount() {
        return toAccount;
    }

    public void setToAccount(int toAccount) {
        this.toAccount = toAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}
