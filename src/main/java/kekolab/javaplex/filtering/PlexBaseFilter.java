package kekolab.javaplex.filtering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;

public class PlexBaseFilter extends PlexFilter {
    static enum Operator {
        IS_OR_CONTAINS(""),
        IS_NOT_OR_DOES_NOT_CONTAIN("!"),
        STRING_EQUALS("="),
        STRING_DOES_NOT_EQUAL("!="),
        BEGINS_WITH("<"),
        ENDS_WITH(">"),
        IS_LESS_THAN("<<"),
        IS_GREATER_THAN(">>");

        private final String value;

        public String getValue() {
            return value;
        }

        private Operator(String value) {
            this.value = value;
        }
    }

    private final String field;
    private final PlexBaseFilter.Operator operator;
    private final String value;

    protected PlexBaseFilter(String field, PlexBaseFilter.Operator operator, String value) {
        this.field = Objects.requireNonNull(field);
        this.operator = Objects.requireNonNull(operator);
        this.value = Objects.requireNonNull(value);
    }

    public String getField() {
        return field;
    }

    public String getValue() {
        return value;
    }

    public PlexBaseFilter.Operator getOperator() {
        return operator;
    }

    @Override
    public List<NameValuePair> getQueryParameters() {
        return Arrays.asList(new BasicNameValuePair(field.concat(operator.getValue()), value));
    }

    @Override
    public PlexFilter and(PlexFilter... conditions) {
        return buildNewConditionWithOperator(PlexCompositeFilter.Operator.AND, conditions);
    }

    @Override
    public PlexFilter or(PlexFilter... conditions) {
        return buildNewConditionWithOperator(PlexCompositeFilter.Operator.OR, conditions);
    }

    private PlexFilter buildNewConditionWithOperator(PlexCompositeFilter.Operator and,
            PlexFilter... conditions) {
        List<PlexFilter> c = new ArrayList<>(Arrays.asList(conditions));
        c.add(this);
        return new PlexCompositeFilter(and, c.toArray(new PlexFilter[0]));
    }

    private static Operator parseOperator(String value) {
        if (value.endsWith(Operator.IS_GREATER_THAN.getValue()))
            return Operator.IS_GREATER_THAN;
        if (value.endsWith(Operator.IS_LESS_THAN.getValue()))
            return Operator.IS_LESS_THAN;
        if (value.endsWith(Operator.ENDS_WITH.getValue()))
            return Operator.ENDS_WITH;
        if (value.endsWith(Operator.BEGINS_WITH.getValue()))
            return Operator.BEGINS_WITH;
        if (value.endsWith(Operator.STRING_DOES_NOT_EQUAL.getValue()))
            return Operator.STRING_DOES_NOT_EQUAL;
        if (value.endsWith(Operator.STRING_EQUALS.getValue()))
            return Operator.STRING_EQUALS;
        if (value.endsWith(Operator.IS_NOT_OR_DOES_NOT_CONTAIN.getValue()))
            return Operator.IS_NOT_OR_DOES_NOT_CONTAIN;
        return Operator.IS_OR_CONTAINS;
    }

    public static PlexFilter parse(NameValuePair pair) {
        String key = pair.getName();
        Operator operator = parseOperator(key);
        int index = key.indexOf(operator.getValue());
        if (index > 0)
            key = key.substring(0, index);
        String value = pair.getValue();

        return new PlexBaseFilter(key, operator, value);
    }

}