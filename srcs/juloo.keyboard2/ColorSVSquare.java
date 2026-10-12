package juloo.keyboard2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.view.MotionEvent;
import android.view.View;

/** A saturation and value square for the hue selected in [ColorHueBar].
    [on_change] runs whenever the user drags on it. */
public class ColorSVSquare extends View
{
  private final Paint _paint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private float _sat = 1.f, _val = 1.f, _hue = 0.f;
  public Runnable on_change = null;

  public ColorSVSquare(Context ctx)
  {
    super(ctx);
  }

  /** Needed by the layout inflater. */
  public ColorSVSquare(Context ctx, android.util.AttributeSet attrs)
  {
    super(ctx, attrs);
  }

  public float sat() { return _sat; }
  public float val() { return _val; }

  public void set_color(int color)
  {
    float[] hsv = new float[3];
    Color.colorToHSV(color, hsv);
    _hue = hsv[0];
    _sat = hsv[1];
    _val = hsv[2];
    invalidate();
  }

  @Override
  protected void onDraw(Canvas c)
  {
    int base = Color.HSVToColor(new float[] { _hue, 1.f, 1.f });
    // White to the hue, then a transparent to black gradient over it.
    _paint.setShader(new LinearGradient(0, 0, getWidth(), 0,
            Color.WHITE, base, Shader.TileMode.CLAMP));
    c.drawRect(0, 0, getWidth(), getHeight(), _paint);
    _paint.setShader(new LinearGradient(0, 0, 0, getHeight(),
            Color.TRANSPARENT, Color.BLACK, Shader.TileMode.CLAMP));
    c.drawRect(0, 0, getWidth(), getHeight(), _paint);
    // A ring marks the current position.
    Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    p.setStyle(Paint.Style.STROKE);
    p.setStrokeWidth(5);
    float x = _sat * getWidth();
    float y = (1 - _val) * getHeight();
    p.setColor(0xFF000000);
    c.drawCircle(x, y, 14, p);
    p.setColor(0xFFFFFFFF);
    c.drawCircle(x, y, 11, p);
  }

  @Override
  public boolean onTouchEvent(MotionEvent e)
  {
    if (getWidth() == 0 || getHeight() == 0)
      return true;
    _sat = ColorPickerDialog.clamp(e.getX() / getWidth());
    _val = 1 - ColorPickerDialog.clamp(e.getY() / getHeight());
    invalidate();
    if (on_change != null)
      on_change.run();
    return true;
  }
}