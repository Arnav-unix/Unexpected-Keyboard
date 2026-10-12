package juloo.keyboard2.prefs;

import android.content.Context;
import android.util.AttributeSet;
import android.preference.Preference;
import android.widget.Toast;
import juloo.keyboard2.R;

/** Copies every custom theme to the clipboard.

    The themes live in the app preferences, which an uninstall destroys, so this
    is the way to keep them across a reinstall. Pair it with [ImportTheme]. */
public class ExportThemes extends Preference
{
  public ExportThemes(Context context, AttributeSet attrs)
  {
    super(context, attrs);
    setPersistent(false);
  }

  @Override
  protected void onClick()
  {
    Context ctx = getContext();
    ThemesPreference.export_all(ctx);
    Toast.makeText(ctx, R.string.pref_themes_exported, Toast.LENGTH_LONG).show();
  }
}