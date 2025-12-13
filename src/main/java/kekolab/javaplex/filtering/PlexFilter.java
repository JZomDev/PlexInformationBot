package kekolab.javaplex.filtering;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.hc.core5.http.NameValuePair;

import kekolab.javaplex.PlexException;

public abstract class PlexFilter {
    public abstract List<NameValuePair> getQueryParameters();

    public abstract PlexFilter or(PlexFilter... conditions);

    public abstract PlexFilter and(PlexFilter... conditions);

    public static PlexFilter parse(List<NameValuePair> queryParameters) {
        Iterator<NameValuePair> qp = queryParameters.iterator();
        List<PlexFilter> filters = new ArrayList<>();
        PlexCompositeFilter.Operator operator = null;
        while (qp.hasNext()) {
            NameValuePair pair = qp.next();
            String key = pair.getName();
            if (key.equals("push"))
                filters.add(PlexBaseFilter.parse(pair));
            else if (key.equals("and") && operator == null)
                operator = PlexCompositeFilter.Operator.AND;
            else if (key.equals("or") && operator == null)
                operator = PlexCompositeFilter.Operator.OR;
            else
                filters.add(PlexCompositeFilter.parse(qp));
        }

        if (filters.size() == 1)
            return filters.get(0);
        else if (filters.size() > 1 && operator != null)
            return new PlexCompositeFilter(operator, filters.toArray(new PlexFilter[0]));

        throw new PlexException("Error while parsing condition");
    }
}
