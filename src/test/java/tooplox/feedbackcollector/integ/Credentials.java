package tooplox.feedbackcollector.integ;

public record Credentials(String userName, String password) {
    public static Credentials anonymous() {
        return new Credentials(null, null);
    }

    public static Credentials withName(String name) {
        return new Credentials(name, "P@ssw0rd");
    }

    public String basicAuth() {
        return "Basic " + java.util.Base64.getEncoder().encodeToString((userName + ":" + password).getBytes());
    }

    public boolean isAnonymous() {
        return userName == null && password == null;
    }
}

