package juloo.keyboard2;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

/** Reads and writes [ThemeSpec] as JSON. Kept apart from [ThemeSpec] so that
    the theme logic stays free of org.json and can be unit tested on the JVM.

    The format is:
      {"name":"Mine","base":"dark",
       "colors":{"colorKey":"#292e42"},"keyBorderRadiusDp":6} */
public class ThemeSpecJson
{
  static final String F_NAME = "name";
  static final String F_BASE = "base";
  static final String F_COLORS = "colors";
  static final String F_RADIUS = "keyBorderRadiusDp";

  public static JSONObject to_json(ThemeSpec spec) throws JSONException
  {
    JSONObject obj = new JSONObject();
    obj.put(F_NAME, spec.name);
    obj.put(F_BASE, spec.base);
    JSONObject colors = new JSONObject();
    for (Map.Entry<ThemeSpec.Role, Integer> e : spec.colors.entrySet())
      colors.put(e.getKey().attr, ThemeSpec.format_color(e.getValue().intValue()));
    obj.put(F_COLORS, colors);
    if (spec.keyBorderRadiusDp != null)
      obj.put(F_RADIUS, spec.keyBorderRadiusDp.intValue());
    return obj;
  }

  /** Returns [null] when [s] is not a valid theme. */
  public static ThemeSpec from_json(String s)
  {
    try
    {
      return from_object(new JSONObject(s));
    }
    catch (JSONException e)
    {
      Logs.exn("ThemeSpecJson.from_json", e);
      return null;
    }
  }

  public static ThemeSpec from_object(JSONObject obj) throws JSONException
  {
    String name = obj.optString(F_NAME, "");
    String base = obj.optString(F_BASE, "dark");
    Map<ThemeSpec.Role, Integer> colors = new EnumMap<ThemeSpec.Role, Integer>(ThemeSpec.Role.class);
    JSONObject c_obj = obj.optJSONObject(F_COLORS);
    if (c_obj != null)
    {
      for (Iterator<String> i = c_obj.keys(); i.hasNext(); )
      {
        String attr = i.next();
        ThemeSpec.Role role = ThemeSpec.Role.of_attr(attr);
        // Unknown roles are ignored so that a theme saved by a newer version
        // does not break an older app.
        if (role == null)
          continue;
        Integer color = ThemeSpec.parse_color(c_obj.optString(attr, ""));
        if (color != null)
          colors.put(role, color);
      }
    }
    Integer radius = obj.has(F_RADIUS) ? Integer.valueOf(obj.optInt(F_RADIUS)) : null;
    return new ThemeSpec(name, base, colors, radius);
  }
}