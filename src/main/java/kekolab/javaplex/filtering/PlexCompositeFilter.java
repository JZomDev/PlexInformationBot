package kekolab.javaplex.filtering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import kekolab.javaplex.PlexException;

public class PlexCompositeFilter extends PlexFilter {
    static enum Operator {
        OR("or"), AND("and");

        private String value;

        private Operator(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private List<PlexFilter> conditions;
    private final PlexCompositeFilter.Operator operator;

    protected PlexCompositeFilter(PlexCompositeFilter.Operator operator, PlexFilter... conditions) {
        this.conditions = Arrays.asList(Objects.requireNonNull(conditions));
        this.operator = Objects.requireNonNull(operator);
    }

    @Override
    public PlexFilter and(PlexFilter... conditions) {
        return buildNewConditionWithOperator(Operator.AND, conditions);
    }

    @Override
    public PlexFilter or(PlexFilter... conditions) {
        return buildNewConditionWithOperator(Operator.OR, conditions);
    }

    private PlexFilter buildNewConditionWithOperator(PlexCompositeFilter.Operator and, PlexFilter... conditions) {
        List<PlexFilter> c = new ArrayList<>();
        c.add(this);
        c.addAll(Arrays.asList(conditions));
        return new PlexCompositeFilter(and, c.toArray(new PlexFilter[0]));
    }

    @Override
    public List<NameValuePair> getQueryParameters() {
        List<NameValuePair> queryParameters = conditions.stream().map(PlexFilter::getQueryParameters)
                .reduce((c1, c2) -> {
                    List<NameValuePair> reduced = new ArrayList<>();
                    reduced.addAll(c1);
                    reduced.add(new BasicNameValuePair(operator == Operator.OR ? "or" : "and", "1"));
                    reduced.addAll(c2);
                    return reduced;
                }).get();
        queryParameters.add(0, new BasicNameValuePair("push", "1"));
        queryParameters.add(new BasicNameValuePair("pop", "1"));
        return queryParameters;
    }

    public static PlexFilter parse(Iterator<NameValuePair> params) {
        Operator operator = null;
        List<PlexFilter> filters = new ArrayList<>();
        while (params.hasNext()) {
            NameValuePair param = params.next();
            String key = param.getName();
            if (key.equals("push"))
                filters.add(parse(params));
            else if (key.equals("and") && operator == null)
                operator = Operator.AND;
            else if (key.equals("or") && operator == null)
                operator = Operator.OR;
            else if (key.equals("pop"))
                return new PlexCompositeFilter(operator, filters.toArray(new PlexFilter[0]));
            else
                filters.add(PlexBaseFilter.parse(param));
        }
        throw new PlexException("Cannot parse condition"); // TODO Improve
    }
}