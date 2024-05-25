import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum Command {
    EXIT(Pattern.compile("\\s*exit\\s*")),
    REGISTER(Pattern.compile("\\s*register\\s*:\\s*(?<id>\\S+)\\s*:\\s*(?<name>\\S+)\\s*:\\s*(?<money>\\S+)\\s*")),
    LOGIN(Pattern.compile("\\s*login\\s*:\\s*(?<id>\\S+)\\s*")),
    LOGOUT(Pattern.compile("\\s*logout\\s*")),
    GET_PRICE(Pattern.compile("\\s*get\\s+price\\s*:\\s*(?<name>\\S+)\\s*")),
    GET_QUANTITY(Pattern.compile("\\s*get\\s+quantity\\s*:\\s*(?<name>\\S+)\\s*")),
    GET_MONEY(Pattern.compile("\\s*get\\s+money\\s*")),
    CHARGE(Pattern.compile("\\s*charge\\s*:\\s*(?<money>\\S+)\\s*")),
    PURCHASE(Pattern.compile("\\s*purchase\\s*:\\s*(?<name>\\S+)\\s*:\\s*(?<quantity>\\S+)\\s*"));

    private final Pattern pattern;

    Command(Pattern pattern) {
        this.pattern = pattern;
    }

    public Pattern pattern() {
        return pattern;
    }

    public Matcher matcher(String input) {
        return pattern.matcher(input);
    }
}
