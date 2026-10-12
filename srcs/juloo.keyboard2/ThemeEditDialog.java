package juloo.keyboard2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.InputType;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.widget.ScrollView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Dialog to create or edit a custom theme.

    A custom theme starts from a built-in theme, its *base*, and overrides
    individual colours. The swatch of every colour shows the colour actually in
    use, that is the override if there is one and the colour of the base theme
    otherwise, so that it is possible to see what a theme looks like without
    having to override everything. Tap a swatch to change it, long press it to
    drop the override and go back to the base theme. */
public class ThemeEditDialog
{
  /** Built-in theme names, in the order they appear in the theme picker. */
  static List<String> base_names(Resources res)
  {
    return new ArrayList<String>(Arrays.asList(
        res.getStringArray(R.array.pref_theme_values)));
  }

  /** The colours of a built-in theme, so that the editor can show them.
      Returns [null] when the theme cannot be resolved. */
  static Theme base_theme(Context ctx, String base_name)
  {
    int style = Config.theme_style_id(ctx.getResources(), base_name, null);
    if (style == 0)
      return null;
    // obtainStyledAttributes() with a null AttributeSet resolves the values of
    // the context theme alone, which is exactly what is needed here.
    return new Theme(new ContextThemeWrapper(ctx, style), null, null);
  }

  public static void show(Context ctx, ThemeSpec initial, boolean allow_remove,
      final Callback callback)
  {
    final ThemeSpec[] spec = new ThemeSpec[] { initial };

    LinearLayout layout = new LinearLayout(ctx);
    layout.setOrientation(LinearLayout.VERTICAL);
    int pad = dp(ctx, 14);
    layout.setPadding(pad, pad, pad, pad);

    EditText name = new EditText(ctx);
    name.setHint(R.string.pref_custom_theme_name);
    name.setSingleLine(true);
    name.setInputType(InputType.TYPE_CLASS_TEXT);
    if (initial.name != null)
      name.setText(initial.name);
    layout.addView(name, lp(WRAP, WRAP));

    final Resources res = ctx.getResources();
    final List<String> bases = base_names(res);
    final String[] labels = res.getStringArray(R.array.pref_theme_entries);
    final Spinner base = new Spinner(ctx);
    base.setAdapter(new ArrayAdapter<String>(ctx,
          android.R.layout.simple_spinner_dropdown_item, labels));
    int at = bases.indexOf(initial.base);
    base.setSelection(at < 0 ? 0 : at);
    // The label must wrap its content and the spinner take the rest of the
    // row. A width of 0 with no weight would collapse the spinner to nothing.
    LinearLayout.LayoutParams spinner_params = new LinearLayout.LayoutParams(0, WRAP, 1f);
    layout.addView(labelled(ctx, R.string.pref_custom_theme_base, base,
          lp(WRAP, WRAP), spinner_params));

    final ThemePreviewView preview = new ThemePreviewView(ctx);
    layout.addView(preview, lp(MATCH, dp(ctx, 156)));

    final Runnable redraw_preview = new Runnable()
    {
      public void run() { preview.set_spec(spec[0]); }
    };
    final LinearLayout rows = new LinearLayout(ctx);
    rows.setOrientation(LinearLayout.VERTICAL);
    int section = 0;
    for (final ThemeSpec.Role role : ThemeSpec.Role.values())
    {
      // Without headers the list is an unreadable wall of nineteen rows.
      int want = section_of(role);
      if (want != section)
      {
        section = want;
        rows.addView(section_header(ctx, want));
      }
      rows.addView(color_row(ctx, spec, role, redraw_preview));
    }

    // A dialog does not scroll, so without this the lower half of the roles is
    // unreachable.
    ScrollView scroll = new ScrollView(ctx);
    scroll.addView(rows);
    layout.addView(scroll, new LinearLayout.LayoutParams(MATCH, 0, 1f));

    TextView hint = new TextView(ctx);
    hint.setText(R.string.pref_custom_theme_hint);
    hint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
    hint.setPadding(dp(ctx, 4), dp(ctx, 10), dp(ctx, 4), 0);
    layout.addView(hint);

    final Runnable refresh = new Runnable()
    {
      public void run()
      {
        spec[0] = spec[0].with_base(bases.get(base.getSelectedItemPosition()));
        // The preview needs the colours of the new base, so rebuild it.
        preview.set_base(base_theme(preview.getContext(), spec[0].base));
        preview.set_spec(spec[0]);
        // Swatches of colours that are not overridden follow the base. The
        // section headers are TextViews, so they must be skipped.
        for (int i = 0; i < rows.getChildCount(); i++)
        {
          View child = rows.getChildAt(i);
          if (child instanceof ColorRow)
            ((ColorRow)child).refresh();
        }
      }
    };
    base.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
    {
      public void onItemSelected(AdapterView<?> p, View v, int pos, long id)
      { refresh.run(); }
      public void onNothingSelected(AdapterView<?> p) {}
    });
    preview.set_base(base_theme(ctx, spec[0].base));
    refresh.run();

