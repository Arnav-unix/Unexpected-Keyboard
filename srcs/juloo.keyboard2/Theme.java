package juloo.keyboard2;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;

public class Theme
{
  // Key colors
  public int colorKey;
  public int colorKeyActivated;
  public int colorKeyAction;
  public int colorKeySpaceBar;

  /** Background behind the keys. Read from the theme so that custom themes
      can override it, as it is not drawn on the keyboard view itself. */
  public int colorKeyboard;

  // Optional keyboard background gradient
  public boolean hasKeyboardGradient;
  public int keyboardGradientStart;
  public int keyboardGradientEnd;

  // Label colors
  public int lockedColor;
  public int activatedColor;
  public int pressedColor;
  public int labelColor;
  public int subLabelColor;
  public int secondaryLabelColor;
  public int greyedLabelColor;

  /** Colours of the emoji pane and the clipboard pane. Those views are
      inflated with theme attributes, they are read here so that custom themes
      can override them too. */
  public int emojiButtonBg;
  public int emojiColor;
  public int emojiKeyBg;
  public int emojiKeyText;
  public int clipboardDividerColor;

  // Key borders
  public float keyBorderRadius;
  public final float keyBorderWidth;
  public final float keyBorderWidthActivated;
  public final float keyBorderWidthAction;
  public final float keyBorderWidthSpaceBar;
  public int keyBorderColorLeft;
  public int keyBorderColorTop;
  public int keyBorderColorRight;
  public int keyBorderColorBottom;

  public int colorNavBar;
  public boolean isLightNavBar;

  /** [secondaryDimming] and [greyedDimming], kept to recompute the derived
      label colours when a custom theme overrides [colorLabel]. */
  private final float _secondaryDimming;
  private final float _greyedDimming;

  public Theme(Context context, AttributeSet attrs)
  {
    this(context, attrs, null);
  }

  /** [spec] is the active custom theme, or [null] to use the colours of the
      XML style only. */
  public Theme(Context context, AttributeSet attrs, ThemeSpec spec)
  {
    getKeyFont(context); // _key_font will be accessed
    TypedArray s = context.getTheme().obtainStyledAttributes(attrs, R.styleable.keyboard, 0, 0);
    hasKeyboardGradient = s.hasValue(R.styleable.keyboard_keyboardGradientStart) && s.hasValue(R.styleable.keyboard_keyboardGradientEnd);
    keyboardGradientStart = s.getColor(R.styleable.keyboard_keyboardGradientStart, 0);
    keyboardGradientEnd = s.getColor(R.styleable.keyboard_keyboardGradientEnd, 0);
    colorKey = s.getColor(R.styleable.keyboard_colorKey, 0);
    colorKeyActivated = s.getColor(R.styleable.keyboard_colorKeyActivated, 0);
    colorKeyAction = s.getColor(R.styleable.keyboard_colorKeyAction, colorKey);
    colorKeySpaceBar = s.getColor(R.styleable.keyboard_colorKeySpaceBar, colorKey);
    colorKeyboard = s.getColor(R.styleable.keyboard_colorKeyboard, 0);
    colorNavBar = s.getColor(R.styleable.keyboard_navigationBarColor, 0);
    isLightNavBar = s.getBoolean(R.styleable.keyboard_windowLightNavigationBar, false);
    labelColor = s.getColor(R.styleable.keyboard_colorLabel, 0);
    activatedColor = s.getColor(R.styleable.keyboard_colorLabelActivated, 0);
    pressedColor = s.getColor(R.styleable.keyboard_colorLabelPressed, labelColor);
    lockedColor = s.getColor(R.styleable.keyboard_colorLabelLocked, 0);
    subLabelColor = s.getColor(R.styleable.keyboard_colorSubLabel, 0);
    _secondaryDimming = s.getFloat(R.styleable.keyboard_secondaryDimming, 0.25f);
    _greyedDimming = s.getFloat(R.styleable.keyboard_greyedDimming, 0.5f);
    secondaryLabelColor = adjustLight(labelColor, _secondaryDimming);
    greyedLabelColor = adjustLight(labelColor, _greyedDimming);
    emojiButtonBg = s.getColor(R.styleable.keyboard_emoji_button_bg, 0);
    emojiColor = s.getColor(R.styleable.keyboard_emoji_color, 0);
    emojiKeyBg = s.getColor(R.styleable.keyboard_emoji_key_bg, emojiButtonBg);
    emojiKeyText = s.getColor(R.styleable.keyboard_emoji_key_text, labelColor);
    clipboardDividerColor = s.getColor(R.styleable.keyboard_clipboard_divider_color, colorKey);
    keyBorderRadius = s.getDimension(R.styleable.keyboard_keyBorderRadius, 0);
    keyBorderWidth = s.getDimension(R.styleable.keyboard_keyBorderWidth, 0);
    keyBorderWidthActivated = s.getDimension(R.styleable.keyboard_keyBorderWidthActivated, 0);
    keyBorderWidthAction = s.getDimension(R.styleable.keyboard_keyBorderWidthAction, 0);
    keyBorderWidthSpaceBar = s.getDimension(R.styleable.keyboard_keyBorderWidthSpaceBar, 0);
    keyBorderColorLeft = s.getColor(R.styleable.keyboard_keyBorderColorLeft, colorKey);
    keyBorderColorTop = s.getColor(R.styleable.keyboard_keyBorderColorTop, colorKey);
    keyBorderColorRight = s.getColor(R.styleable.keyboard_keyBorderColorRight, colorKey);
    keyBorderColorBottom = s.getColor(R.styleable.keyboard_keyBorderColorBottom, colorKey);
    s.recycle();
    if (spec != null)
      apply_spec(context, spec);
  }

