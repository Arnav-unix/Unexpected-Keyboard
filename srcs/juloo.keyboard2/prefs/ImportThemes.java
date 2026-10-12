package juloo.keyboard2.prefs;

import android.content.Context;
import android.util.AttributeSet;
import android.preference.Preference;
import android.widget.Toast;
import juloo.keyboard2.R;

/** Adds the custom themes found on the clipboard to the list.

    Pairs with [ExportThemes] to carry the themes across a reinstall. */
public class ImportThemes extends Preference
{
  public ImportThemes(Context context, AttributeSet attrs)
  {
    super(context, attrs);
    setPersistent(false);
  }

  @Override
  protected void onClick()
  {
    Context ctx = getContext();
    int added = ThemesPreference.import_all(ctx);
    int res = (added == 0)
      ? R.string.pref_themes_import_nothing : R.string.pref_themes_imported;
    Toast.makeText(ctx, ctx.getString(res, added), Toast.LENGTH_LONG).show();
    if (added > 0)
      // The list on screen is a view of the same preference, redraw it.
      ThemesPreference.refresh_attached();
  }
}