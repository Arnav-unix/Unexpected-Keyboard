package juloo.keyboard2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.view.MotionEvent;
import android.view.View;

/** A horizontal opacity slider, drawn over a checkerboard so that a
    transparent colour stays visible. [on_change] runs whenever the user drags
    on it. */
public class ColorAlphaBar extends View
{
  private final Paint _paint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private int _color = 0xFF000000;
  public Runnable on_change = null;

  public ColorAlphaBar(Context ctx)
  {
    super(ctx);
  }

  /** Needed by the layout inflater. */
  public ColorAlphaBar(Context ctx, android.util.AttributeSet attrs)
  {
    super(ctx, attrs);
  }

  /** The opacity currently shown, 0 to 255. */
  public int value() { return (int)((_color >> 24) & 0xFF); }

  public void set_color(int color)
  {
    _color = color;
    invalidate();
  }

  @Override
  protected void onDraw(Canvas c)
  {
    int rgb = _color & 0xFFFFFF;
    Paint p = new Paint();
    int n = 10;
    float w = getWidth() / (float)n, h = getHeight();
    for (int x = 0; x < n; x++)
      for (int y = 0; y < 2; y++)
      {
        p.setColor(((x + y) % 2 == 0) ? 0xFFFFFFFF : 0xFFCCCCCC);
        c.drawRect(x * w, y * h, (x + 1) * w, (y + 1) * h, p);
      }
    _paint.setShader(new LinearGradient(0, 0, getWidth(), 0,
            ((rgb & 0xFFFFFF) | 0x00000000), rgb | 0xFF000000,
            Shader.TileMode.CLAMP));
    c.drawRect(0, 0, getWidth(), getHeight(), _paint);
    Paint s = new Paint(Paint.ANTI_ALIAS_FLAG);
    s.setStyle(Paint.Style.STROKE);
    s.setStrokeWidth(5);
    float x = (_color >> 24) / 255.f * getWidth();
    s.setColor(0xFF000000);
    c.drawRect(x - 9, 0, x + 9, getHeight(), s);
    s.setColor(0xFFFFFFFF);
    c.drawRect(x - 6, 0, x + 6, getHeight(), s);
  }

  @Override
  public boolean onTouchEvent(MotionEvent e)
  {
    if (getWidth() == 0)
      return true;
    int a = Math.round(ColorPickerDialog.clamp(e.getX() / getWidth()) * 255);
    _color = (a << 24) | (_color & 0xFFFFFF);
    invalidate();
    if (on_change != null)
      on_change.run();
    return true;
  }
}