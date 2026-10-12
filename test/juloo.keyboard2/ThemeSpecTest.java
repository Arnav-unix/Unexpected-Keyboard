package juloo.keyboard2;

import java.util.EnumMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/** Tests for [ThemeSpec]. Only the parts that do not need org.json or the
    Android framework are covered here, so that they run as plain unit tests. */
public class ThemeSpecTest
{
  static final int RED = 0xFFFF0000;

  @Test
  public void parse_rgb()
  {
    assertEquals(Integer.valueOf(0xFFAABBCC), ThemeSpec.parse_color("#abc"));
    assertEquals(Integer.valueOf(0xFFAABBCC), ThemeSpec.parse_color("#AABBCC"));
    assertEquals(Integer.valueOf(0xFF112233), ThemeSpec.parse_color("#123"));
    assertEquals(Integer.valueOf(RED), ThemeSpec.parse_color("#f00"));
  }

  @Test
  public void parse_argb()
  {
    // "#aarrggbb": the first two digits are the alpha channel.
    assertEquals(Integer.valueOf(0x12345680), ThemeSpec.parse_color("#12345680"));
    // A short "#argb" doubles every digit.
    assertEquals(Integer.valueOf(0x88112233), ThemeSpec.parse_color("#8123"));
  }

  @Test
  public void parse_rejects_garbage()
  {
    assertNull(ThemeSpec.parse_color(null));
    assertNull(ThemeSpec.parse_color(""));
    assertNull(ThemeSpec.parse_color("#"));
    assertNull(ThemeSpec.parse_color("#12"));
    assertNull(ThemeSpec.parse_color("#12345"));
    assertNull(ThemeSpec.parse_color("#1234567"));
    assertNull(ThemeSpec.parse_color("#123456789"));
    assertNull(ThemeSpec.parse_color("123456"));
    assertNull(ThemeSpec.parse_color("#gggggg"));
    assertNull(ThemeSpec.parse_color("red"));
  }

  @Test
  public void parse_tolerates_spaces()
  {
    assertEquals(Integer.valueOf(0xFFAABBCC), ThemeSpec.parse_color("  #aabbcc "));
  }

  @Test
  public void format_roundtrip()
  {
    assertEquals("#ff0000", ThemeSpec.format_color(0xFFFF0000));
    assertEquals("#11223344", ThemeSpec.format_color(0x11223344));
    for (String c : new String[]{"#abc", "#aabbcc", "#12345680", "#8123"})
      assertEquals(Integer.valueOf(ThemeSpec.parse_color(c)),
          Integer.valueOf(ThemeSpec.parse_color(
              ThemeSpec.format_color(ThemeSpec.parse_color(c)))));
  }

  @Test
  public void role_lookup_matches_attribute_names()
  {
    for (ThemeSpec.Role r : ThemeSpec.Role.values())
      assertSame(r, ThemeSpec.Role.of_attr(r.attr));
    assertNull(ThemeSpec.Role.of_attr("notARole"));
    assertNull(ThemeSpec.Role.of_attr("colorKeyboard2"));
  }

  @Test
  public void template_overrides_nothing()
  {
    ThemeSpec t = ThemeSpec.template("dark", "Mine");
    assertEquals("Mine", t.name);
    assertEquals("dark", t.base);
    assertTrue(t.colors.isEmpty());
    assertNull(t.colors.get(ThemeSpec.Role.colorKey));
    assertNull(t.keyBorderRadiusDp);
  }

  @Test
  public void with_sets_and_removes()
  {
    ThemeSpec t = ThemeSpec.template("dark", "Mine")
      .with(ThemeSpec.Role.colorKey, Integer.valueOf(RED));
    assertEquals(Integer.valueOf(RED), t.colors.get(ThemeSpec.Role.colorKey));
    // Roles that were not overridden stay absent.
    assertNull(t.colors.get(ThemeSpec.Role.colorLabel));
    // Removing brings the base colour back.
    t = t.with(ThemeSpec.Role.colorKey, null);
    assertNull(t.colors.get(ThemeSpec.Role.colorKey));
  }

  @Test
  public void themes_are_immutable()
  {
    ThemeSpec a = ThemeSpec.template("dark", "A");
    ThemeSpec b = a.with(ThemeSpec.Role.colorKey, Integer.valueOf(RED));
    assertNotSame(a, b);
    // [a] must not have been modified.
    assertNull(a.colors.get(ThemeSpec.Role.colorKey));
    // The map handed in must not be aliased.
    Map<ThemeSpec.Role, Integer> src = new EnumMap<ThemeSpec.Role, Integer>(ThemeSpec.Role.class);
    src.put(ThemeSpec.Role.colorKey, Integer.valueOf(RED));
    ThemeSpec c = new ThemeSpec("C", "dark", src, null);
    src.clear();
    assertEquals(Integer.valueOf(RED), c.colors.get(ThemeSpec.Role.colorKey));
  }

  @Test
  public void validate_needs_a_name_and_a_base()
  {
    assertNull(ThemeSpec.template("dark", "Mine").validate());
    assertNotNull(ThemeSpec.template("dark", "").validate());
    assertNotNull(ThemeSpec.template("dark", "  ").validate());
    assertNotNull(ThemeSpec.template("", "Mine").validate());
  }

  @Test
  public void validate_rejects_negative_radius()
  {
    assertNotNull(ThemeSpec.template("dark", "Mine")
      .with_border_radius(Integer.valueOf(-1)).validate());
    assertNull(ThemeSpec.template("dark", "Mine")
      .with_border_radius(Integer.valueOf(0)).validate());
  }

  @Test
  public void light_bases()
  {
    assertTrue(ThemeSpec.is_light_base("light"));
    assertTrue(ThemeSpec.is_light_base("catppuccinlatte"));
    assertFalse(ThemeSpec.is_light_base("dark"));
    assertFalse(ThemeSpec.is_light_base("catppuccinmocha"));
    assertFalse(ThemeSpec.is_light_base("notATheme"));
  }
}