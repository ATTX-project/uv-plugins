/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uh.hulib.attx.uv.e.selectds;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author jkesanie
 */
public class ATTXClient {
    
    public String gmapiEndpoint;
    public String fusekiEndpoint;
    
    public ATTXClient(String gmapiEndpoint, String fusekiEndpoint) {
        this.gmapiEndpoint = gmapiEndpoint;
        this.fusekiEndpoint = fusekiEndpoint;
                
    }
    
    
    public List<OptionValue> getWorkingGraphs() {
        
        List<OptionValue> values = new ArrayList<OptionValue>();
        try {
            String query = "SELECT ?uri ?title ?source\n" +
"from <http://data.hulib.helsinki.fi/attx/prov>\n" +
"WHERE {\n" +
" 	?uri a <http://data.hulib.helsinki.fi/attx/onto#Dataset> .\n" +
"  	?uri <http://data.hulib.helsinki.fi/attx/uri> ?source .\n" +
"	?uri <http://data.hulib.helsinki.fi/attx/title> ?title \n" +
"   MINUS {?uri <http://data.hulib.helsinki.fi/attx/license> ?license}\n" +
"}";
            HttpResponse<String> r = Unirest.post("http://fuseki:3030/ds/query")
                        .header("Content-Type", "application/sparql-query")
                        .header("Accept", "application/sparql-results+json")
                        .body(query)
                        .asString();
            if(r.getStatus() == 404) {
                return values;
            }
            System.out.print(r.getBody());
            JSONObject root = new JSONObject(r.getBody());
            JSONArray a = null;
            try {
                a = root.getJSONObject("results").getJSONArray("bindings");
            }catch(Exception ex) {
                a = new JSONArray(root.getJSONObject("results").getJSONObject("bindings"));
            }
            for(int i = 0; i < a.length(); i++) {
                String uri = a.getJSONObject(i).getJSONObject("source").getString("value");
                String label = a.getJSONObject(i).getJSONObject("title").getString("value");
                values.add(new OptionValue(uri, label));

            }
            return values;
        }catch(Exception ex) {
            ex.printStackTrace();
            values.add(new OptionValue("error", ex.getMessage()));
            return values;
        }        

    }
}
