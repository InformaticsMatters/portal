package portal.domain;

public enum Title {

    MS("Ms."),
    MR("Mr."),
    DR("Dr."),
    PROF("Prof.");

    private String displayName;

    private Title(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

}