    AlertDialog.Builder b = new AlertDialog.Builder(ctx)
      .setTitle(R.string.pref_custom_theme_title)
      .setView(layout)
      .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
      {
        public void onClick(DialogInterface _dialog, int _which)
        {
          ThemeSpec s = spec[0].with_name(name.getText().toString().trim());
          if (s.validate() != null)
          {
            Logs.exn("ThemeEditDialog", new Exception(s.validate()));
            callback.select(null);
            return;
          }
          callback.select(s);
        }
      })
      .setNegativeButton(android.R.string.cancel, null);
    if (allow_remove)
      b.setNeutralButton(R.string.pref_themes_remove,
          new DialogInterface.OnClickListener()
          {
            public void onClick(DialogInterface _dialog, int _which)
            {
              callback.select(null);
            }
          });
    b.show();
  }

  static final int MATCH = -1;
  static final int WRAP = -2;

  static LinearLayout.LayoutParams lp(int w, int h)
  {
    return new LinearLayout.LayoutParams(w, h);
  }

  static int dp(Context ctx, int dp)
  {
    return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
        ctx.getResources().getDisplayMetrics());
  }

  static View labelled(Context ctx, int label_res, View content,
      LinearLayout.LayoutParams label_params,
      LinearLayout.LayoutParams content_params)
  {
    LinearLayout row = new LinearLayout(ctx);
    row.setOrientation(LinearLayout.HORIZONTAL);
    row.setGravity(Gravity.CENTER_VERTICAL);
    row.setPadding(0, dp(ctx, 4), 0, dp(ctx, 4));
    TextView label = new TextView(ctx);
    label.setText(label_res);
    row.addView(label, label_params);
    row.addView(content, content_params);
    return row;
  }

  /** One colour of a theme: a name, the colour in use and the swatch that
      changes it. */
  static class ColorRow extends LinearLayout
  {
    final ThemeSpec[] _spec;
    final ThemeSpec.Role _role;
    final View _swatch;
    final TextView _label;
    /** Called after an override was added or removed, to redraw the preview. */
    final Runnable _on_changed;

    ColorRow(Context ctx, ThemeSpec[] spec, ThemeSpec.Role role,
        Runnable on_changed)
    {
      super(ctx);
      _spec = spec;
      _role = role;
      _on_changed = on_changed;
      setOrientation(HORIZONTAL);
      setGravity(Gravity.CENTER_VERTICAL);
      setPadding(0, dp(ctx, 3), 0, dp(ctx, 3));

      _label = new TextView(ctx);
      addView(_label, new LayoutParams(0, WRAP, 1f));

      _swatch = new View(ctx);
      addView(_swatch, new LayoutParams(dp(ctx, 56), dp(ctx, 30)));
    }

    /** The colour shown in the swatch: the override, or the base theme's. */
    int current(Context ctx)
    {
      Integer override = _spec[0].colors.get(_role);
      if (override != null)
        return override.intValue();
      Theme base = base_theme(ctx, _spec[0].base);
      if (base == null)
        return 0xFF888888;
      switch (_role)
      {
        case colorKeyboard: return base.colorKeyboard;
        case colorKey: return base.colorKey;
        case colorKeyActivated: return base.colorKeyActivated;
        case colorKeyAction: return base.colorKeyAction;
        case colorKeySpaceBar: return base.colorKeySpaceBar;
        case colorLabel: return base.labelColor;
        case colorLabelActivated: return base.activatedColor;
        case colorLabelPressed: return base.pressedColor;
        case colorLabelLocked: return base.lockedColor;
        case colorSubLabel: return base.subLabelColor;
        case emojiButtonBg: return base.emojiButtonBg;
        case emojiColor: return base.emojiColor;
        case emojiKeyBg: return base.emojiKeyBg;
        case emojiKeyText: return base.emojiKeyText;
        case clipboardDividerColor: return base.clipboardDividerColor;
        case keyBorderColorLeft: return base.keyBorderColorLeft;
        case keyBorderColorTop: return base.keyBorderColorTop;
        case keyBorderColorRight: return base.keyBorderColorRight;
        case keyBorderColorBottom: return base.keyBorderColorBottom;
        default: return 0xFF888888;
      }
    }

    void refresh()
    {
      Context ctx = getContext();
      int color = current(ctx);
      boolean overridden = _spec[0].colors.containsKey(_role);
      _swatch.setBackgroundColor(color);
      _label.setText(label_of(ctx, _role, overridden));
      _label.setAlpha(overridden ? 1f : 0.6f);
      _swatch.setOnClickListener(new OnClickListener()
      {
        public void onClick(View v)
        {
          ColorPickerDialog.show(v.getContext(),
              label_of(v.getContext(), _role, true), current(v.getContext()),
              new ColorPickerDialog.Callback()
              {
                public void select(int picked)
                {
                  _spec[0] = _spec[0].with(_role, Integer.valueOf(picked));
                  refresh();
                  _on_changed.run();
                }
              });
        }
      });
      _swatch.setOnLongClickListener(new OnLongClickListener()
      {
        public boolean onLongClick(View v)
        {
          // Drop the override and go back to the base theme colour.
          _spec[0] = _spec[0].with(_role, null);
          refresh();
          _on_changed.run();
          return true;
        }
      });
    }

    /** The colour name, with a marker when it still comes from the base. */
    static String label_of(Context ctx, ThemeSpec.Role role, boolean overridden)
    {
      int res = label_res(role);
      return (res == 0) ? role.attr : ctx.getString(res);
    }

    static int label_res(ThemeSpec.Role role)
    {
      switch (role)
      {
        case colorKeyboard: return R.string.theme_role_colorKeyboard;
        case colorKey: return R.string.theme_role_colorKey;
        case colorKeyActivated: return R.string.theme_role_colorKeyActivated;
        case colorKeyAction: return R.string.theme_role_colorKeyAction;
        case colorKeySpaceBar: return R.string.theme_role_colorKeySpaceBar;
        case colorLabel: return R.string.theme_role_colorLabel;
        case colorLabelActivated: return R.string.theme_role_colorLabelActivated;
        case colorLabelPressed: return R.string.theme_role_colorLabelPressed;
        case colorLabelLocked: return R.string.theme_role_colorLabelLocked;
        case colorSubLabel: return R.string.theme_role_colorSubLabel;
        case emojiButtonBg: return R.string.theme_role_emojiButtonBg;
        case emojiColor: return R.string.theme_role_emojiColor;
        case emojiKeyBg: return R.string.theme_role_emojiKeyBg;
        case emojiKeyText: return R.string.theme_role_emojiKeyText;
        case clipboardDividerColor: return R.string.theme_role_clipboardDividerColor;
        case keyBorderColorLeft: return R.string.theme_role_keyBorderLeft;
        case keyBorderColorTop: return R.string.theme_role_keyBorderTop;
        case keyBorderColorRight: return R.string.theme_role_keyBorderRight;
        case keyBorderColorBottom: return R.string.theme_role_keyBorderBottom;
        default: return 0;
      }
    }
  }

  static View color_row(Context ctx, ThemeSpec[] spec, ThemeSpec.Role role,
      Runnable on_changed)
  {
    ColorRow row = new ColorRow(ctx, spec, role, on_changed);
    row.refresh();
    return row;
  }

  /** The header that [role] is listed under. */
  static int section_of(ThemeSpec.Role role)
  {
    switch (role)
    {
      case colorLabel: case colorLabelActivated: case colorLabelPressed:
      case colorLabelLocked: case colorSubLabel:
        return R.string.theme_section_labels;
      case emojiButtonBg: case emojiColor: case emojiKeyBg: case emojiKeyText:
        return R.string.theme_section_emoji;
      case clipboardDividerColor:
        return R.string.theme_section_clipboard;
      case keyBorderColorLeft: case keyBorderColorTop:
      case keyBorderColorRight: case keyBorderColorBottom:
        return R.string.theme_section_borders;
      default:
        return R.string.theme_section_keys;
    }
  }

  static View section_header(Context ctx, int res)
  {
    TextView t = new TextView(ctx);
    t.setText(res);
    t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
    t.setTypeface(Typeface.DEFAULT_BOLD);
    t.setPadding(dp(ctx, 4), dp(ctx, 12), dp(ctx, 4), dp(ctx, 4));
    return t;
  }

  public interface Callback
  {
    /** [spec] when the user confirms, [null] when the theme is removed or
        invalid. */
    public void select(ThemeSpec spec);
  }

  /** A small drawing of a keyboard, used to preview a theme while editing. */
  static class ThemePreviewView extends View
  {
    final Paint _paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    ThemeSpec _spec;
    /** The colours of the base theme, when it could be resolved. */
    Theme _base;
    float _radius = 6;

    public ThemePreviewView(Context ctx) { super(ctx); }

    void set_spec(ThemeSpec s)
    {
      _spec = s;
      invalidate();
    }

    void set_base(Theme b)
    {
      _base = b;
      invalidate();
    }

    /** The colour of [role], the override if any, the base theme otherwise,
        and a neutral grey when neither is known. */
    int color(ThemeSpec.Role role, int fallback)
    {
      if (_spec != null)
      {
        Integer c = _spec.colors.get(role);
        if (c != null)
          return c.intValue();
      }
      if (_base != null)
      {
        switch (role)
        {
          case colorKeyboard: return _base.colorKeyboard;
          case colorKey: return _base.colorKey;
          case colorKeyActivated: return _base.colorKeyActivated;
          case colorKeyAction: return _base.colorKeyAction;
          case colorKeySpaceBar: return _base.colorKeySpaceBar;
          case colorLabel: return _base.labelColor;
          case colorLabelPressed: return _base.pressedColor;
          case colorSubLabel: return _base.subLabelColor;
          default: break;
        }
      }
      return fallback;
    }

    /** One key of the preview: its label, its width in key units, and whether
        it is drawn as an action key. */
    static class KeyDef
    {
      final String text;
      final float units;
      final boolean action;
      KeyDef(String text, float units) { this(text, units, false); }
      KeyDef(String text, float units, boolean action)
      { this.text = text; this.units = units; this.action = action; }
    }

    /** The keys of row [r]. Every row is ten units wide, so the rows line up
        whatever the number of keys they hold. */
    static KeyDef[] key_row(int r)
    {
      switch (r)
      {
        case 0:
          return new KeyDef[] {
            new KeyDef("q", 1), new KeyDef("w", 1), new KeyDef("e", 1),
            new KeyDef("r", 1), new KeyDef("t", 1), new KeyDef("y", 1),
            new KeyDef("u", 1), new KeyDef("i", 1), new KeyDef("o", 1),
            new KeyDef("p", 1) };
        case 1:
          return new KeyDef[] {
            new KeyDef("a", 1), new KeyDef("s", 1), new KeyDef("d", 1),
            new KeyDef("f", 1), new KeyDef("g", 1), new KeyDef("h", 1),
            new KeyDef("j", 1), new KeyDef("k", 1), new KeyDef("l", 1) };
        case 2:
          return new KeyDef[] {
            new KeyDef("shift", 1.5f, true),
            new KeyDef("z", 1), new KeyDef("x", 1), new KeyDef("c", 1),
            new KeyDef("v", 1), new KeyDef("b", 1), new KeyDef("n", 1),
            new KeyDef("m", 1), new KeyDef("del", 1.5f, true) };
        default:
          return new KeyDef[] {
            new KeyDef("?123", 2, true), new KeyDef(",", 1),
            new KeyDef("space", 4), new KeyDef(".", 1),
            new KeyDef("return", 2, true) };
      }
    }

    /** The baseline that centres the current text size between [top] and
        [bottom]. */
    float baseline(float top, float bottom, float text_size)
    {
      _paint.setTextSize(text_size);
      Paint.FontMetrics fm = _paint.getFontMetrics();
      return (top + bottom) / 2 - (fm.ascent + fm.descent) / 2;
    }

    @Override
    protected void onDraw(Canvas c)
    {
      if (_spec == null)
        return;
      int bg = color(ThemeSpec.Role.colorKeyboard, 0xFF1B1B1B);
      int key = color(ThemeSpec.Role.colorKey, 0xFF333333);
      int action = color(ThemeSpec.Role.colorKeyAction, key);
      int space_key = color(ThemeSpec.Role.colorKeySpaceBar, key);
      int label = color(ThemeSpec.Role.colorLabel, 0xFFFFFFFF);
      int sub = color(ThemeSpec.Role.colorSubLabel, 0xFFCCCCCC);
      int down_key = color(ThemeSpec.Role.colorKeyActivated, key);
      int down_label = color(ThemeSpec.Role.colorLabelPressed, label);
      _radius = (_spec.keyBorderRadiusDp == null) ? dp_h(6)
        : dp_h(Math.min(_spec.keyBorderRadiusDp.intValue(), 14));

      c.drawColor(bg);

      float gap = dp_h(3);
      float bar = dp_h(18);
      // Ten keys plus the gaps around them fill the width.
      float unit = (getWidth() - gap * 10) / 10.f;

      // Candidates bar.
      _paint.setColor(label);
      _paint.setTextAlign(Paint.Align.LEFT);
      c.drawText("the quick brown fox", dp_h(6),
          baseline(0, bar, bar * 0.48f), _paint);
      _paint.setColor(key);
      c.drawRect(0, bar, getWidth(), bar + Math.max(1f, dp_h(1) * 0.4f), _paint);

      float h = (getHeight() - bar - gap) / 4.f;
      for (int r = 0; r < 4; r++)
      {
        KeyDef[] keys = key_row(r);
        // The second row is inset by half a key, as on a real keyboard.
        float x = (r == 1) ? gap + unit * 0.5f : gap;
        float y = bar + gap + r * h;
        for (int i = 0; i < keys.length; i++)
        {
          KeyDef k = keys[i];
          float kw = k.units * unit;
          // One key is drawn held down, to show what a touch looks like.
          boolean down = (r == 0 && i == 4);
          _paint.setColor(down ? down_key
              : (k.units >= 4 ? space_key : (k.action ? action : key)));
          c.drawRoundRect(x, y, x + kw, y + h - gap, _radius, _radius, _paint);
          _paint.setColor(down ? down_label : label);
          _paint.setTextAlign(Paint.Align.CENTER);
          float ts = h * (k.text.length() > 1 ? 0.24f : 0.36f);
          c.drawText(k.text, x + kw / 2, baseline(y, y + h - gap, ts), _paint);
          if (r == 0)
          {
            _paint.setColor(sub);
            _paint.setTextSize(h * 0.2f);
            _paint.setTextAlign(Paint.Align.RIGHT);
            c.drawText(String.valueOf(i + 1), x + kw - dp_h(3),
                y + dp_h(1) + h * 0.2f, _paint);
          }
          x += kw + gap;
        }
      }
    }

    float dp_h(int dp)
    {
      return dp * getResources().getDisplayMetrics().density;
    }
  }
}