  /** Override the colours read from the XML style with the ones from the
      custom theme [spec]. */
  void apply_spec(Context context, ThemeSpec spec)
  {
    for (ThemeSpec.Role role : ThemeSpec.Role.values())
    {
      Integer c = spec.colors.get(role);
      if (c == null)
        continue;
      int color = c.intValue();
      switch (role)
      {
        case colorKeyboard: colorKeyboard = color; break;
        case colorKey: colorKey = color; break;
        case colorKeyActivated: colorKeyActivated = color; break;
        case colorKeyAction: colorKeyAction = color; break;
        case colorKeySpaceBar: colorKeySpaceBar = color; break;
        case colorLabel: labelColor = color; break;
        case colorLabelActivated: activatedColor = color; break;
        case colorLabelPressed: pressedColor = color; break;
        case colorLabelLocked: lockedColor = color; break;
        case colorSubLabel: subLabelColor = color; break;
        case emojiButtonBg: emojiButtonBg = color; break;
        case emojiColor: emojiColor = color; break;
        case emojiKeyBg: emojiKeyBg = color; break;
        case emojiKeyText: emojiKeyText = color; break;
        case clipboardDividerColor: clipboardDividerColor = color; break;
        case keyBorderColorLeft: keyBorderColorLeft = color; break;
        case keyBorderColorTop: keyBorderColorTop = color; break;
        case keyBorderColorRight: keyBorderColorRight = color; break;
        case keyBorderColorBottom: keyBorderColorBottom = color; break;
      }
    }
    if (spec.keyBorderRadiusDp != null)
      keyBorderRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
          spec.keyBorderRadiusDp.intValue(), context.getResources().getDisplayMetrics());
    // The dimmed label colours are derived from [labelColor] and must follow it.
    secondaryLabelColor = adjustLight(labelColor, _secondaryDimming);
    greyedLabelColor = adjustLight(labelColor, _greyedDimming);
    // An overridden label colour must not leave the emoji grid unreadable.
    if (!spec.colors.containsKey(ThemeSpec.Role.emojiKeyText))
      emojiKeyText = labelColor;
    if (!spec.colors.containsKey(ThemeSpec.Role.emojiKeyBg))
      emojiKeyBg = emojiButtonBg;
  }

  /** Interpolate the 'value' component toward its opposite by 'alpha'. */
  static int adjustLight(int color, float alpha)
  {
    float[] hsv = new float[3];
    Color.colorToHSV(color, hsv);
    float v = hsv[2];
    hsv[2] = alpha - (2 * alpha - 1) * v;
    return Color.HSVToColor(hsv);
  }

  Paint initIndicationPaint(Paint.Align align, Typeface font)
  {
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    paint.setTextAlign(align);
    if (font != null)
      paint.setTypeface(font);
    return (paint);
  }

  static Typeface _key_font = null;

  static public Typeface getKeyFont(Context context)
  {
    if (_key_font == null)
      _key_font = Typeface.createFromAsset(context.getAssets(), "special_font.ttf");
    return _key_font;
  }

  public static final class Computed
  {
    public final float vertical_margin;
    public final float horizontal_margin;
    public final float margin_top;
    public final float margin_left;
    public final float row_height;
    public final Paint keyboard_background_paint;
    public final Paint indication_paint;

    public final Key key;
    public final Key key_activated;
    public final Key key_action;
    public final Key key_space_bar;
    public final Key key_suggestion;

    public Computed(Theme theme, Config config, float keyWidth, KeyboardData layout)
    {
      // Make sure that the layout isn't higher than the screen. Take the
      // height of the candidates view into account.
      row_height = Math.min(config.keyboard_rows_height_pixels,
          (config.screenHeightPixels - config.keyboard_rows_height_pixels) / layout.keysHeight);
      vertical_margin = config.key_vertical_margin * row_height;
      horizontal_margin = config.key_horizontal_margin * keyWidth;
      // Add half of the key margin on the left and on the top as it's also
      // added on the right and on the bottom of every keys.
      margin_top = config.marginTop + vertical_margin / 2;
      margin_left = horizontal_margin / 2;
      keyboard_background_paint = init_keyboard_background_paint(theme, margin_top + row_height * layout.keysHeight + config.margin_bottom);
      key = new Key(theme, config, keyWidth, false, KeyboardData.Key.Role.Normal);
      key_action = new Key(theme, config, keyWidth, false, KeyboardData.Key.Role.Action);
      key_space_bar = new Key(theme, config, keyWidth, false, KeyboardData.Key.Role.Space_bar);
      key_activated = new Key(theme, config, keyWidth, true, KeyboardData.Key.Role.Normal);
      key_suggestion = new Key(theme, config, keyWidth, false, KeyboardData.Key.Role.Suggestion);
      indication_paint = init_label_paint(config, null);
      indication_paint.setColor(theme.subLabelColor);
    }

    static Paint init_keyboard_background_paint(Theme theme, float height)
    {
      if (!theme.hasKeyboardGradient)
        return null;

      Paint p = new Paint();
      p.setShader(new LinearGradient(
              0,
              0,
              0,
              height,
              theme.keyboardGradientStart,
              theme.keyboardGradientEnd,
              Shader.TileMode.CLAMP));
      return p;
    }

    public static final class Key
    {
      public final Paint bg_paint = new Paint();
      public final Paint border_left_paint;
      public final Paint border_top_paint;
      public final Paint border_right_paint;
      public final Paint border_bottom_paint;
      public final float border_width;
      public final float border_radius;
      final Paint _label_paint;
      final Paint _special_label_paint;
      final Paint _sublabel_paint;
      final Paint _special_sublabel_paint;
      final int _label_alpha_bits;

      public Key(Theme theme, Config config, float keyWidth, boolean activated,
          KeyboardData.Key.Role role)
      {
        border_radius = config.borderConfig ? config.customBorderRadius * keyWidth : theme.keyBorderRadius;
        if (activated)
        {
          border_width = theme.keyBorderWidthActivated;
          bg_paint.setColor(theme.colorKeyActivated);
          bg_paint.setAlpha(config.keyActivatedOpacity);
        }
        else
        {
          switch (role)
          {
            case Action:
              bg_paint.setColor(theme.colorKeyAction);
              border_width = theme.keyBorderWidthAction;
              break;
            case Space_bar:
              bg_paint.setColor(theme.colorKeySpaceBar);
              border_width = theme.keyBorderWidthSpaceBar;
              break;
            case Suggestion:
              bg_paint.setColor(0);
              border_width = 0;
              break;
            default:
              bg_paint.setColor(theme.colorKey);
              border_width = config.borderConfig ? config.customBorderLineWidth : theme.keyBorderWidth;
              break;
          }
          bg_paint.setAlpha(config.keyOpacity);
        }
        border_left_paint = init_border_paint(config, border_width, theme.keyBorderColorLeft);
        border_top_paint = init_border_paint(config, border_width, theme.keyBorderColorTop);
        border_right_paint = init_border_paint(config, border_width, theme.keyBorderColorRight);
        border_bottom_paint = init_border_paint(config, border_width, theme.keyBorderColorBottom);
        _label_paint = init_label_paint(config, null);
        _special_label_paint = init_label_paint(config, _key_font);
        _sublabel_paint = init_label_paint(config, null);
        _special_sublabel_paint = init_label_paint(config, _key_font);
        _label_alpha_bits = (config.labelBrightness & 0xFF) << 24;
      }

      public Paint label_paint(boolean special_font, int color, float text_size)
      {
        Paint p = special_font ? _special_label_paint : _label_paint;
        p.setColor((color & 0x00FFFFFF) | _label_alpha_bits);
        p.setTextSize(text_size);
        return p;
      }

      public Paint sublabel_paint(boolean special_font, int color, float text_size, Paint.Align align)
      {
        Paint p = special_font ? _special_sublabel_paint : _sublabel_paint;
        p.setColor((color & 0x00FFFFFF) | _label_alpha_bits);
        p.setTextSize(text_size);
        p.setTextAlign(align);
        return p;
      }
    }

    static Paint init_border_paint(Config config, float border_width, int color)
    {
      Paint p = new Paint();
      p.setAlpha(config.keyOpacity);
      p.setStyle(Paint.Style.STROKE);
      p.setStrokeWidth(border_width);
      p.setColor(color);
      return p;
    }

    static Paint init_label_paint(Config config, Typeface font)
    {
      Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
      p.setTextAlign(Paint.Align.CENTER);
      if (font != null)
        p.setTypeface(font);
      return p;
    }
  }
}
