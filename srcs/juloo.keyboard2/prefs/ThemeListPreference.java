package juloo.keyboard2.prefs;

import android.content.Context;
import android.content.res.Resources;
import android.preference.ListPreference;
import android.util.AttributeSet;
import java.util.ArrayList;
import java.util.List;
import juloo.keyboard2.R;
import juloo.keyboard2.ThemeSpec;

/** The theme picker, listing the built-in themes followed by the custom ones.

    The custom themes are read from the "themes" preference at the time the
    picker is shown, so that a theme saved in the list below appears here right
    away. */
public class ThemeListPreference extends ListPreference
{
  static final String KEY = "theme";

  public ThemeListPreference(Context context, AttributeSet attrs)
  {
    super(context, attrs);
  }

  /** The custom themes currently saved, in order. */
  static List<ThemesPreference.CustomTheme> custom_themes(Context ctx)
  {
    List<ThemesPreference.CustomTheme> out =
      new ArrayList<ThemesPreference.CustomTheme>();
    for (ThemesPreference.Theme t : ThemesPreference.load_all(ctx))
      if (t instanceof ThemesPreference.CustomTheme)
        out.add((ThemesPreference.CustomTheme)t);
    return out;
  }

  /** Append the custom themes to the built-in ones. */
  void refresh()
  {
    Resources res = getContext().getResources();
    List<CharSequence> values = new ArrayList<CharSequence>();
    for (CharSequence c : res.getStringArray(R.array.pref_theme_values))
      values.add(c);
    List<CharSequence> labels = new ArrayList<CharSequence>();
    for (CharSequence c : res.getStringArray(R.array.pref_theme_entries))
      labels.add(c);

    for (ThemesPreference.CustomTheme t : custom_themes(getContext()))
    {
      String name = t.spec.name;
      if (name == null || name.isEmpty())
        continue;
      values.add(ThemesPreference.CUSTOM_PREFIX + name);
      labels.add(name);
    }

    // A theme that was removed while selected must not stay selected.
    String current = getValue();
    if (current != null && !values.contains(current))
      setValue(null);

    setEntries(labels.toArray(new CharSequence[labels.size()]));
    setEntryValues(values.toArray(new CharSequence[values.size()]));
  }

  @Override
  public void setValue(String value)
  {
    super.setValue(value);
    refresh();
  }
}