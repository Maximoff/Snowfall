package ru.maximoff.snowfall;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class SnowfallView extends LinearLayout {
    private static final int SNOWFLAKE_COUNT = 100; // Количество снежинок
    private static final float BASE_SIZE = 20f; // Размер снежинки базовый
    private static final float BASE_SPEED = 2f; // Скорость падения, базовая
    private static final float ROTATION_SPEED = 1f; // Скорость вращения, базовая
    private static final float SWAY_AMPLITUDE = 15f; // Амплитуда раскачивания, базовая

    private final List<Snowflake> snowflakes = new ArrayList<>();
    private final Random random = new Random();
    private Bitmap snowflakeBitmap;

    public SnowfallView(Context context) {
        super(context);
        init();
    }

    public SnowfallView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SnowfallView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotDraw(false); // Включение рисования в LinearLayout

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		if (prefs.getBoolean("res", false)) {
			snowflakeBitmap = loadSnowflakeBitmapFromResources((int) BASE_SIZE); // Загрузка шаблона снежинки из ресурсов
		} else {
			snowflakeBitmap = createSnowflakeBitmapWithShadow((int) BASE_SIZE); // Генерация простой шестиконечной снежинки с тенью
		}
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        snowflakes.clear();

        for (int i = 0; i < SNOWFLAKE_COUNT; i++) {
            float sizeMultiplier = 0.5f + random.nextFloat();
            snowflakes.add(new Snowflake(
							   random.nextFloat() * w, // Начальная X позиция
							   random.nextFloat() * h, // Начальная Y позиция
							   BASE_SPEED + random.nextFloat() * 2, // Лёгкий рандом для скорости падения
							   BASE_SIZE * sizeMultiplier, // Рандом для размера снежинок
							   random.nextFloat() * 360, // Рандомный начальный угол поворота
							   ROTATION_SPEED * (0.5f + random.nextFloat()), // Рандом для скорости вращения
							   SWAY_AMPLITUDE * sizeMultiplier // Рандом амплитуды раскачивания, связанный с размером
						   ));
        }

        // Сортировка снежинок по размеру (для эффекта глубины)
        Collections.sort(snowflakes, new Comparator<Snowflake>() {
				@Override
				public int compare(Snowflake s1, Snowflake s2) {
					return Float.compare(s1.size, s2.size);
				}
			});
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (Snowflake snowflake : snowflakes) {
            snowflake.update(getWidth(), getHeight());
            snowflake.draw(canvas, snowflakeBitmap);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            postInvalidateOnAnimation();
        } else {
            postInvalidateDelayed(16L); // 1000ms / 60fps
        }
    }

	private Bitmap loadSnowflakeBitmapFromResources(int size) {
		Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.snowflake);
		return Bitmap.createScaledBitmap(bitmap, size, size, true);
	}

    private Bitmap createSnowflakeBitmapWithShadow(int size) {
        int shadowOffset = size / 5;
        int bitmapSize = size + shadowOffset * 2;

        Bitmap bitmap = Bitmap.createBitmap(bitmapSize, bitmapSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);

        // Рисуем тень
        paint.setColor(Color.GRAY);
        paint.setMaskFilter(new BlurMaskFilter(shadowOffset / 2f, BlurMaskFilter.Blur.NORMAL));
        drawSnowflakeShape(canvas, paint, bitmapSize / 2f, bitmapSize / 2f, size / 2f);

        // Рисуем снежинку
        paint.setColor(Color.WHITE);
        paint.setMaskFilter(null);
        drawSnowflakeShape(canvas, paint, bitmapSize / 2f, bitmapSize / 2f, size / 2f);

        return bitmap;
    }

    private void drawSnowflakeShape(Canvas canvas, Paint paint, float centerX, float centerY, float radius) {
        for (int i = 0; i < 6; i++) {
            float angleRad = (float) Math.toRadians(i * 60);
            float endX = centerX + radius * (float) Math.cos(angleRad);
            float endY = centerY + radius * (float) Math.sin(angleRad);
            canvas.drawLine(centerX, centerY, endX, endY, paint);
        }
    }

    private static class Snowflake {
		private float x, y;
		private final float speedY;
		private final float size;
		private final float swayAmplitude;
		private final float swayPhase;
		private float angle;
		private final float rotationSpeed;
		private float time;

		Snowflake(float x, float y, float speedY, float size, float angle, float rotationSpeed, float swayAmplitude) {
			this.x = x;
			this.y = y;
			this.speedY = speedY;
			this.size = size;
			this.angle = angle;
			this.rotationSpeed = rotationSpeed * (Math.random() > 0.5 ? 1 : -1); // Случайное направление вращения
			this.swayAmplitude = swayAmplitude;
			this.swayPhase = (float) (Math.random() * 2 * Math.PI); // Рандомная фаза раскачивания
			this.time = 0;
		}

		void update(int width, int height) {
			y += speedY;
			angle += rotationSpeed;
			time += 0.05f;

			// Возвращаем снежинку наверх, если она выходит за экран
			if (y > height) {
				y = 0;
				x = (float) (Math.random() * width);
				time = 0;
			}
		}

		void draw(Canvas canvas, Bitmap bitmap) {
			// Генерация смещения с использованием случайной фазы
			float sway = (float) Math.sin(time + swayPhase) * swayAmplitude;

			canvas.save();
			canvas.translate(x + sway, y);
			canvas.rotate(angle, size / 2f, size / 2f);

			Rect src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
			RectF dst = new RectF(0, 0, size, size);
			canvas.drawBitmap(bitmap, src, dst, null);

			canvas.restore();
		}
	}
}
