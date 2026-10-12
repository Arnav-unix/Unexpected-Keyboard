package juloo.keyboard2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.view.MotionEvent;
import android.view.View;

/** A horizontal hue slider. [on_change] runs whenever the user drags on it. */
public class ColorHueBar extends View
{
  private final Paint _paint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private float _hue = 0.f;
  public Runnable on_change = null;

  public ColorHueBar(Context ctx)
  {
    super(ctx);
  }

  /** Needed by the layout inflater. */
  public ColorHueBar(Context ctx, android.util.AttributeSet attrs)
  {
    super(ctx, attrs);
  }

  public float hue() { return _hue; }

  public void set_color(int color)
  {
    float[] hsv = new float[3];
    Color.colorToHSV(color, hsv);
    _hue = hsv[0];
    invalidate();
  }

  @Override
  protected void onDraw(Canvas c)
  {
    int[] colors = new int[7];
    for (int i = 0; i < 7; i++)
      colors[i] = Color.HSVToColor(new float[] { i * 60.f, 1.f, 1.f });
    _paint.setShader(new LinearGradient(0, 0, getWidth(), 0,
            colors, null, Shader.TileMode.CLAMP));
    c.drawRect(0, 0, getWidth(), getHeight(), _paint);
    // A marker for the current hue, drawn black then white so that it shows
    // on both ends of the spectrum.
    Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    p.setStyle(Paint.Style.STROKE);
    p.setStrokeWidth(5);
    float x = _hue / 360.f * getWidth();
    p.setColor(0xFF000000);
    c.drawRect(x - 9, 0, x + 9, getHeight(), p);
    p.setColor(0xFFFFFFFF);
    c.drawRect(x - 6, 0, x + 6, getHeight(), p);
  }

  @Override
  public boolean onTouchEvent(MotionEvent e)
  {
    if (getWidth() == 0)
      return true;
    _hue = ColorPickerDialog.clamp(e.getX() / getWidth()) * 360.f;
    invalidate();
    if (on_change != null)
      on_change.run();
    return true;
  }
}