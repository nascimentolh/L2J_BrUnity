package org.l2jbr_unity.gameserver.api.responses;

public class CharacterResponse extends Response {
    private final int charId;
    private final String charName;
    private final int level;
    private final long exp;
    private final boolean online;
    private final int clanId;
    private final String clanName;
    private final int clanLevel;
    private final int reputationScore;
    private final String allyName;
    private final boolean nobless;

    private CharacterResponse(Builder builder) {
        super(builder.code, builder.message);
        this.charId = builder.charId;
        this.charName = builder.charName;
        this.level = builder.level;
        this.exp = builder.exp;
        this.online = builder.online;
        this.clanId = builder.clanId;
        this.clanName = builder.clanName;
        this.clanLevel = builder.clanLevel;
        this.reputationScore = builder.reputationScore;
        this.allyName = builder.allyName;
        this.nobless = builder.nobless;
    }

    public static class Builder {
        private int code;
        private String message;
        private int charId;
        private String charName;
        private int level;
        private long exp;
        private boolean online;
        private int clanId;
        private String clanName;
        private int clanLevel;
        private int reputationScore;
        private String allyName;
        private boolean nobless;

        public Builder withCode(int code) {
            this.code = code;
            return this;
        }

        public Builder withMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder withCharId(int charId) {
            this.charId = charId;
            return this;
        }

        public Builder withCharName(String charName) {
            this.charName = charName;
            return this;
        }

        public Builder withLevel(int level) {
            this.level = level;
            return this;
        }

        public Builder withExp(long exp) {
            this.exp = exp;
            return this;
        }

        public Builder withOnline(boolean online) {
            this.online = online;
            return this;
        }

        public Builder withClanId(int clanId) {
            this.clanId = clanId;
            return this;
        }

        public Builder withClanName(String clanName) {
            this.clanName = clanName;
            return this;
        }

        public Builder withClanLevel(int clanLevel) {
            this.clanLevel = clanLevel;
            return this;
        }

        public Builder withReputationScore(int reputationScore) {
            this.reputationScore = reputationScore;
            return this;
        }

        public Builder withAllyName(String allyName) {
            this.allyName = allyName;
            return this;
        }

        public Builder withNobless(boolean nobless) {
            this.nobless = nobless;
            return this;
        }

        public CharacterResponse build() {
            return new CharacterResponse(this);
        }
    }

    // Getters

    public int getCharId() {
        return charId;
    }

    public String getCharName() {
        return charName;
    }

    public int getLevel() {
        return level;
    }

    public long getExp() {
        return exp;
    }

    public boolean isOnline() {
        return online;
    }

    public int getClanId() {
        return clanId;
    }

    public String getClanName() {
        return clanName;
    }

    public int getClanLevel() {
        return clanLevel;
    }

    public int getReputationScore() {
        return reputationScore;
    }

    public String getAllyName() {
        return allyName;
    }

    public boolean isNobless() {
        return nobless;
    }
}
