package chess.base;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class WrappedImageView extends ImageView
{	
	private final int minWidth, minHeight;
	public WrappedImageView(Image im, int minWidth, int minHeight) {
		super(im);
        setPreserveRatio(false);
        this.minWidth = minWidth;
        this.minHeight = minHeight;
	}
	
    public WrappedImageView(Image im)
    {
    	this(im, 20, 20);
    }

    @Override
    public double minWidth(double height)
    {
        return minWidth;
    }

    @Override
    public double prefWidth(double height)
    {
        Image I=getImage();
        if (I==null) return minWidth(height);
        return I.getWidth();
    }

    @Override
    public double maxWidth(double height)
    {
        return 16384;
    }

    @Override
    public double minHeight(double width)
    {
        return minHeight;
    }

    @Override
    public double prefHeight(double width)
    {
        Image I=getImage();
        if (I==null) return minHeight(width);
        return I.getHeight();
    }

    @Override
    public double maxHeight(double width)
    {
        return 16384;
    }

    @Override
    public boolean isResizable()
    {
        return true;
    }

    @Override
    public void resize(double width, double height)
    {
        setFitWidth(width);
        setFitHeight(height);
    }
}
