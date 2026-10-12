package juloo.keyboard2;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

/** Applies a custom theme to the views that are inflated from XML.

    The keys of the keyboard are drawn on the canvas by [Keyboard2View] and
    follow [Theme] already. The candidates bar, the emoji pane and the
    clipboard pane instead resolve their colours from theme attributes when
    they are inflated, so they would keep the colours of the base theme.

    A role that is not overridden needs no change: the view already shows the
    colour of the base theme. That is why this only needs the [ThemeSpec] and
    not a fully resolved [Theme].

    Every method is a no-op when [spec] is [null], that is, when a built-in
    theme is used. Built-in themes are therefore completely unaffected. */
public final class ThemeApplier
{
  private ThemeApplier() {}

  /** Set the text colour of [v] if the custom theme overrides [role]. */
  public static void text(View v, ThemeSpec spec, ThemeSpec.Role role)
  {
    if (spec == null)
      return;
    Integer c = spec.colors.get(role);
    if (c != null && v instanceof TextView)
      ((TextView)v).setTextColor(c.intValue());
  }

  /** Tint the background of [v] if the custom theme overrides [role]. */
  public static void background(View v, ThemeSpec spec, ThemeSpec.Role role)
  {
    if (spec == null)
      return;
    Integer c = spec.colors.get(role);
    if (c == null)
      return;
    Drawable bg = v.getBackground();
    if (bg == null)
    {
      v.setBackgroundColor(c.intValue());
      return;
    }
    // Mutate first, the drawable might be shared with other views.
    bg = bg.mutate();
    bg.setTint(c.intValue());
    v.setBackground(bg);
  }
}