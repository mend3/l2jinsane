package enginemods.main.holders;

public class ValuesHolder {
    private final String _mod;

    private final String _event;

    private String _value;

    public ValuesHolder(String mod, String event, String value) {
        this._mod = mod;
        this._event = event;
        this._value = value;
    }

    public String getEvent() {
        return this._event;
    }

    public String getMod() {
        return this._mod;
    }

    public String getValue() {
        return this._value;
    }

    public void setValue(String value) {
        this._value = value;
    }
}
