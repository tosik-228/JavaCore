package API_Tested.java;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Model_parse_JSON {

    private Model_parse_JSON() {
    }

    public static String getJSONStringValue(String json, String key) {
        try {
            JSONObject obj = new JSONObject(json);
            return obj.getString(key);
        }
        catch (JSONException e) {
            throw new JSONException(String.format("[JSONParser] Failed to get \"%s\"  from JSON object", key));
        }
    }

}
