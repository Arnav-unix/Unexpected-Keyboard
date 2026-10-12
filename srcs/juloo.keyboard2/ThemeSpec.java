package juloo.keyboard2;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/** A user-defined theme, as opposed to the built-in themes which are XML
    styles in [res/values/themes.xml].

    A custom theme overrides some of the colours of a built-in theme, called
    its [base]. Anything not overridden is inherited from the base.

    Instances are immutable. [Serialization] to and from JSON is done by
    [ThemeSpecJson] so that this class stays free of the Android framework and
    can be unit tested. */
public final class ThemeSpec
{
  /** A colour that can be overridden by a custom theme. The string names match
      the attribute names used in [res/values/themes.xml], so that themes.xml
      and custom themes read the same way. */
  public enum Role
  {
    colorKeyboard("colorKeyboard"),
    colorKey("colorKey"),
    colorKeyActivated("colorKeyActivated"),
    colorKeyAction("colorKeyAction"),
    colorKeySpaceBar("colorKeySpaceBar"),
    colorLabel("colorLabel"),
    colorLabelActivated("colorLabelActivated"),
    colorLabelPressed("colorLabelPressed"),
    colorLabelLocked("colorLabelLocked"),
    colorSubLabel("colorSubLabel"),
    emojiButtonBg("emoji_button_bg"),
    emojiColor("emoji_color"),
    emojiKeyBg("emoji_key_bg"),
    emojiKeyText("emoji_key_text"),
    clipboardDividerColor("clipboard_divider_color"),
    keyBorderColorLeft("keyBorderColorLeft"),
    keyBorderColorTop("keyBorderColorTop"),
    keyBorderColorRight("keyBorderColorRight"),
    keyBorderColorBottom("keyBorderColorBottom");

    public final String attr;

    Role(String a) { attr = a; }

    /** The role with attribute name [attr], or [null]. */
    public static Role of_attr(String attr)
    {
      for (Role r : values())
        if (r.attr.equals(attr))
          return r;
      return null;
    }
  }

  public final String name;

  /** Name of the built-in theme this one derives from, eg. "dark". */
  public final String base;

  /** Overridden colours. Roles that are absent are inherited from [base]. */
  public final Map<Role, Integer> colors;

  /** Corner radius of the keys in dp, or [null] to inherit from [base]. */
  public final Integer keyBorderRadiusDp;

  public ThemeSpec(String name, String base,
      Map<Role, Integer> colors, Integer keyBorderRadiusDp)
  {
    this.name = name;
    this.base = base;
    EnumMap<Role, Integer> copy = null;
    if (colors != null && !colors.isEmpty())
    {
      copy = new EnumMap<Role, Integer>(Role.class);
      copy.putAll(colors);
    }
    this.colors = (copy == null)
      ? Collections.<Role, Integer>emptyMap()
      : Collections.unmodifiableMap(copy);
    this.keyBorderRadiusDp = keyBorderRadiusDp;
  }

  /** Whether [base_name] is one of the built-in light themes. Mirrors the
    android:isLightTheme item of each style in [res/values/themes.xml], and is
      only used to guess colours for the preview. */
  public static boolean is_light_base(String base_name)
  {
    switch (base_name)
    {
      case "light": case "white": case "epaper": case "desert": case "jungle":
      case "monetlight": case "everforestlight": case "tokyonightday":
      case "catppuccinlatte": case "gruvboxlight":
        return true;
      default:
        return false;
    }
  }

  /** A theme that overrides nothing and is based on the built-in theme
      [base_name]. */
  public static ThemeSpec template(String base_name, String name)
  {
    return new ThemeSpec(name, base_name, null, null);
  }

  /** A copy of this theme with the colour [role] overridden. A [null] colour
      removes the override. */
  public ThemeSpec with(Role role, Integer color)
  {
    // EnumMap.copyOf() rejects an empty source, so build one explicitly.
    EnumMap<Role, Integer> c = new EnumMap<Role, Integer>(Role.class);
    c.putAll(colors);
    if (color == null)
      c.remove(role);
    else
      c.put(role, color);
    return new ThemeSpec(name, base, c, keyBorderRadiusDp);
  }

  public ThemeSpec with_name(String new_name)
  {
    return new ThemeSpec(new_name, base, colors, keyBorderRadiusDp);
  }

  public ThemeSpec with_base(String new_base)
  {
    return new ThemeSpec(name, new_base, colors, keyBorderRadiusDp);
  }

  public ThemeSpec with_border_radius(Integer dp)
  {
    return new ThemeSpec(name, base, colors, dp);
  }

  /** Parse a colour as "#rgb", "#argb", "#rrggbb" or "#aarrggbb". Returns
      [null] when [s] is not a valid colour. */
  public static Integer parse_color(String s)
  {
    if (s == null)
      return null;
    String hex = s.trim();
    if (hex.length() < 4 || hex.charAt(0) != '#')
      return null;
    hex = hex.substring(1);
    for (int i = 0; i < hex.length(); i++)
      if (Character.digit(hex.charAt(i), 16) < 0)
        return null;
    switch (hex.length())
    {
      case 3:
      case 4:
      {
        // "#rgb" or "#argb", each digit is doubled: "#abc" is "#aabbcc".
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < hex.length(); i++)
        {
          b.append(hex.charAt(i));
          b.append(hex.charAt(i));
        }
        hex = b.toString();
        if (hex.length() == 6)
          hex = "ff" + hex;
        return Integer.valueOf((int)Long.parseLong(hex, 16));
      }
      case 6:
        hex = "ff" + hex;
        return Integer.valueOf((int)Long.parseLong(hex, 16));
      case 8:
        return Integer.valueOf((int)Long.parseLong(hex, 16));
      default:
        return null;
    }
  }

  /** Format a colour as "#rrggbb", or "#aarrggbb" when not fully opaque. */
  public static String format_color(int color)
  {
    int a = (color >> 24) & 0xFF;
    if (a == 0xFF)
      return String.format("#%06x", color & 0xFFFFFF);
    return String.format("#%08x", color);
  }

  /** A human readable error, or [null] when the theme is usable. */
  public String validate()
  {
    if (name == null || name.trim().isEmpty())
      return "The theme needs a name";
    if (base == null || base.trim().isEmpty())
      return "The theme needs a base theme";
    if (keyBorderRadiusDp != null && keyBorderRadiusDp.intValue() < 0)
      return "The border radius must not be negative";
    return null;
  }

  @Override
  public String toString()
  {
    return "ThemeSpec(" + name + ", base=" + base + ", " + colors.size() + " overrides)";
  }
}