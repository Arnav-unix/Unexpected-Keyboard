package juloo.keyboard2.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import juloo.keyboard2.*;
import org.json.JSONException;
import org.json.JSONObject;

/** The list of custom themes. The built-in themes are selected with the
    "theme" preference above, so they are not listed again here.

    A custom theme is stored as a JSON object. The theme that is actually used
    is the value of the separate "theme" preference, which is either a
    built-in name or [CUSTOM_PREFIX] followed by the name of a custom theme. */
public class ThemesPreference extends ListGroupPreference<ThemesPreference.Theme>
{
  static final String KEY = "themes";
  /** Prefix of the value of the "theme" preference for custom themes. */
  public static final String CUSTOM_PREFIX = "custom:";

  static final List<Theme> DEFAULT = Collections.emptyList();

  /** The instance currently on screen, so that an import can redraw it. */
  private static ThemesPreference _attached = null;
  static final ListGroupPreference.Serializer<Theme> SERIALIZER = new Serializer();

  public ThemesPreference(Context ctx, AttributeSet attrs)
  {
    super(ctx, attrs);
    setKey(KEY);
    _attached = this;
  }

  /** Re-read the stored themes and redraw, after an import. */
  static void refresh_attached()
  {
    if (_attached != null)
      _attached.set_values(new ArrayList<Theme>(load_all(_attached.getContext())), false);
  }

  /** Every custom theme that was saved, in order. */
  public static List<Theme> load_all(Context ctx)
  {
    SharedPreferences prefs = android.preference.PreferenceManager
      .getDefaultSharedPreferences(ctx);
    return load_from_preferences(KEY, prefs, DEFAULT, SERIALIZER);
  }

  /** The custom theme selected by the "theme" preference, or [null] when a
      built-in theme is used. */
  public static ThemeSpec custom_theme_of(SharedPreferences prefs, String theme_name)
  {
    if (theme_name == null || !theme_name.startsWith(CUSTOM_PREFIX))
      return null;
    String name = theme_name.substring(CUSTOM_PREFIX.length());
    for (Theme t : load_from_preferences(KEY, prefs, DEFAULT, SERIALIZER))
      if (t instanceof CustomTheme)
      {
        CustomTheme ct = (CustomTheme)t;
        if (name.equals(ct.spec.name))
          return ct.spec;
      }
    return null;
  }

  /** Copy every custom theme to the clipboard.

      Custom themes live in the app preferences, which an uninstall destroys.
      Exporting before reinstalling and importing after is the way to keep
      them. */
  static void export_all(Context ctx)
  {
    String s = ListGroupPreference.save_to_string(load_all(ctx), SERIALIZER);
    android.content.ClipboardManager cm =
      (android.content.ClipboardManager)ctx.getSystemService(
          Context.CLIPBOARD_SERVICE);
    cm.setPrimaryClip(android.content.ClipData.newPlainText(
        ctx.getString(R.string.pref_themes_clip_label), s));
  }

  /** Add the custom themes found on the clipboard. Returns how many were
      added, 0 when the clipboard does not hold a theme. */
  static int import_all(Context ctx)
  {
    android.content.ClipboardManager cm =
      (android.content.ClipboardManager)ctx.getSystemService(
          Context.CLIPBOARD_SERVICE);
    if (cm == null || !cm.hasPrimaryClip())
      return 0;
    CharSequence text = cm.getPrimaryClip().getItemAt(0).coerceToText(ctx);
    List<Theme> imported =
      ListGroupPreference.load_from_string(text.toString(), SERIALIZER);
    if (imported == null)
      return 0;
    List<Theme> values = new ArrayList<Theme>(load_all(ctx));
    int added = 0;
    for (Theme t : imported)
    {
      if (!(t instanceof CustomTheme))
        continue;
      // Do not add a theme that is already there.
      boolean known = false;
      for (Theme v : values)
        if (v instanceof CustomTheme
            && ((CustomTheme)v).spec.name.equals(((CustomTheme)t).spec.name))
          known = true;
      if (!known)
      {
        values.add(t);
        added++;
      }
    }
    if (added > 0)
      persist_values(ctx, values);
    return added;
  }

