package com.paygate.sdk.request;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Request parameters for the {@code /pay} endpoint.
 * Only non-null fields are serialized.
 *
 * <p>Required: {@code amount}, {@code paymentMethodCode}, {@code terminalType}, {@code settlementCurrency}.
 *
 * @author alphah
 * @since 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PayRequest {

    /**
     * Payment amount, e.g. {@code "100.00"} (required)
     */
    private final String amount;
    /**
     * Payment method code, e.g. {@code "ALIPAY_CN"} (required)
     */
    private final String paymentMethodCode;
    /**
     * Terminal type: {@code "WEB"}, {@code "APP"}, {@code "WAP"} (required)
     */
    private final String terminalType;
    /**
     * Settlement currency, e.g. {@code "USD"} (required)
     */
    private final String settlementCurrency;
    /**
     * Transaction currency (optional)
     */
    private final String currency;
    /**
     * Order description for the merchant's records (optional)
     */
    private final String orderDescription;
    /**
     * ISO 3166-1 buyer country code (optional)
     */
    private final String buyerCountry;
    /**
     * Merchant's reference buyer identifier (optional)
     */
    private final String referenceBuyerId;
    /**
     * Buyer phone number (optional)
     */
    private final String buyerPhoneNo;
    /**
     * Payment method detail object as JSON string (optional)
     */
    private final String paymentMethod;
    /**
     * Available payment methods as JSON string (optional)
     */
    private final String availablePaymentMethod;
    /**
     * Saved payment methods as JSON string (optional)
     */
    private final String savedPaymentMethods;
    /**
     * Operating system type: {@code "ANDROID"} or {@code "IOS"} (optional)
     */
    private final String osType;

    private PayRequest(Builder builder) {
        this.amount = builder.amount;
        this.paymentMethodCode = builder.paymentMethodCode;
        this.terminalType = builder.terminalType;
        this.settlementCurrency = builder.settlementCurrency;
        this.currency = builder.currency;
        this.orderDescription = builder.orderDescription;
        this.buyerCountry = builder.buyerCountry;
        this.referenceBuyerId = builder.referenceBuyerId;
        this.buyerPhoneNo = builder.buyerPhoneNo;
        this.paymentMethod = builder.paymentMethod;
        this.availablePaymentMethod = builder.availablePaymentMethod;
        this.savedPaymentMethods = builder.savedPaymentMethods;
        this.osType = builder.osType;
    }

    /**
     * @return payment amount as a string, e.g. {@code "100.00"}
     */
    public String getAmount() {
        return amount;
    }

    /**
     * @return payment method, e.g. {@code "ALIPAY_CN"}
     */
    public String getPaymentMethodCode() {
        return paymentMethodCode;
    }

    /**
     * @return terminal type, e.g. {@code "WEB"}, {@code "APP"}
     */
    public String getTerminalType() {
        return terminalType;
    }

    /**
     * @return settlement currency, e.g. {@code "USD"}
     */
    public String getSettlementCurrency() {
        return settlementCurrency;
    }

    /**
     * @return transaction currency (optional)
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * @return order description (optional)
     */
    public String getOrderDescription() {
        return orderDescription;
    }

    /**
     * @return buyer country code (optional)
     */
    public String getBuyerCountry() {
        return buyerCountry;
    }

    /**
     * @return merchant's reference buyer ID (optional)
     */
    public String getReferenceBuyerId() {
        return referenceBuyerId;
    }

    /**
     * @return buyer phone number (optional)
     */
    public String getBuyerPhoneNo() {
        return buyerPhoneNo;
    }

    /**
     * @return payment method detail (optional)
     */
    public String getPaymentMethod() {
        return paymentMethod;
    }

    /**
     * @return available payment method (optional)
     */
    public String getAvailablePaymentMethod() {
        return availablePaymentMethod;
    }

    /**
     * @return saved payment methods (optional)
     */
    public String getSavedPaymentMethods() {
        return savedPaymentMethods;
    }

    /**
     * @return OS type, e.g. {@code "ANDROID"}, {@code "IOS"} (optional)
     */
    public String getOsType() {
        return osType;
    }

    /**
     * @return a new {@link Builder}
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link PayRequest}.
     */
    public static class Builder {
        private String amount;
        private String paymentMethodCode;
        private String terminalType;
        private String settlementCurrency;
        private String currency;
        private String orderDescription;
        private String buyerCountry;
        private String referenceBuyerId;
        private String buyerPhoneNo;
        private String paymentMethod;
        private String availablePaymentMethod;
        private String savedPaymentMethods;
        private String osType;

        /**
         * @param amount payment amount (required)
         */
        public Builder amount(String amount) {
            this.amount = amount;
            return this;
        }

        /**
         * @param paymentMethodCode e.g. {@code "ALIPAY_CN"} (required)
         */
        public Builder paymentMethodCode(String paymentMethodCode) {
            this.paymentMethodCode = paymentMethodCode;
            return this;
        }

        /**
         * @param terminalType e.g. {@code "WEB"} (required)
         */
        public Builder terminalType(String terminalType) {
            this.terminalType = terminalType;
            return this;
        }

        /**
         * @param settlementCurrency e.g. {@code "USD"} (required)
         */
        public Builder settlementCurrency(String settlementCurrency) {
            this.settlementCurrency = settlementCurrency;
            return this;
        }

        /**
         * @param currency transaction currency (optional)
         */
        public Builder currency(String currency) {
            this.currency = currency;
            return this;
        }

        /**
         * @param orderDescription (optional)
         */
        public Builder orderDescription(String orderDescription) {
            this.orderDescription = orderDescription;
            return this;
        }

        /**
         * @param buyerCountry ISO country code (optional)
         */
        public Builder buyerCountry(String buyerCountry) {
            this.buyerCountry = buyerCountry;
            return this;
        }

        /**
         * @param referenceBuyerId merchant's reference (optional)
         */
        public Builder referenceBuyerId(String referenceBuyerId) {
            this.referenceBuyerId = referenceBuyerId;
            return this;
        }

        /**
         * @param buyerPhoneNo (optional)
         */
        public Builder buyerPhoneNo(String buyerPhoneNo) {
            this.buyerPhoneNo = buyerPhoneNo;
            return this;
        }

        /**
         * @param paymentMethod (optional)
         */
        public Builder paymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
            return this;
        }

        /**
         * @param availablePaymentMethod (optional)
         */
        public Builder availablePaymentMethod(String availablePaymentMethod) {
            this.availablePaymentMethod = availablePaymentMethod;
            return this;
        }

        /**
         * @param savedPaymentMethods (optional)
         */
        public Builder savedPaymentMethods(String savedPaymentMethods) {
            this.savedPaymentMethods = savedPaymentMethods;
            return this;
        }

        /**
         * @param osType {@code "ANDROID"} / {@code "IOS"} (optional)
         */
        public Builder osType(String osType) {
            this.osType = osType;
            return this;
        }

        /**
         * @return the configured {@link PayRequest}
         */
        public PayRequest build() {
            if (amount == null)
                throw new IllegalArgumentException("amount is required");
            if (paymentMethodCode == null)
                throw new IllegalArgumentException("paymentMethodCode is required");
            if (terminalType == null)
                throw new IllegalArgumentException("terminalType is required");
            if (settlementCurrency == null)
                throw new IllegalArgumentException("settlementCurrency is required");
            return new PayRequest(this);
        }
    }
}
