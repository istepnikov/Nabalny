package space.greenraven.android.nabalny.remoting;

public class VoteStats {
    String errors = "";
    String[] labels;
    Integer[] values;

    public String getErrors() {
        return errors;
    }

    public void setErrors(String errors) {
        this.errors = errors;
    }

    public String[] getLabels() {
        return labels;
    }

    public void setLabels(String[] labels) {
        this.labels = labels;
    }

    public Integer[] getValues() {
        return values;
    }

    public void setValues(Integer[] values) {
        this.values = values;
    }
}
