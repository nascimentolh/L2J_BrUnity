package org.l2jbr_unity.gameserver.api.enums;

public enum AccessLevel {
    BANNED(-1, "Banned", "FFFFFF", "ECF9A2", false, false, false, false, false, false, false, false),
    USER(0, "User", "FFFFFF", "ECF9A2", false, false, false, true, false, true, true, true),
    CHAT_MODERATOR(10, "Chat Moderator", "FFFFFF", "ECF9A2", false, false, false, true, false, true, true, true),
    TEST_GM(20, "Test GM", "FFFFFF", "ECF9A2", false, false, true, false, true, false, false, false),
    GENERAL_GM(30, "General GM", "0000C0", "0000C0", false, false, true, false, true, false, false, false),
    SUPPORT_GM(40, "Support GM", "000C00", "000C00", false, false, true, false, true, false, false, false),
    EVENT_GM(50, "Event GM", "00C000", "00C000", false, false, true, false, true, false, false, false),
    HEAD_GM(60, "Head GM", "0C0000", "0C0000", false, false, true, true, true, true, true, true),
    ADMIN(70, "Admin", "0FF000", "0FF000", true, true, true, true, true, true, true, true),
    MASTER(100, "Master", "00CCFF", "00CCFF", true, true, true, true, true, true, true, true);

    private final int level;
    private final String name;
    private final String nameColor;
    private final String titleColor;
    private final boolean isGM;
    private final boolean allowPeaceAttack;
    private final boolean allowFixedRes;
    private final boolean allowTransaction;
    private final boolean allowAltg;
    private final boolean giveDamage;
    private final boolean takeAggro;
    private final boolean gainExp;

    AccessLevel(int level, String name, String nameColor, String titleColor, boolean isGM,
                boolean allowPeaceAttack, boolean allowFixedRes, boolean allowTransaction,
                boolean allowAltg, boolean giveDamage, boolean takeAggro, boolean gainExp) {
        this.level = level;
        this.name = name;
        this.nameColor = nameColor;
        this.titleColor = titleColor;
        this.isGM = isGM;
        this.allowPeaceAttack = allowPeaceAttack;
        this.allowFixedRes = allowFixedRes;
        this.allowTransaction = allowTransaction;
        this.allowAltg = allowAltg;
        this.giveDamage = giveDamage;
        this.takeAggro = takeAggro;
        this.gainExp = gainExp;
    }

    public int getLevel() {
        return level;
    }

    public String getName() {
        return name;
    }

    public String getNameColor() {
        return nameColor;
    }

    public String getTitleColor() {
        return titleColor;
    }

    public boolean isGM() {
        return isGM;
    }

    public boolean allowPeaceAttack() {
        return allowPeaceAttack;
    }

    public boolean allowFixedRes() {
        return allowFixedRes;
    }

    public boolean allowTransaction() {
        return allowTransaction;
    }

    public boolean allowAltg() {
        return allowAltg;
    }

    public boolean giveDamage() {
        return giveDamage;
    }

    public boolean takeAggro() {
        return takeAggro;
    }

    public boolean gainExp() {
        return gainExp;
    }

    public static AccessLevel getByLevel(int level) {
        for (AccessLevel accessLevel : AccessLevel.values()) {
            if (accessLevel.getLevel() == level) {
                return accessLevel;
            }
        }
        return AccessLevel.USER;
    }

}
