package org.l2jbr_unity.gameserver.api.responses;

public class AccountInfoResponse extends Response {
    private final String login;
    private final String email;
    private final String createdTime;
    private final long lastActive;
    private final String lastIP;
    private final int charactersCount;
    private final double virtualCurrencyBalance;
    private final String accessLevelName;
    private final String accessLevelColor;
    private final boolean isGM;

    public AccountInfoResponse(Builder builder) {
        super(builder.code, builder.message);
        this.login = builder.login;
        this.email = builder.email;
        this.createdTime = builder.createdTime;
        this.lastActive = builder.lastActive;
        this.lastIP = builder.lastIP;
        this.charactersCount = builder.charactersCount;
        this.virtualCurrencyBalance = builder.virtualCurrencyBalance;
        this.accessLevelName = builder.accessLevelName;
        this.accessLevelColor = builder.accessLevelColor;
        this.isGM = builder.isGM;
    }

    public static class Builder {
        private int code;
        private String message;
        private String login;
        private String email;
        private String createdTime;
        private long lastActive;
        private String lastIP;
        private int charactersCount;
        private double virtualCurrencyBalance;
        private String accessLevelName;
        private String accessLevelColor;
        private boolean isGM;

        public Builder withCode(int code) {
            this.code = code;
            return this;
        }

        public Builder withMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder withLogin(String login) {
            this.login = login;
            return this;
        }

        public Builder withEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder withCreatedTime(String createdTime) {
            this.createdTime = createdTime;
            return this;
        }

        public Builder withLastActive(long lastActive) {
            this.lastActive = lastActive;
            return this;
        }

        public Builder withLastIP(String lastIP) {
            this.lastIP = lastIP;
            return this;
        }

        public Builder withCharactersCount(int charactersCount) {
            this.charactersCount = charactersCount;
            return this;
        }

        public Builder withVirtualCurrencyBalance(double virtualCurrencyBalance) {
            this.virtualCurrencyBalance = virtualCurrencyBalance;
            return this;
        }

        public Builder withAccessLevelName(String accessLevelName) {
            this.accessLevelName = accessLevelName;
            return this;
        }

        public Builder withAccessLevelColor(String accessLevelColor) {
            this.accessLevelColor = accessLevelColor;
            return this;
        }

        public Builder withIsGM(boolean isGM) {
            this.isGM = isGM;
            return this;
        }

        public AccountInfoResponse build() {
            return new AccountInfoResponse(this);
        }
    }

    // Getters para os campos

    public String getLogin() {
        return login;
    }

    public String getEmail() {
        return email;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public long getLastActive() {
        return lastActive;
    }

    public String getLastIP() {
        return lastIP;
    }

    public int getCharactersCount() {
        return charactersCount;
    }

    public double getVirtualCurrencyBalance() {
        return virtualCurrencyBalance;
    }

    public String getAccessLevelName() {
        return accessLevelName;
    }

    public String getAccessLevelColor() {
        return accessLevelColor;
    }

    public boolean isGM() {
        return isGM;
    }
}