  /** Write [values] back to the preference. */
  static void persist_values(Context ctx, List<Theme> values)
  {
    SharedPreferences prefs = android.preference.PreferenceManager
      .getDefaultSharedPreferences(ctx);
    prefs.edit().putString(KEY,
        ListGroupPreference.save_to_string(values, SERIALIZER)).apply();
  }

  /** Use the custom theme [spec]. Passing [null] goes back to the system
      theme. */
  static void activate(Context ctx, ThemeSpec spec)
  {
    SharedPreferences prefs = android.preference.PreferenceManager
      .getDefaultSharedPreferences(ctx);
    prefs.edit().putString("theme",
        spec == null ? "system" : CUSTOM_PREFIX + spec.name).apply();
  }

  @Override
  protected void onSetInitialValue(boolean restoreValue, Object defaultValue)
  {
    super.onSetInitialValue(restoreValue, defaultValue);
    if (_values == null)
      set_values(new ArrayList<Theme>(), false);
  }

  @Override
  String label_of_value(Theme value, int i)
  {
    return label_of_theme(value);
  }

  String label_of_theme(Theme t)
  {
    if (!(t instanceof CustomTheme))
      return getContext().getString(R.string.pref_theme_e_custom);
    String name = ((CustomTheme)t).spec.name;
    return (name == null || name.isEmpty())
      ? getContext().getString(R.string.pref_theme_e_custom) : name;
  }

  @Override
  AddButton on_attach_add_button(AddButton prev_btn)
  {
    if (prev_btn == null)
      return new ThemesAddButton(getContext());
    return prev_btn;
  }

  @Override
  ListGroupPreference.Serializer<Theme> get_serializer() { return SERIALIZER; }

  /** Edit an existing custom theme, or create a new one when [prev_theme] is
      [null]. */
  @Override
  void select(final SelectionCallback callback, Theme prev_theme)
  {
    ThemeSpec spec = (prev_theme instanceof CustomTheme)
      ? ((CustomTheme)prev_theme).spec
      : ThemeSpec.template("dark", "");
    ThemeEditDialog.show(getContext(), spec,
        callback.allow_remove() && _values.size() > 1,
        new ThemeEditDialog.Callback()
        {
          public void select(ThemeSpec edited)
          {
            if (edited == null)
            {
              // Removed, fall back to a built-in theme.
              activate(getContext(), null);
              callback.select(null);
              return;
            }
            // Saving a theme makes it the active one, otherwise there would be
            // no obvious way to choose between several custom themes.
            activate(getContext(), edited);
            callback.select(new CustomTheme(edited));
          }
        });
  }

  class ThemesAddButton extends AddButton
  {
    public ThemesAddButton(Context ctx)
    {
      super(ctx);
      setLayoutResource(R.layout.pref_themes_add_btn);
    }
  }

  /** A custom theme. */
  public interface Theme {}

  /** A user-defined theme. */
  public static final class CustomTheme implements Theme
  {
    public final ThemeSpec spec;
    public CustomTheme(ThemeSpec s) { spec = s; }
  }

  /** Custom themes are saved as JSON objects. Strings are accepted when
      reading, for forward and backward compatibility. */
  public static class Serializer implements ListGroupPreference.Serializer<Theme>
  {
    public Theme load_item(Object obj) throws JSONException
    {
      if (!(obj instanceof JSONObject))
        return null;
      JSONObject obj_ = (JSONObject)obj;
      if ("custom".equals(obj_.optString("kind")))
      {
        ThemeSpec spec = ThemeSpecJson.from_object(obj_.optJSONObject("spec"));
        if (spec != null && spec.validate() == null)
          return new CustomTheme(spec);
      }
      return null;
    }

    public Object save_item(Theme v) throws JSONException
    {
      if (v instanceof CustomTheme)
        return new JSONObject().put("kind", "custom")
          .put("spec", ThemeSpecJson.to_json(((CustomTheme)v).spec));
      return new JSONObject().put("kind", "custom").put("spec", new JSONObject());
    }
  }
}