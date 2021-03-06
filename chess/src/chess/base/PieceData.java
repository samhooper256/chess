package chess.base;

import java.io.Serializable;

import chess.piecebuilder.PieceBuilder;
import chess.util.ActionTree;
import javafx.scene.image.Image;

public class PieceData implements Serializable{
	
	public String getWhiteImageURIString() {
		return whiteImageURIString;
	}

	public void setWhiteImageURIString(String whiteImageURIString) {
		if(whiteImageURIString == null) {
			whiteImage = PieceBuilder.WHITE_DEFAULT_IMAGE;
			return;
		}
		this.whiteImageURIString = whiteImageURIString;
		try {
			whiteImage = new Image(whiteImageURIString, PieceBuilder.IMAGE_SIZE, PieceBuilder.IMAGE_SIZE, false, true);
		}
		catch(Throwable t) {
			System.err.println("Failed to load image from URI="+whiteImageURIString);
			whiteImage = PieceBuilder.ERROR_LOADING_IMAGE;
		}
	}

	public String getBlackImageURIString() {
		return blackImageURIString;
	}

	public void setBlackImageURIString(String blackImageURIString) {
		if(blackImageURIString == null) {
			blackImage = PieceBuilder.BLACK_DEFAULT_IMAGE;
			return;
		}
		this.blackImageURIString = blackImageURIString;
		try {
			blackImage = new Image(blackImageURIString, PieceBuilder.IMAGE_SIZE, PieceBuilder.IMAGE_SIZE, false, true);
		}
		catch(Throwable t) {
			System.err.println("Failed to load image from URI="+blackImageURIString);
			blackImage = PieceBuilder.ERROR_LOADING_IMAGE;
		}
	}

	public int getPointValue() {
		return pointValue;
	}

	public void setPointValue(int pointValue) {
		this.pointValue = pointValue;
	}

	public ActionTree getTree() {
		return tree;
	}

	public void setTree(ActionTree tree) {
		this.tree = tree;
	}

	public String getName() {
		return name;
	}

	public PieceType getPieceType() {
		return pieceType;
	}
	
	public Image getImage(boolean color) {
		if(color == Piece.WHITE) {
			if(whiteImage == null) {
				if(whiteImageURIString == null) {
					whiteImage = PieceBuilder.WHITE_DEFAULT_IMAGE;
				}
				else {
					try {
						whiteImage = new Image(whiteImageURIString, PieceBuilder.IMAGE_SIZE, PieceBuilder.IMAGE_SIZE, false, true);
					}
					catch(Throwable t) {
						System.err.println("Failed to load image from URI="+whiteImageURIString);
						whiteImage = PieceBuilder.ERROR_LOADING_IMAGE;
					}
				}
			}
			return whiteImage;
		}
		else if(color == Piece.BLACK) {
			if(blackImage == null) {
				if(blackImageURIString == null) {
					blackImage = PieceBuilder.BLACK_DEFAULT_IMAGE;
				}
				else {
					try {
						blackImage = new Image(blackImageURIString, PieceBuilder.IMAGE_SIZE, PieceBuilder.IMAGE_SIZE, false, true);
					}
					catch(Throwable t) {
						System.err.println("Failed to load image from URI="+blackImageURIString);
						blackImage = PieceBuilder.ERROR_LOADING_IMAGE;
					}
				}
			}
			return blackImage;
		}
		else {
			return null; //cannot ever happen
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 6260521702195702415L;
	private final String name;
	private int pointValue;
	private transient Image whiteImage;
	private transient Image blackImage;
	private String whiteImageURIString, blackImageURIString;
	private  ActionTree tree;
	private final PieceType pieceType;
	
	public PieceData(String name) {
		this(name, PieceBuilder.WHITE_DEFAULT_IMAGE, PieceBuilder.BLACK_DEFAULT_IMAGE);
	}
	
	public PieceData(String name, Image whiteImg, Image blackImg) {
		this.name = name;
		whiteImage = whiteImg;
		blackImage = blackImg;
		whiteImageURIString = null;
		blackImageURIString = null;
		tree = null;
		pointValue = 5;
		pieceType = PieceType.define(name, true);
	}
	
	public void updateImages() {
		if(whiteImageURIString == null) {
			whiteImage = PieceBuilder.WHITE_DEFAULT_IMAGE;
		}
		else {
			try {
				whiteImage = new Image(whiteImageURIString, PieceBuilder.IMAGE_SIZE, PieceBuilder.IMAGE_SIZE, false, true);
			}
			catch(Throwable t) {
				System.err.println("Failed to load image from URI="+whiteImageURIString);
				whiteImage = PieceBuilder.ERROR_LOADING_IMAGE;
			}
		}
		if(blackImageURIString == null) {
			blackImage = PieceBuilder.BLACK_DEFAULT_IMAGE;
		}
		else {
			try {
				blackImage = new Image(blackImageURIString, PieceBuilder.IMAGE_SIZE, PieceBuilder.IMAGE_SIZE, false, true);
			}
			catch(Throwable t) {
				System.err.println("Failed to load image from URI="+blackImageURIString);
				blackImage = PieceBuilder.ERROR_LOADING_IMAGE;
			}
		}
	}
}
