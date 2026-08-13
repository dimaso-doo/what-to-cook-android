package rs.brainno.stakuvamo;

public enum CookingMode {
    STRICT("strict"),
    IDEAS("ideas");

    public final String apiValue;

    CookingMode(String apiValue) {
        this.apiValue = apiValue;
    }
}
