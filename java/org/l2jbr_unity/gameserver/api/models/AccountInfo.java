package org.l2jbr_unity.gameserver.api.models;

import org.l2jbr_unity.gameserver.api.enums.AccessLevel;

import java.util.Objects;

public class AccountInfo {
    private final String login;
    private final String passHash;
    private final AccessLevel accessLevel;
    private final int lastServer;

    private AccountInfo(Builder builder) {
        this.login = Objects.requireNonNull(builder.login, "login cannot be null").toLowerCase();
        this.passHash = Objects.requireNonNull(builder.passHash, "passHash cannot be null");
        if (login.isEmpty() || passHash.isEmpty()) {
            throw new IllegalArgumentException("login and passHash cannot be empty");
        }
        this.accessLevel = Objects.requireNonNull(builder.accessLevel, "accessLevel cannot be null");
        this.lastServer = builder.lastServer;
    }

    public boolean checkPassHash(String passHash) {
        return this.passHash.equals(passHash);
    }

    public String getLogin() {
        return login;
    }

    public AccessLevel getAccessLevel() {
        return accessLevel;
    }

    public int getLastServer() {
        return lastServer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountInfo that = (AccountInfo) o;
        return login.equals(that.login);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login);
    }

    @Override
    public String toString() {
        return "AccountInfo{" +
                "login='" + login + '\'' +
                ", accessLevel=" + accessLevel.getName() +
                ", lastServer=" + lastServer +
                '}';
    }

    public static class Builder {
        private String login;
        private String passHash;
        private AccessLevel accessLevel;
        private int lastServer;

        public Builder withLogin(String login) {
            this.login = login;
            return this;
        }

        public Builder withPassHash(String passHash) {
            this.passHash = passHash;
            return this;
        }

        public Builder withAccessLevel(AccessLevel accessLevel) {
            this.accessLevel = accessLevel;
            return this;
        }

        public Builder withLastServer(int lastServer) {
            this.lastServer = lastServer;
            return this;
        }

        public AccountInfo build() {
            return new AccountInfo(this);
        }
    }
}