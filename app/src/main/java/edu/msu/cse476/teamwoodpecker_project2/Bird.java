package edu.msu.cse476.teamwoodpecker_project2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.Serializable;

/**
 * This is a starting point for a class for a bird. It includes functions to
 * load the bird image and to do collision detection against another bird.
 */
public class Bird implements Serializable {
    private static final long serialVersionUID = 3L;

    public Bitmap getBird() {
        return bird;
    }

    /**
     * The image for the actual bird.
     */
    private transient Bitmap bird;

    public Rect getRect() {
        return rect;
    }

    /**
     * Rectangle that is where our bird is.
     */
    private transient Rect rect;

    /**
     * Rectangle we will use for intersection testing
     */
    private transient Rect overlap = new Rect();

    /**
     * The resource id for this bird's image
     */
    private int id;

    private float relX;

    private float relY;

    /**
     * x location
     */
    private float x;

    /**
     * y location
     */
    private float y;

    private Bird() { }

    public Bird(Context context, int id, float relX, float relY) {
        this.id = id;
        bird = BitmapFactory.decodeResource(context.getResources(), id);
        this.relX = relX;
        this.relY = relY;
        x = -1;
        y = -1;

        //bird.move()
        rect = new Rect();
        setRect();
    }

    public Bird(Bird copy) {
        this.id = copy.id;
        this.bird = copy.bird;
        this.relX = 0.5f;
        this.relY = 0.5f;
        x = -1;
        y = -1;

        rect = new Rect();
        setRect();
    }

    public void reloadBitmap(Context context) {
        bird = BitmapFactory.decodeResource(context.getResources(), id);
        rect = new Rect();
        setRect();
        overlap = new Rect();
    }

    public void move(float dx, float dy, float gameSize) {

        float width = bird.getWidth();
        float height = bird.getHeight();

        x += dx;
        y += dy;

        relX = x / (gameSize - bird.getWidth());
        relY = y / (gameSize - bird.getHeight());

        if (x < 0)
            x = 0;
        else if (x + width > gameSize)
            x = gameSize - width;

        if (y < 0)
            y = 0;
        else if (y + height > gameSize)
            y = gameSize - height;

        setRect();
    }

    private void setRect() {
        rect.set((int)x, (int)y, (int)x+bird.getWidth(), (int)y+bird.getHeight());
    }

    public boolean hit(float testX, float testY, int gameSize, float scaleFactor) {
        int pX = (int)(testX - x);
        int pY = (int)(testY - y);

        if(pX < 0 || pX >= bird.getWidth() ||
                pY < 0 || pY >= bird.getHeight()) {
            return false;
        }
        // We are within the rectangle of the piece.
        // Are we touching actual picture?
        return (bird.getPixel(pX, pY) & 0xff000000) != 0;
    }

    /**
     * Collision detection between two birds. This object is
     * compared to the one referenced by other
     * @param other Bird to compare to.
     * @return True if there is any overlap between the two birds.
     */
    public boolean collisionTest(Bird other) {
        // Do the rectangles overlap?
        if(!Rect.intersects(rect, other.rect)) {
            return false;
        }

        // Determine where the two images overlap
        overlap.set(rect);
        overlap.intersect(other.rect);

        // We have overlap. Now see if we have any pixels in common
        for(int r=overlap.top; r<overlap.bottom;  r++) {
            int aY = (int)((r - y));
            int bY = (int)((r - other.y));

            for(int c=overlap.left;  c<overlap.right;  c++) {

                int aX = (int)((c - x));
                int bX = (int)((c - other.x));

                if( (bird.getPixel(aX, aY) & 0x80000000) != 0 &&
                        (other.bird.getPixel(bX, bY) & 0x80000000) != 0) {
                    Log.i("collision", "Overlap " + r + "," + c);
                    return true;
                }
            }
        }

        return false;
    }

    public void draw(Canvas canvas, int marginX, int marginY, float gameSize) {

        if (x == -1 && y == -1) {
            x = (relX * gameSize) - (relX * bird.getWidth());
            y = (relY * gameSize) - (relY * bird.getHeight());
            setRect();
        }

        canvas.save();
        canvas.translate(marginX + x, marginY + y);
        //canvas.translate(-bird.getWidth() / 2, -bird.getHeight() / 2);
        canvas.drawBitmap(bird, 0, 0, null);
        canvas.restore();
    }

    public void serialize(XmlSerializer serializer) throws IOException {
        serializer.startTag(null, "bird");
        serializer.attribute(null, "id", String.valueOf(id));
        serializer.attribute(null, "relX", String.valueOf(relX));
        serializer.attribute(null, "relY", String.valueOf(relY));
        serializer.endTag(null, "bird");
    }

    public static Bird deserialize(Context context, XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "bird");

        int id = Integer.parseInt(parser.getAttributeValue(null, "id"));
        float relX = Float.parseFloat(parser.getAttributeValue(null, "relX"));
        float relY = Float.parseFloat(parser.getAttributeValue(null, "relY"));

        parser.nextTag();
        parser.require(XmlPullParser.END_TAG, null, "bird");

        Bird bird = new Bird(context, id, relX, relY);

        return bird;
    }
}