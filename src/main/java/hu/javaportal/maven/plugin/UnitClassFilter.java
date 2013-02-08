package hu.javaportal.maven.plugin;

public class UnitClassFilter {
    private String name;
    private String[] packages;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getPackages() {
        return packages;
    }

    public void setPackages(String[] packages) {
        this.packages = packages;
    }
}
