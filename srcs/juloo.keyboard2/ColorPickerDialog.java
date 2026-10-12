package juloo.keyboard2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

/** A colour picker for [ThemeEditDialog]: a hexadecimal field, a big colour
    preview, a hue bar, an opacity bar and a saturation/value square.

    The views are declared in [res/layout/dialog_color_picker.xml]. The hex
    field sets its text colour explicitly, it used to inherit a colour from the
    dialog theme that made it invisible.

    Hand written on purpose: the framework has no colour picker dialog and
    pulling in a library for this would be a heavy dependency. */
public class ColorPickerDialog
{
  public static void show(Context ctx, String title, int initial_color,
      final Callback callback)
  {
    final int[] color = new int[] { initial_color };

    View view = View.inflate(ctx, R.layout.dialog_color_picker, null);
    final View preview = view.findViewById(R.id.colour_preview);
    final EditText hex = view.findViewById(R.id.hex_field);
    final ColorHueBar hue = view.findViewById(R.id.hue_bar);
    final ColorAlphaBar alpha = view.findViewById(R.id.alpha_bar);
    final ColorSVSquare sv = view.findViewById(R.id.sv_square);

    final Runnable sync_from_color = new Runnable()
    {
      public void run()
      {
        sv.set_color(color[0]);
        hue.set_color(color[0]);
        alpha.set_color(color[0]);
        preview.setBackgroundColor(color[0]);
        String text = ThemeSpec.format_color(color[0]);
        if (!text.equals(hex.getText().toString()))
          hex.setText(text);
      }
    };
    final Runnable set_color = new Runnable()
    {
      public void run()
      {
        float[] hsv = new float[3];
        Color.colorToHSV(color[0], hsv);
        hsv[0] = hue.hue();
        hsv[1] = sv.sat();
        hsv[2] = sv.val();
        int rgb = Color.HSVToColor(hsv);
        // The alpha has to be read back from the opacity bar: keeping the one
        // already in [color] threw away whatever the user had just picked.
        color[0] = (alpha.value() << 24) | (rgb & 0xFFFFFF);
        sync_from_color.run();
      }
    };
    sv.on_change = set_color;
    hue.on_change = set_color;
    alpha.on_change = set_color;
    hex.addTextChangedListener(new TextWatcher()
    {
      public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
      public void onTextChanged(CharSequence s, int a, int b, int c) {}
      public void afterTextChanged(Editable s)
      {
        Integer parsed = ThemeSpec.parse_color(s.toString());
        if (parsed != null && parsed.intValue() != color[0])
        {
          color[0] = parsed.intValue();
          sync_from_color.run();
        }
      }
    });

    new AlertDialog.Builder(ctx)
      .setTitle(title)
      .setView(view)
      .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
      {
        public void onClick(DialogInterface d, int w) { callback.select(color[0]); }
      })
      .setNegativeButton(android.R.string.cancel, null)
      .show();
    sync_from_color.run();
  }

  public interface Callback
  {
    /** [color] is the picked colour, opaque or not. */
    public void select(int color);
  }

  static float clamp(float v) { return Math.max(0.f, Math.min(1.f, v)); }
}