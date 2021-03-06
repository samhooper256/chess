package chess.base;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import chess.base.GamePanel.Mode;
import chess.util.AFC;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;

/* *
 * @author Sam Hooper
 */
public class Board extends StackPane{
	public static final int MAX_BOARD_SIZE = 26;
	public static final int MIN_BOARD_SIZE = 3;
	public static final int DEFAULT_BOARD_SIZE = 8;
	private static BoardPreset defaultBoardPreset;
	
	private final int BOARD_SIZE;
	private Tile[][] board;
	private int moveNumber, playNumber, movesSincePawnOrCapture;
	/**
	 * kingLocations[0] is white king, kingLocations[1] is black king
	 */
	private int[][] kingLocations;
	private Tile currentlySelectedTile;
	private BoardPreset preset;
	private boolean turn;
	private volatile boolean boardInteractionAllowed = true;
	private volatile boolean orientation;
	//UI Components
	private GamePanel associatedGP;
	private BorderPane boardOverlay;
	private StackPane boardPopupBox, promotionStackPane;
	private GridPane grid, promotionGridPane, popupMessageArea;
	private AnchorPane popupMessageOverlay;
	private Label boardPopupMessage;
	private Text popupMessageXButton, popupGameOverText;
	private Button popupResetButton, popupViewBoardButton;
	private Tile[][] boardDisplay;
	private ScrollPane promotionScrollPane;
	private ActionOptionsDisplay actionOptionsDisplay;
	
	private Lock lock2 = new ReentrantLock();
	
	static String LIGHT_COLOR = "#1abb9a";
	static String DARK_COLOR = "#2c3d4f";
	private static String LIGHT_COLOR_SELECTED = "#38e8c4";
	private static String DARK_COLOR_SELECTED = "#45576b";
	
	static {
		defaultBoardPreset = new BoardPreset("Default", DEFAULT_BOARD_SIZE);
		defaultBoardPreset.setTurn(Piece.WHITE);
		defaultBoardPreset.setPieces(new String[][] {
			{"-Rook", "-Knight", "-Bishop", "-Queen", "-King", "-Bishop", "-Knight", "-Rook"},
			{"-Pawn", "-Pawn", "-Pawn", "-Pawn", "-Pawn", "-Pawn", "-Pawn", "-Pawn"},
			null, null, null, null,
			{"+Pawn", "+Pawn", "+Pawn", "+Pawn", "+Pawn", "+Pawn", "+Pawn", "+Pawn"},
			{"+Rook", "+Knight", "+Bishop", "+Queen", "+King", "+Bishop", "+Knight", "+Rook"}
		});
	}
	
	private class ClickHandler extends Service<Void>{
		
		private MouseEvent event;
		
		public void handle(MouseEvent event) {
			this.event = event;
			this.restart();
		}
		
		@Override
		protected Task<Void> createTask() {
			return new Task<Void>() {
				volatile boolean flag;
				@Override
				protected Void call(){
					try {
						Tile source = (Tile) event.getSource();
						//System.out.printf("source = Tile @ (%d, %d)%n", source.row, source.col);
						
						flag = false;
						//You click on square that's showing as legal (aka it has a dot on it).
						if(source.isShowingLegal()) {
							System.out.printf("You clicked on square that's showing as legal (aka it has a dot on it)%n");
							Object lock1 = new Object();
							//System.out.println("BIT = false :: from CH");
							boardInteractionAllowed = false;
							final int row = currentlySelectedTile.row;
							final int col = currentlySelectedTile.col;
							final Set<LegalAction> plays = new HashSet<>(source.legalMovesShowing);
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									//System.out.println("\tAttempting to sync from CH (on FX Thread)");
									synchronized(lock1) {
										//System.out.println("\t\tSuccessfully synced from CH (on FX Thread)");
										currentlySelectedTile.hideLegalMovesAndDepopulate();
										currentlySelectedTile.setHighlighted(false);
										currentlySelectedTile = null;
										//System.out.println("\t\tnotifying all CH (on FX Thread)");
										lock1.notifyAll();
										flag = true;
									}
								}
							});
							
							//System.out.println(">>> attempting to sync on lock1; " + Thread.currentThread().getName());
							synchronized(lock1) {
								while(!flag) {
									//System.out.println("\t>>> starting wait on lock1; " + Thread.currentThread().getName());
									try {
										lock1.wait();
									} catch (InterruptedException e) {}						
								}
							}
							//System.out.println("\t>>> done waiting on lock1; " + Thread.currentThread().getName());	
							
							System.out.println("----------starting select-and-making");
							synchronized(board) {
								Board.this.moveMaker.selectAndMake(row, col, plays);
							}
							System.out.println("----------done select-and-making");
							
							//System.out.println("BIT = true :: from CH");
							boardInteractionAllowed = true;
							
							
							
						}
						//You click on one of your own pieces:
						else if(source.isOccupied() && source.getPiece().getColor() == turn) {
							Object lock1 = new Object();
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									synchronized(lock1) {
										if(currentlySelectedTile == null) {
											source.setHighlighted(true);
											source.showLegalMovesAndPopulate();
											currentlySelectedTile = source;
										}
										else if(source == currentlySelectedTile) {
											source.hideLegalMovesAndDepopulate();
											source.setHighlighted(false);
											currentlySelectedTile = null;
										}
										else if(currentlySelectedTile != null) {
											currentlySelectedTile.hideLegalMovesAndDepopulate();
											currentlySelectedTile.setHighlighted(false);
											source.setHighlighted(true);
											source.showLegalMovesAndPopulate();
											currentlySelectedTile = source;
										}
										lock1.notifyAll();
										flag = true;
									}
								}
							});
							synchronized(lock1) {
								while(!flag) {
									try {
										lock1.wait();
									} catch (InterruptedException e) {}
								}
							}
							
							
						}
						//You click on an empty square or on an enemy piece (that is not showing as a legal move):
						else if(source.isEmpty() || source.getPiece().getColor() == !turn) {
							Object lock1 = new Object();
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									synchronized(lock1) {
										Board.this.deselect();
										lock1.notifyAll();
										flag = true;
									}
								}
							});
							synchronized(lock1) {
								while(!flag) {
									try {
										lock1.wait();
									} catch (InterruptedException e) {}
								}
							}
							
						}
						else {
							throw new IllegalArgumentException("Illegal click - Sam exposed!?!?!?");
						}
					}
					catch(Throwable t) {
						if(!(t instanceof InterruptedException && isCancelled())) {
							t.printStackTrace();
						}
						else {
							boardInteractionAllowed = true;
						}
					}
					finally {
						lock2.unlock();
					}
					return null;
					
				}
				
			};
		}
		
	}
	
	public void deselect() {
		if(currentlySelectedTile != null) {
			currentlySelectedTile.hideLegalMovesAndDepopulate();
			currentlySelectedTile.setHighlighted(false);
			currentlySelectedTile = null;
		}
	}
	
	private ClickHandler clickHandler = new ClickHandler();
	
	public class MovePreparer extends Service<Void>{
		
		public void prepare() {
			if(!isRunning()) {
				this.restart();
			}
			
		}
		@Override
		protected Task<Void> createTask() {
			return new Task<Void>() {

				@Override
				protected Void call() {
					try {
						synchronized(board) {
							System.out.println("$$$Move preparing prepping:");
							prepareForNextMove();
						}
					}
					catch(Throwable t) {
						t.printStackTrace();
					}
					return null;
				}
				
			};
		}
		
	}
	
	public MovePreparer movePreparerForFXThread = new MovePreparer();
	
	class MoveMaker{
		public MoveMaker() {}
		Lock playLock = new ReentrantLock();
		Lock selectionLock = new ReentrantLock();
		volatile boolean makePlayHelperFlag;
		
		private volatile Piece selectionPiece;
		public Piece selectPiece(final boolean pieceColor, Collection<String> options) {
			selectionLock.lock();
			try {
				selectionPiece = null;
				Object o = new Object();
				class PromotionIcon extends ImageView{
					Piece myPiece;
					
					public PromotionIcon(Piece p) {
						super(p.getImage());
						this.myPiece = p;
						this.setFitWidth(100);
						this.setPreserveRatio(true);
						this.setOnMouseClicked(event -> {
							promotionGridPane.getChildren().clear();
							boardOverlay.setVisible(false);
							if(selectionPiece == null) {
								selectionPiece = myPiece;
							}
							synchronized(o) {
								o.notifyAll();
							}
						});
					}
				}
				Piece[] optionsArr = new Piece[options.size()];
				int j = 0;
				for(Iterator<String> itr = options.iterator(); itr.hasNext();) {
					optionsArr[j] = Piece.forName(itr.next(), pieceColor);
					j++;
				}
				
				Platform.runLater(new Runnable() {

					@Override
					public void run() {
						boardOverlay.setCenter(null);
						for(int i = 0; i < optionsArr.length; i++) {
							promotionGridPane.add(new PromotionIcon(optionsArr[i]), i % 4, i / 4);
						}
						boardOverlay.setCenter(promotionStackPane);
						boardOverlay.setVisible(true);
						boardOverlay.requestLayout();
					}
					
				});
				
				synchronized(o) {
					while(selectionPiece == null) {
						try {
							o.wait();
						} catch (InterruptedException e) {}
					}
				}
			}
			catch(Throwable t) {
				t.printStackTrace();
			}
			finally {
				selectionLock.unlock();
			}
			if(selectionPiece == null) {
				throw new IllegalArgumentException("Bad news bears! null returned as the selected piece...");
			}
			Piece thePiece = selectionPiece;
			selectionPiece = null;
			return thePiece;
		}
		/**
		 * Should NOT be called from FX Thread.
		 */
		public LegalAction selectAction(Collection<LegalAction> options) {
			Object lock1 = new Object();
			Platform.runLater(new Runnable() {
				public void run() {
					actionOptionsDisplay.choose(lock1, options);
				}
			});
			
			synchronized(lock1) {
				while(!actionOptionsDisplay.hasAction()) {
					try {
						lock1.wait();
					} catch (InterruptedException e) {}
				}
			}
			
			return actionOptionsDisplay.takeAction();
			
		}
		
		public void selectAndMake(int startRow, int startCol, Set<LegalAction> options) {
			playLock.lock();
			LegalAction end = null;
			Piece onDest = null;
			try {
				Piece piece = Board.this.getPieceAt(startRow, startCol);
				if(options.size() == 1) {
					end = options.iterator().next();
				}
				else {
					end = selectAction(options);
				}
				synchronized(Board.this.board) {
					onDest = Board.this.board[end.destRow()][end.destCol()].currentPiece;
					end.handle(startRow, startCol, Board.this);
				}
				
				synchronized(kingLocations) {
					kingLocations = findKings();
				}
				BoardPlay play = log.log(startRow, startCol, piece, onDest, end);
				wrapUpPlay(play);
			}
			catch(Throwable t) {
				t.printStackTrace();
			}
			finally {
				playLock.unlock();
			}
			
		}
		
		private void wrapUpPlay(BoardPlay play){
			System.out.println("wrapup called");
			if(turn == Piece.WHITE) {
				setTurn(Piece.BLACK);
			}
			else {
				setTurn(Piece.WHITE);
				moveNumber++;
				LegalAction action = play.getPlay();
				if((action instanceof LegalMoveAndCapture || action instanceof LegalCapture) && play.getOnDest() != null
						|| play.getPiece() instanceof Pawn) {
					movesSincePawnOrCapture = 0;
				}
				else {
					movesSincePawnOrCapture++;
				}
			}
			
			playNumber++;
			
			System.out.println("turn = " + turn + ", autoflip = " + Settings.getAutoFlip());
			if(Settings.getAutoFlip()) {
				setOrientation(turn);
			}
			
			prepareForNextMove();
		}
	}
	
	private void setTurn(boolean color) {
		Board.this.turn = color;
		
		Platform.runLater(new Runnable() {
			public void run() {
				if(color == Piece.WHITE) {
					Board.this.associatedGP.setTurnText(GamePanel.WHITE_TO_MOVE_TEXT);
				}
				else {
					Board.this.associatedGP.setTurnText(GamePanel.BLACK_TO_MOVE_TEXT);
				}
			}
		});
	}
	
	protected MoveMaker moveMaker = new MoveMaker();
	
	private EventHandler<? super MouseEvent> tileClickAction = event -> {
		if(event.getButton() == MouseButton.PRIMARY) {
			//Tile source = (Tile) event.getSource();
			if(Board.this.associatedGP.getMode() == GamePanel.Mode.PLAY) {
				if(!boardInteractionAllowed) {
					System.out.println("tile click blocked because board interaction is not allowed.");
					return;
				}
				
				if(!clickHandler.isRunning()) {
					if(lock2.tryLock()) {
						clickHandler.handle(event);
					}
					else {
						System.out.println("cucked! tryLock failed :(");
					}
				}
			}
			else {
				System.out.println("Interaction blocked because GamePanel mode is not Mode.PLAY");
			}
		}
	};
	
	private EventHandler<? super MouseEvent> popupMessageXButtonClickAction = event -> {
		boardOverlay.setCenter(null);
		boardOverlay.setVisible(false);
	};
	
	
	private EventHandler<DragEvent> tileOnDragOver = dragEvent -> {
		
		Dragboard db = dragEvent.getDragboard();
        if (db.hasString()) {
        	dragEvent.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
        
        dragEvent.consume();
	};
	
	private EventHandler<DragEvent> tileOnDragDropped = dragEvent -> {
		System.out.println("DRAG DROPPED");
		Tile eventSource = (Tile) dragEvent.getSource();
		Dragboard db = dragEvent.getDragboard();
        boolean success = false;
        if (db.hasString()) {
        	Set<TransferMode> transferModes = db.getTransferModes();
        	if(transferModes.size() == 1) {
        		TransferMode tmode = transferModes.iterator().next();
        		if(tmode == TransferMode.COPY) {
        			String pieceName = db.getString();
                    System.out.println("Dropped " + pieceName + " on " + eventSource);
                    eventSource.setPiece(Piece.forName(pieceName));
                    success = true;
        		}
        		else if(tmode == TransferMode.MOVE) {
        			String text = db.getString();
        			int commaIndex = text.indexOf(',');
        			if(commaIndex > 0) {
        				int row = Integer.parseInt(text.substring(0, commaIndex));
        				int col = Integer.parseInt(text.substring(commaIndex + 1));
        				Tile pieceSource = Board.this.getTileAt(row, col);
        	        	Piece pieceToMove = pieceSource.getPiece();
        	        	pieceSource.setPiece(null);
        	        	eventSource.setPiece(pieceToMove);
        	        	success = true;
        			}
        			
        		}
        		
        	}

    		
        }
        if(success) {
        	Board.this.associatedGP.finishDrag();
        }
        dragEvent.setDropCompleted(success);
        dragEvent.consume();
	};
	
	
	private EventHandler<? super DragEvent> tileOnDragDone = dragEvent -> {
		Board.this.associatedGP.finishDrag();
	};
	
	public class Tile extends StackPane{

		class ColorBox extends Pane{
			private ArrayList<Shape> indicators;
			private int MAX_INDICATORS = 20;
			
			public ColorBox() {
				super();
				indicators = new ArrayList<>();
			}
			
			public void addIndicatorWithoutRefreshing(Shape indicator) {
				indicators.add(indicator);
			}
			
			public void refresh() {
				getChildren().clear();
				recalculate();
				int max = Math.min(indicators.size(), MAX_INDICATORS);
				
				for(int i = 0; i < max; i++) {
					getChildren().add(indicators.get(i));
				}
				
			}
			
			public void addIndicator(Shape indicator) {
				indicators.add(indicator);
				if(indicators.size() < MAX_INDICATORS) {
					refresh();
				}
			}
			
			public void addAllIndicators(Shape... indicatorArr) {
				for(int i = 0; i < indicatorArr.length; i++) {
					indicators.add(indicatorArr[i]);
				}
				refresh();
			}
			
			public void removeIndicator(Shape indicator) {
				indicators.remove(indicator);
				refresh();
			}
			
			public void removeAllIndicators() {
				indicators.clear();
				refresh();
			}
			
			private void recalculate() {
				final int size = indicators.size();
				if(size == 1) {
					Shape indicator = indicators.get(0);
					indicator.layoutXProperty().bind(widthProperty().divide(2));
					indicator.layoutYProperty().bind(heightProperty().divide(2));
				}
				else {
					int displayedIndicators = Math.min(size, MAX_INDICATORS);
					for(int i = 0; i < displayedIndicators; i++) {
						Shape indicator = indicators.get(i);
						double mult = 2 * Math.PI / displayedIndicators;
						indicator.layoutXProperty().bind(widthProperty().divide(2).add(widthProperty().divide(4).multiply(Math.cos(-Math.PI/2 + mult * i))));
						indicator.layoutYProperty().bind(heightProperty().divide(2).add(heightProperty().divide(4).multiply(Math.sin(-Math.PI/2 + mult * i))));
					}
				}
				
			}

		}
		private Piece currentPiece;
		private int row;
		private int col;
		private boolean isHighlighted;
		private boolean isShowingLegal;
		private Set<LegalAction> legalActions;
		private Set<LegalAction> legalMovesShowing;
		private WrappedImageView currentImageView;
		private ColorBox colorBox;
		
		public Tile(int row, int col) {
			super();
			this.row = row;
			this.col = col;
			this.legalMovesShowing = new HashSet<>();
			isHighlighted = false;
			isShowingLegal = false;
			currentPiece = null;
			currentImageView = null;
			colorBox = new ColorBox();
			this.getChildren().add(colorBox);
			this.setStyle("-fx-background-color: " + ((row+col) % 2 == 0 ? LIGHT_COLOR : DARK_COLOR) + ";");
	        this.setOnMouseClicked(tileClickAction);
	        this.setOnDragOver(tileOnDragOver);
	        this.setOnDragDropped(tileOnDragDropped);
	        this.setOnDragDetected(dragEvent -> {
	        	if(Board.this.associatedGP.getMode() == GamePanel.Mode.FREEPLAY) {
	    			Tile source = (Tile) dragEvent.getSource();
	    			if(source.isOccupied()) {
	    				Dragboard db = startDragAndDrop(TransferMode.MOVE);
	    				SnapshotParameters parameters = new SnapshotParameters();
	    				parameters.setFill(Color.TRANSPARENT);
	    				Image dragViewImage = source.currentImageView.snapshot(parameters, null);
	    				db.setDragView(dragViewImage, dragViewImage.getWidth()/2, dragViewImage.getHeight()/2);
	    				System.out.println("started dnd");
	    				ClipboardContent content = new ClipboardContent();
	    		        content.putString(source.getRow() + "," + source.getCol());
	    		        db.setContent(content);
	    		        Board.this.associatedGP.startDrag(dragEvent, currentPiece);
	    		        dragEvent.consume();
	    			}
	    		}
	        });
	        this.setOnDragDone(tileOnDragDone);
		}
		
		public void showLegalMovesAndPopulate() {
			if(legalActions != null) {
				for(LegalAction a : legalActions) {
					board[a.row()][a.col()].legalMovesShowing.add(a);
					board[a.row()][a.col()].showLegalIndicators();
				}
			}
		}
		
		void printDetailedData(PrintStream out){
			out.printf("---------------%nDetailed Data for Tile at (%d, %d) :%n", row, col);
			out.printf("Piece >> %s%n", currentPiece);
			out.printf("legalMovesShowing >> %s%n", legalMovesShowing);
			out.printf("legalActions >> %s%n", legalActions);
			out.printf("---------------%n");
		}
		/**
		 * Calculates the {@code Set<LegalAction>} available to the piece on this tile by calling
		 * {@link Piece#getLegalActions(Board, int, int)}. Stores the result in {@code Tile.this.legalActions}
		 * (private instance variable).
		 * 
		 * @return true if one or more legal moves are found, false otherwise.
		 */
		private boolean calculateLegalActions() {
			this.legalActions = currentPiece == null ? null : currentPiece.getLegalActions(Board.this, row, col);
			
			return this.legalActions != null && this.legalActions.size() > 0;
			
		}
		
		/**
		 * Interacts with scene graph. MUST BE CALLED FROM FX APPLICATION THREAD.
		 * @param p
		 */
		Piece setPiece(Piece p) {
			if(p == null && currentPiece == null) return null;
			Piece temp = currentPiece;
			currentPiece = p;
			
			synchronized(Tile.this) {
				if(currentPiece == null) {
					Tile.this.getChildren().remove(currentImageView);
					currentImageView = null;
					Tile.this.legalActions = null;
				}
				else {
					Tile.this.getChildren().remove(currentImageView);
					Tile.this.getChildren().add(0, currentImageView = new WrappedImageView(p.getImage()));
				}
				Tile.this.requestLayout();
			}
			
			return temp;
		}
		
		/**
		 * Interacts with scene graph. <b>Must be called from the FX Application Thread.</b>
		 * @param value
		 */
		private void setHighlighted(boolean value) {
			if(value == isHighlighted) {
				return;
			}
			else {
				isHighlighted = value;
			}
			
			synchronized(Tile.this) {
				if(isHighlighted) {
					Tile.this.setStyle("-fx-background-color: " + ((row+col) % 2 == 0 ? LIGHT_COLOR_SELECTED : DARK_COLOR_SELECTED) + ";");
				}
				else {
					Tile.this.setStyle("-fx-background-color: " + ((row+col) % 2 == 0 ? LIGHT_COLOR : DARK_COLOR) + ";");
				}
			}
			
		}
		
		
		private void hideLegalMovesAndDepopulate() {
			if(legalActions != null) {
				for(LegalAction a : legalActions) {
					board[a.row()][a.col()].hideLegalIndicators();
					board[a.row()][a.col()].legalMovesShowing.clear();
				}
			}
		}
		
		private void hideLegalIndicators() {
			colorBox.removeAllIndicators();
			isShowingLegal = false;
		}
		
		private void showLegalIndicators() {
			colorBox.removeAllIndicators();
			for(LegalAction la : legalMovesShowing) {
				colorBox.addIndicatorWithoutRefreshing(la.indicator(8));
			}
			colorBox.refresh();
			isShowingLegal = true;
			
		}
		
		private boolean isShowingLegal() {
			return isShowingLegal;
		}
		
		@AFC(name="is occupied")
		public boolean isOccupied() {
			return currentPiece != null;
		}
		
		@AFC(name="is empty")
		public boolean isEmpty() {
			return currentPiece == null;
		}
		
		@AFC(name="get piece")
		public Piece getPiece() {
			return currentPiece;
		}
		public String toString() {
			return String.format("[Tile %d,%d]", row,col);
		}
		
		@AFC(name="row")
		public int getRow() { return row; }
		@AFC(name="col")
		public int getCol() { return col; }
		
		public boolean isCheckableBy(boolean myColor) {
			boolean attackingColor = !myColor;
			for(int i = 0; i < BOARD_SIZE; i++) {
				for(int j = 0; j < BOARD_SIZE; j++) {
					/*this is NECESSARY! otherwise you get a stackOverflow because
					a king tries to see if it can check itself, but in order to know whether it can
					check itself it has to know what its legal moves are, and in order to know
					its legal moves, it calls this method... then it enters this loop again and... well...
					*/
					if(i == this.row && j == this.col) continue; 
					Piece o = getPieceAt(i, j);
					if(	//if this breaks down, it's because I removed the condition "!(o instanceof King)
						o != null && o.getColor() == attackingColor &&
						o.canCheck(Board.this, i, j, row, col)) {
						return true;
					}
				}
			}
			return false;
		}
		
		@AFC(name="enemy line distance", paramDescriptions={"color"})
		public int enemyLineDistance(boolean color) {
			return color == Piece.WHITE ? row + 1 : BOARD_SIZE - row;
		}
		
		/**
		 * Return the color of this tile.
		 * @return true if this is a light square, false if it's a dark square.
		 */
		public boolean tileColor() {
			return (row+col) % 2 == 0;
		}
		
		@AFC(name="is light")
		public boolean isLight() {
			return tileColor() == true;
		}
		
		@AFC(name="is dark")
		public boolean isDark() {
			return tileColor() == false;
		}
		
	}
	
	public class BoardLog{
		private List<BoardPlay> logList;
		public BoardLog() {
			logList = new ArrayList<>();
		}
		
		@AFC(name="play count")
		public int playCount() {
			return logList.size();
		}
		
		@AFC(name="last play")
		public BoardPlay last() {
			return logList.get(logList.size() - 1);
		}
		
		public BoardPlay log(int startRow, int startCol, Piece piece, Piece onDest, LegalAction action) {
			if(action == null) {
				throw new NullPointerException("cannot log a null action");
			}
			BoardPlay play = BoardPlay.of(startRow, startCol, piece, onDest, action);
			logList.add(play);
			return play;
		}
		
		public void clear() {
			logList.clear();
		}
	}
	
	private BoardLog log = new BoardLog();
	
	@AFC(name="get play log")
	public BoardLog getLog() {
		return log;
	}
	
	@AFC(name="last play")
	public BoardPlay lastPlay() {
		return log.last();
	}
	
	@AFC(name="has play")
	public boolean hasPlay() {
		return log.playCount() > 0;
	}
	
	public static Board emptyBoard(GamePanel panel) {
		return new Board(panel, DEFAULT_BOARD_SIZE);
	}
	
	public static Board emptyBoard(GamePanel panel, int size) {
		return new Board(panel, size);
	}
	
	public static Board defaultBoard(GamePanel panel) {
		return new Board(panel, defaultBoardPreset);
	}
	
	public static Board fromPreset(GamePanel panel, BoardPreset preset) {
		return new Board(panel, preset);
	}
	
	private Board(GamePanel panel, int size) {
		if(size < MIN_BOARD_SIZE || size > MAX_BOARD_SIZE) {
			throw new IllegalArgumentException("Desired board size is in the range " + MIN_BOARD_SIZE + " to " +
					MAX_BOARD_SIZE);
		}
		this.associatedGP = panel;
		BOARD_SIZE = size;
		turn = Piece.WHITE;
		this.preset = null;
		
		finishBoardInit();
		//Empty board constructor does not prepare for next move so that you don't immediately
		//see "Game Over" because no kings exist. (and it would waste time).
	}
	
	private Board(GamePanel panel, BoardPreset preset) {
		if(preset.getBoardSize() < MIN_BOARD_SIZE || preset.getBoardSize() > MAX_BOARD_SIZE) {
			throw new IllegalArgumentException("Desired board size (" + preset.getBoardSize() + ") is greater " + 
			"than MAX_BOARD_SIZE (26)");
		}
		this.associatedGP = panel;
		BOARD_SIZE = preset.getBoardSize();
		turn = preset.getTurn();
		this.preset = preset;
		
		finishBoardInit();
		setupPiecesFromPreset(preset);
		if(panel.getMode() == Mode.PLAY) {
			movePreparerForFXThread.prepare();
		}
	}
	
	private void finishBoardInit() {
		board = new Tile[BOARD_SIZE][BOARD_SIZE];
		boardDisplay = new Tile[BOARD_SIZE][BOARD_SIZE];
		kingLocations = new int[][] {{-1,-1},{-1,-1}};
		orientation = Piece.WHITE;
		moveNumber = 1;
		playNumber = 1;
		movesSincePawnOrCapture = 0;
		initGUI();
	}

	private void initGUI() {
		//Setup board tiles:
		grid = new GridPane();
		grid.setMinSize(200, 200);
		
		for (int i = 0; i < BOARD_SIZE; i++) {
			final ColumnConstraints columnConstraints = new ColumnConstraints(Control.USE_PREF_SIZE, Control.USE_COMPUTED_SIZE, Double.MAX_VALUE);
            columnConstraints.setPercentWidth(100.0/BOARD_SIZE);
            grid.getColumnConstraints().add(columnConstraints);
            
            final RowConstraints rowConstraints = new RowConstraints(Control.USE_PREF_SIZE, Control.USE_COMPUTED_SIZE, Double.MAX_VALUE);
            rowConstraints.setPercentHeight(100.0/BOARD_SIZE);
            grid.getRowConstraints().add(rowConstraints);
        }
		
		
		
		for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
            	Tile child = new Tile(i, j);
	            board[i][j] = boardDisplay[i][j] = child;
                GridPane.setRowIndex(child, i);
                GridPane.setColumnIndex(child, j);
                grid.getChildren().add(child);
            }
        }
		
		this.getChildren().add(0, grid);
		
		////Setup "Game Over" screen:
		boardOverlay = new BorderPane();
		boardOverlay.setVisible(false);
		boardOverlay.setMaxSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		
		boardPopupBox = new StackPane();
		boardPopupBox.setMaxSize(300, 200);
		boardPopupBox.setPrefSize(300, 200);
		boardPopupBox.setAlignment(Pos.CENTER);
		
		popupMessageArea = new GridPane();
		popupMessageArea.setId("popup-message-area");
		//popupMessageArea.setGridLinesVisible(true);
		{
			RowConstraints row1 = new RowConstraints();
			row1.setValignment(VPos.CENTER);
			row1.setPercentHeight(25);
			RowConstraints row2 = new RowConstraints();
			row2.setValignment(VPos.CENTER);
			row2.setPercentHeight(35);
			RowConstraints row3 = new RowConstraints();
			row3.setValignment(VPos.CENTER);
			row3.setPercentHeight(40);
			ColumnConstraints col1 = new ColumnConstraints();
			col1.setHalignment(HPos.CENTER);
			col1.setPercentWidth(50);
			ColumnConstraints col2 = new ColumnConstraints();
			col2.setHalignment(HPos.CENTER);
			col2.setPercentWidth(50);
			popupMessageArea.getRowConstraints().addAll(row1, row2, row3);
			popupMessageArea.getColumnConstraints().addAll(col1, col2);
		}
		
		boardPopupBox.getChildren().add(popupMessageArea);
		
		boardPopupMessage = new Label();
		boardPopupMessage.setId("popup-message");
		
		popupGameOverText = new Text("Game Over");
		popupGameOverText.setId("game-over-text");
		
		popupResetButton = new Button("Reset Board");
		popupResetButton.getStyleClass().add("popup-button");
		popupResetButton.setPrefSize(100, 50);
		popupResetButton.setOnMouseClicked(event -> Board.this.reset());
		
		popupViewBoardButton = new Button("View Board");
		popupViewBoardButton.getStyleClass().add("popup-button");
		popupViewBoardButton.setPrefSize(100, 50);
		popupViewBoardButton.setOnMouseClicked(event -> {
			boardOverlay.setCenter(null);
			boardOverlay.setVisible(false);
		});
		
		popupMessageArea.add(popupGameOverText, 0, 0, 2, 1);
		popupMessageArea.add(boardPopupMessage, 0, 1, 2, 1);
		popupMessageArea.add(popupResetButton, 0, 2, 1, 1);
		popupMessageArea.add(popupViewBoardButton, 1, 2, 1, 1);
		
		popupMessageOverlay = new AnchorPane();
		popupMessageOverlay.setPickOnBounds(false);
		
		popupMessageXButton = new Text("X");
		popupMessageXButton.setId("popup-x");
		popupMessageXButton.setOnMouseClicked(popupMessageXButtonClickAction);
		
		AnchorPane.setRightAnchor(popupMessageXButton, 5.0);
		AnchorPane.setTopAnchor(popupMessageXButton, 5.0);
		popupMessageOverlay.getChildren().add(popupMessageXButton);
		
		boardPopupBox.getChildren().add(popupMessageOverlay);
		
		this.getChildren().add(1, boardOverlay);
		
		////Setup Promotion Menu:
		promotionGridPane = new GridPane();
		promotionGridPane.setGridLinesVisible(true);
		
		promotionScrollPane = new ScrollPane(promotionGridPane);
		promotionScrollPane.setFitToWidth(true);
		
		promotionStackPane = new StackPane();
		promotionStackPane.maxHeightProperty().bind(grid.heightProperty().divide(2));
		promotionStackPane.maxWidthProperty().bind(grid.widthProperty().divide(2));
		promotionStackPane.setStyle("-fx-background-color : #1FFFFE");
		
		promotionStackPane.getChildren().add(promotionScrollPane);
		
		actionOptionsDisplay = new ActionOptionsDisplay();
		actionOptionsDisplay.setId("action-options-display");
		actionOptionsDisplay.setMessage("Select an action");
		actionOptionsDisplay.setMinSize(300, 300);
		actionOptionsDisplay.maxWidthProperty().bind(this.widthProperty().divide(2));
		actionOptionsDisplay.maxHeightProperty().bind(actionOptionsDisplay.widthProperty());
		actionOptionsDisplay.setVisible(false);
		BorderPane wrap = new BorderPane();
		wrap.setCenter(actionOptionsDisplay);
		wrap.setPickOnBounds(false);
		this.getChildren().add(2, wrap);
	}
	
	/**
	 * Simply clears the board if passed "null."
	 * Throws an IllegalArgumentException if the preset has the wrong size.
	 * 
	 * Does not throw an exception if the board has too many/too few kings, that will get
	 * detected the next time "prepare move" is called. 
	 * @param preset
	 */
	private void setupPiecesFromPreset(BoardPreset preset) {
		if(preset == null) {
			clearPieces();
			return;
		}
		else if(preset.getBoardSize() != BOARD_SIZE) {
			throw new IllegalArgumentException("Board preset has the wrong size. Is " + preset.getBoardSize() +
					", should be " + BOARD_SIZE);
		}
		clearPieces(); //this sets all values in kingLocations to -1.
		
		for(int i = 0; i < BOARD_SIZE; i++) {
			for(int j = 0; j < BOARD_SIZE; j++) {
				board[i][j].setPiece(Piece.forName(preset.getPieceNameAt(i, j)));
				if(board[i][j].currentPiece instanceof King) {
					if(board[i][j].currentPiece.getColor() == Piece.WHITE) {
						if(kingLocations[0][0] == -1) {
							kingLocations[0][0] = i;
							kingLocations[0][1] = j;
						}
					}
					else {
						if(kingLocations[1][0] == -1) {
							kingLocations[1][0] = i;
							kingLocations[1][1] = j;
						}
					}
				}
			}
		}
	}

	private void clearPieces() {
		for(int i = 0; i < BOARD_SIZE; i++) {
			for(int j = 0; j < BOARD_SIZE; j++) {
				board[i][j].setPiece(null);
			}
		}
		kingLocations[0][0] = kingLocations[0][1] = kingLocations[1][0] = kingLocations[1][1] = -1;
	}

	/**
	 * Precondition: {@code kingLocations} holds accurate values if there is only 1 white and 1 black king on
	 * the board. If there are too many kings on the board, that will get detected regardless of kingLocations
	 * and endGameWithMessage(String) will be called.
	 * 
	 * BLOCKS until it is done.
	 * If it needs to be called from FX Thread, it Should ONLY be called through MovePreparer.prepare().
	 */
	private void prepareForNextMove() {
		/*
		System.out.println("------------it's move prep time, here are the pieces::");
		printPieces();
		System.out.println("--------------------");
		*/
		System.out.println("Starting prep!");
		long start_time = System.nanoTime();
		PREP:
		{
			boardInteractionAllowed = false;
			
			if(kingLocations[0][0] == -1 || kingLocations[0][1] == -1) {
				if(kingLocations[1][0] == -1 || kingLocations[1][1] == -1) {
					endGameWithMessage("Neither player has a king on the board.");
					break PREP;
				}
				else {
					endGameWithMessage("Black wins because white does not have a king.");
					break PREP;
				}
			}
			else if(kingLocations[1][0] == -1 || kingLocations[1][1] == -1) {
				endGameWithMessage("White wins because black does not have a king.");
				break PREP;
			}
			
			if(Settings.moveRuleEnabled()) {
				int moveRule = Settings.getMoveRule();
				//System.out.println("MOVE RULE ENABLED" + moveRule);
				if(movesSincePawnOrCapture >= moveRule) {
					endGameWithMessage("Draw by " + moveRule + "-move rule");
					break PREP;
				}
			} 
				
			System.out.println("prep 10% complete");
			boolean anyLegalMovesWhite = false;
			boolean anyLegalMovesBlack = false;
			int whiteKingCount = 0;
			int blackKingCount = 0;
			ArrayList<Tile> whitePieces = new ArrayList<>(BOARD_SIZE/2);
			ArrayList<Tile> blackPieces = new ArrayList<>(BOARD_SIZE/2);
			for(int i = 0; i < BOARD_SIZE; i++) {
				for(int j = 0; j < BOARD_SIZE; j++) {
					board[i][j].legalMovesShowing.clear();
					if(board[i][j].calculateLegalActions()) {
						if(board[i][j].currentPiece.getColor() == Piece.WHITE) {
							anyLegalMovesWhite = true;
						}
						else {
							anyLegalMovesBlack = true;
						}
					}
					Piece p;
					if((p = board[i][j].currentPiece) != null) {
						if(p.getColor() == Piece.WHITE) {
							whitePieces.add(board[i][j]);
							if(p instanceof King) { whiteKingCount++; }
						}
						else {
							blackPieces.add(board[i][j]);
							if(p instanceof King) { blackKingCount++; }
						}
					}
				}
			}
			
			if(whiteKingCount > 1 || blackKingCount > 1) {
				endGameWithMessage("There are too many kings on the board!");
				break PREP;
			}
			
			
			System.out.println("prep 99% complete");
			
			//Now check for insufficient material:
			if(Settings.getInsufficientMaterial()) {
				MATERIAL_CHECK:
				{
					int wk = 0, bk = 0, wb = 0, bb = 0; //White Knights, Black Knights, White Bishops, Black Bishops
					boolean wbc = false, bbc = false; //White Bishop Color, Black Bishop Color (as in light or dark square)
					for(int i = 0; i < whitePieces.size(); i++) {
						Piece p = whitePieces.get(i).getPiece();
						if(p instanceof King) {
							continue;
						}
						else if(p instanceof Knight) {
							wk++;
							if(wk > 2) {
								break MATERIAL_CHECK;
							}
						}
						else if(p instanceof Bishop) {
							wb++;
							if(wb > 1) {
								break MATERIAL_CHECK;
							}
							else {
								wbc = whitePieces.get(i).tileColor();
							}
						}
						else {
							break MATERIAL_CHECK;
						}
					}
					for(int i = 0; i < blackPieces.size(); i++) {
						Piece p = blackPieces.get(i).getPiece();
						if(p instanceof King) {
							continue;
						}
						else if(p instanceof Knight) {
							bk++;
							if(bk > 2) {
								break MATERIAL_CHECK;
							}
						}
						else if(p instanceof Bishop) {
							bb++;
							if(bb > 1) {
								break MATERIAL_CHECK;
							}
							else {
								bbc = blackPieces.get(i).tileColor();
							}
						}
						else {
							break MATERIAL_CHECK;
						}
					}
					/*
					 * ONLY the following conditions will cause an automatic draw:
					 * 
					 * King vs king with no other pieces.
					 * King and bishop vs king.
					 * King and knight vs king.
					 * King and bishop vs king and bishop of the same colored square.
					 * 
					 * Although there are other conditions where neither player can FORCE mate,
					 * those positions are not an automatic draw because a mate could still be
					 * achieved if one player "helps" the other.
					 * */
					 
					if(	wk == 0 && bk == 0 && wb == 0 && bb == 0 ||
						wk == 0 && bk == 0 && (wb == 1 ^ bb == 1) ||
						(wk == 0 ^ bk == 0) && wb == 0 && bb == 0 ||
						wk == 0 && bk == 0 && wb == 1 && bb == 1 && wbc == bbc) {
						endGame("material");
						break PREP;
					}
					else {
						break MATERIAL_CHECK;
					}
				}
			}	
			
			//Now check for checkmate/stalemate:
			
			if(Board.this.turn == Piece.WHITE) {
				if(!anyLegalMovesWhite) {
					boolean isCheckmate = false;
					OUTER:
					for(int i = 0; i < BOARD_SIZE; i++) {
						for(int j = 0; j < BOARD_SIZE; j++) {
							Piece p = board[i][j].currentPiece;
							if(p != null && p.getColor() == Piece.BLACK && p.canCheck(Board.this, i, j, kingLocations[0][0], kingLocations[0][1])){
								isCheckmate = true;
								break OUTER;
							}
						}
					}
					if(isCheckmate) {
						endGame("black");
						break PREP;
					}
					else {
						endGame("stalemate");
						break PREP;
					}
				}
			}
			else {
				if(!anyLegalMovesBlack) {
					boolean isCheckmate = false;
					OUTER:
					for(int i = 0; i < BOARD_SIZE; i++) {
						for(int j = 0; j < BOARD_SIZE; j++) {
							Piece p = board[i][j].currentPiece;
							if(p != null && p.getColor() == Piece.WHITE && p.canCheck(Board.this, i, j, kingLocations[1][0], kingLocations[1][1])){
								isCheckmate = true;
								break OUTER;
							}
						}
					}
					if(isCheckmate) {
						endGame("white");
						break PREP;
					}
					else {
						endGame("stalemate");
						break PREP;
					}
				}
			}
		}
		
		System.out.println("Prep took " + (System.nanoTime() - start_time));
		boardInteractionAllowed = true;
	}

	void flip() {
		Platform.runLater(new Runnable() {
			public void run() {
				boolean newOrientation = !Board.this.orientation;
				if(newOrientation == Piece.BLACK) {
					for(int i = 0; i < BOARD_SIZE; i++) {
						for(int j = 0; j < BOARD_SIZE; j++) {
							boardDisplay[i][j] = board[BOARD_SIZE - i - 1][BOARD_SIZE - j - 1];
						}
					}
				}
				else {
					for(int i = 0; i < BOARD_SIZE; i++) {
						for(int j = 0; j < BOARD_SIZE; j++) {
							boardDisplay[i][j] = board[i][j];
						}
					}
				}
				
				
				grid.getChildren().clear();
				for (int i = 0; i < BOARD_SIZE; i++) {
		            for (int j = 0; j < BOARD_SIZE; j++) {
		            	Tile child = boardDisplay[i][j];
		                GridPane.setRowIndex(child, i);
		                GridPane.setColumnIndex(child, j);
		                grid.getChildren().add(child);
		            }
		        }
				Board.this.orientation = newOrientation;
			}
		});
		
	}
	
	void flipTurn() {
		this.turn = !this.turn;
		if(currentlySelectedTile != null) {
			currentlySelectedTile.hideLegalMovesAndDepopulate();
			currentlySelectedTile.setHighlighted(false);
			currentlySelectedTile = null;
		}
		associatedGP.turnLabel.setText(turn == Piece.WHITE ? 
				GamePanel.WHITE_TO_MOVE_TEXT : GamePanel.BLACK_TO_MOVE_TEXT);
	}

	boolean tryMoveForLegality(int startRow, int startCol, LegalAction action) {
		Piece p = getPieceAt(startRow, startCol);
		boolean result;
		if(p == null) {
			throw new IllegalArgumentException("cannot move a nonexistent piece. (startRow, startCol) represents an empty square.");
		}
		else if(action instanceof LegalOtherMoveAndCapture) {
			LegalOtherMoveAndCapture a = (LegalOtherMoveAndCapture) action;
			System.out.println("LEGAL OTHER: " + a);
			result = tryMoveForLegalityMNC(p.getColor(), a.startRow(), a.startCol(), a.destRow(), a.destCol());
		}
		else if(action instanceof LegalMulti) {
			result = tryMultiForLegality(startRow, startCol, (LegalMulti) action);
		}
		else if(p instanceof King) {
			result = tryMoveForLegalityKing(startRow, startCol, action);
		}
		else {
			result = tryMoveForLegalityNonKing(startRow, startCol, action);
		}
		/*
		if(!result) {
			System.out.printf("Invalidated (%d,%d) -> %s%n", startRow, startCol, action.toString());
		}
		*/
		return result;
	}
	
	boolean tryMoveForLegalityMNC(boolean actingColor, int startRow, int startCol, int destRow, int destCol) {
		Piece p = getPieceAt(startRow, startCol);
		if(p == null) {
			throw new IllegalArgumentException("cannot move a nonexistent piece. (startRow, startCol) represents an empty square.");
		}
		else if(p instanceof King) {
			return tryMoveForLegalityMNCKing(actingColor, startRow, startCol, destRow, destCol);
		}
		else {
			return tryMoveForLegalityMNCNonKing(actingColor, startRow, startCol, destRow, destCol);
		}
	}
	
	private boolean tryMoveForLegalityMNCNonKing(boolean actingColor, int startRow, int startCol, int destRow, int destCol) {
		
		Piece p = getPieceAt(startRow, startCol);
		Piece onTile = getPieceAt(destRow, destCol);
		int[] kingSpot = actingColor == Piece.WHITE ? kingLocations[0] : kingLocations[1];
		boolean attackingColor = actingColor == Piece.WHITE ? Piece.BLACK : Piece.WHITE;
		
		boolean pieceMovingColor = p.getColor();
		
		board[startRow][startCol].currentPiece = null;
		board[destRow][destCol].currentPiece = p;
		boolean result = true;
		outer:
		for(int i = 0; i < BOARD_SIZE; i++) {
			for(int j = 0; j < BOARD_SIZE; j++) {
				//the check for "pieceMovingColor == attackingColor" ensures that the piece doesn't get skipped
				//if it could still potentially check the opponent's king.
				if(i == destRow && j == destCol && pieceMovingColor == actingColor) continue;
				Piece o = getPieceAt(i, j);
				if(	/*if this breaks down, it's because I removed the condition "!(o instanceof King)*/
					o != null && o.getColor() == attackingColor &&
					o.canCheck(Board.this, i, j, kingSpot[0], kingSpot[1])) {
					result = false;
					break outer;
				}
			}
		}
		
		board[startRow][startCol].currentPiece = p;
		board[destRow][destCol].currentPiece = onTile;
		
		return result;
	}
	
	private boolean tryMoveForLegalityMNCKing(boolean actingColor, int startRow, int startCol, int destRow, int destCol) {
		Piece p = getPieceAt(startRow, startCol);
		Piece onTile = getPieceAt(destRow, destCol);
		boolean pieceMovingColor = p.getColor();
		boolean attackingColor = actingColor == Piece.WHITE ? Piece.BLACK : Piece.WHITE;

		board[startRow][startCol].currentPiece = null;
		board[destRow][destCol].currentPiece = p;
		boolean result = true;
		
		final int[] mustNotBeCheckable;
		if(pieceMovingColor == actingColor) {
			/* *
			 * For example, if a white king is moving and it's white's turn, the piece that must
			 * not be checkable at the end is the white king - whose location is (destRow,destCol)
			 * during this method. Same thing for black
			 * 
			 * This will account for most of the calls to this method.
			 */
			mustNotBeCheckable = new int[] {destRow, destCol};
		}
		else {
			/* *
			 * However, if, for example, a black king is moving but it's white's turn
			 * (aka actingColor == Piece.WHITE), then the tile that must not be checkable
			 * is the CURRENT location of the white king.
			 * 
			 * This else block will only be entered if this method was called because of an
			 * OtherMoveAndCapture.
			 */
			mustNotBeCheckable = actingColor == Piece.WHITE ? kingLocations[0] : kingLocations[1];
		}
		outer:
		for(int i = 0; i < BOARD_SIZE; i++) {
			for(int j = 0; j < BOARD_SIZE; j++) {
				if(i == destRow && j == destCol) continue;
				Piece o = getPieceAt(i, j);
				if(	
					o != null && o.getColor() == attackingColor &&
					o.canCheck(Board.this, i, j, mustNotBeCheckable[0], mustNotBeCheckable[1])) {
					result = false;
					break outer;
				}
			}
		}
		
		board[startRow][startCol].currentPiece = p;
		board[destRow][destCol].currentPiece = onTile;
		
		return result;
	}

	/**
	 * PRECONDITION: rows & cols are valid, startRow/startCol represents a NON-KING piece.
	 * @param startRow
	 * @param startCol
	 * @param action
	 * @throws ArrayIndexOutOfBoundsException if startRow or startCol are out of bounds.
	 * @throws IllegalArgumentException if startRow/startCol holds a king Piece.
	 * @return
	 */
	private boolean tryMoveForLegalityNonKing(int startRow, int startCol, LegalAction action) {
		Piece p = getPieceAt(startRow, startCol);
		if(action instanceof LegalMoveAndCapture) {
			return tryMoveForLegalityMNCNonKing(p.getColor(), startRow, startCol, action.destRow(), action.destCol());
		}
		else if(action instanceof LegalOtherMoveAndCapture) {
			LegalOtherMoveAndCapture act = (LegalOtherMoveAndCapture) action;
			return tryMoveForLegalityMNC(p.getColor(), act.startRow(), act.startCol(), act.destRow(), act.destCol());
		}
		int destRow = action.destRow(), destCol = action.destCol();
		
		if(p instanceof King) {
			throw new IllegalArgumentException("Piece on startRow/startCol is a king");
		}
		Piece onTile = getPieceAt(destRow, destCol);
		int[] kingSpot = p.getColor() == Piece.WHITE ? kingLocations[0] : kingLocations[1];
		boolean myColor = p.getColor();
		boolean attackingColor = myColor == Piece.WHITE ? Piece.BLACK : Piece.WHITE;
		
		
		if(action instanceof LegalCapture) {
			board[destRow][destCol].currentPiece = null;
			boolean result = true;
			
			outer:
			for(int i = 0; i < BOARD_SIZE; i++) {
				for(int j = 0; j < BOARD_SIZE; j++) {
					if(i == destRow && j == destCol) continue;
					Piece o = getPieceAt(i, j);
					if(	/*if this breaks down, it's because I removed the condition "!(o instanceof King)*/
						o != null && o.getColor() == attackingColor &&
						o.canCheck(Board.this, i, j, kingSpot[0], kingSpot[1])) {
						result = false;
						break outer;
					}
				}
			}
			
			board[destRow][destCol].currentPiece = onTile;
			
			//System.out.println(result);
			return result;
		}
		else if(action instanceof LegalPromotion) {
			LegalPromotion pro = (LegalPromotion) action;
			
			boolean anyValid = false;
			piece_list_loop:
			for(Iterator<String> itr = pro.getOptions().iterator(); itr.hasNext();) {
				String pieceName = itr.next();
				if(!Piece.isNameOfPiece(pieceName)) {
					itr.remove();
					continue piece_list_loop;
				}
				board[startRow][startCol].currentPiece = Piece.forName(pieceName, myColor);
				boolean result = true;
				outer:
				for(int i = 0; i < BOARD_SIZE; i++) {
					for(int j = 0; j < BOARD_SIZE; j++) {
						if(i == destRow && j == destCol) continue;
						Piece o = getPieceAt(i, j);
						if(	/*if this breaks down, it's because I removed the condition "!(o instanceof King)*/
							o != null && o.getColor() == attackingColor &&
							o.canCheck(Board.this, i, j, kingSpot[0], kingSpot[1])) {
							result = false;
							break outer;
						}
					}
				}
				if(result) {
					anyValid = true;
				}
				else {
					itr.remove();
				}
			}
			
			board[startRow][startCol].currentPiece = p;
			
			if(!anyValid) {
				return false;
			}
			else {
				return true;
			}
		}
		else if(action instanceof LegalSummon) {
			LegalSummon sum = (LegalSummon) action;
			boolean anyValid = false;
			piece_list_loop:
			for(Iterator<String> itr = sum.getOptions().iterator(); itr.hasNext();) {
				String pieceName = itr.next();
				if(!Piece.isNameOfPiece(pieceName)) {
					itr.remove();
					continue piece_list_loop;
				}
				board[destRow][destCol].currentPiece = Piece.forName(pieceName, myColor);
				boolean result = true;
				outer:
				for(int i = 0; i < BOARD_SIZE; i++) {
					for(int j = 0; j < BOARD_SIZE; j++) {
						if(i == destRow && j == destCol) continue;
						Piece o = getPieceAt(i, j);
						if(	/*if this breaks down, it's because I removed the condition "!(o instanceof King)*/
							o != null && o.getColor() == attackingColor &&
							o.canCheck(Board.this, i, j, kingSpot[0], kingSpot[1])) {
							result = false;
							break outer;
						}
					}
				}
				if(result) {
					anyValid = true;
				}
				else {
					itr.remove();
				}
			}
			
			board[destRow][destCol].currentPiece = onTile;
			
			if(!anyValid) {
				return false;
			}
			else {
				return true;
			}
		}
		else {
			throw new IllegalArgumentException(action.getClass().getName() + " is not supported by tryMoveForLegalityNonKing");
		}
	}

	/**
	 * Precondition: All rows and columns are valid, startRow/startCol represents the location of a king.
	 */
	private boolean tryMoveForLegalityKing(int startRow, int startCol, LegalAction action) {
		int destRow = action.destRow(), destCol = action.destCol();
		Piece p = getPieceAt(startRow, startCol);
		Piece onTile = getPieceAt(destRow, destCol);
		boolean attackingColor = p.getColor() == Piece.WHITE ? Piece.BLACK : Piece.WHITE;

		if(action instanceof LegalMoveAndCapture) {
			board[startRow][startCol].currentPiece = null;
			board[destRow][destCol].currentPiece = p;
			boolean result = true;
			
			outer:
			for(int i = 0; i < BOARD_SIZE; i++) {
				for(int j = 0; j < BOARD_SIZE; j++) {
					if(i == destRow && j == destCol) continue;
					Piece o = getPieceAt(i, j);
					if(	
						o != null && o.getColor() == attackingColor &&
						o.canCheck(Board.this, i, j, destRow, destCol)) {
						result = false;
						break outer;
					}
				}
			}
			
			board[startRow][startCol].currentPiece = p;
			board[destRow][destCol].currentPiece = onTile;
			
			return result;
		}
		else if(action instanceof LegalCapture) {
			throw new IllegalArgumentException("LegalCapture actions cannot be checked for legality yet.");
		}
		else if(action instanceof LegalPromotion) {
			throw new IllegalArgumentException("Oof! The king can't have a promotion action!");
		}
		else if(action instanceof LegalSummon) {
			throw new IllegalArgumentException("LegalSummon actions cannot be checked for legality yet.");
		}
		else if(action instanceof LegalMulti) {
			throw new IllegalArgumentException("LegalMulti actions cannot be checked for legality yet.");
		}
		else {
			throw new IllegalArgumentException(action.getClass().getName() + " is not supported by tryMoveForLegalityKing");
		}
	}
	
	private boolean tryMultiForLegality(int startRow, int startCol, LegalMulti action) {
		return tryMultiForLegality(startRow, startCol, action, 0, this.board[startRow][startCol].currentPiece.getColor());
	}
	
	private boolean tryMultiForLegality(int startRow, int startCol, LegalMulti action, int index, boolean color) {
		if(index == action.getActions().size()) {
			
			boolean result = isCurrentStateLegalFor(color);
			if(!result) {
				System.out.println("\n\nILLEGAL: ");
				printPieces();
				System.out.println("\n\n");
			}
			return result;
		}
		LegalAction current = action.getActions().get(index);
		boolean result = false;
		if(current instanceof LegalMoveAndCapture) {
			//setup:
			LegalMoveAndCapture mnc = (LegalMoveAndCapture) current;
			Piece p = this.board[startRow][startCol].currentPiece;
			Piece onTile = this.board[mnc.destRow()][mnc.destCol()].currentPiece;
			this.board[startRow][startCol].currentPiece = null;
			this.board[mnc.destRow()][mnc.destCol].currentPiece = p;
			
			//recurse:
			result = tryMultiForLegality(startRow, startCol, action, index + 1, color);
			
			//reset:
			this.board[startRow][startCol].currentPiece = p;
			this.board[mnc.destRow()][mnc.destCol()].currentPiece = onTile;
			
			return result;
		}
		else if(current instanceof LegalOtherMoveAndCapture) {
			//setup:
			LegalOtherMoveAndCapture omnc = (LegalOtherMoveAndCapture) current;
			Piece p = this.board[omnc.startRow()][omnc.startCol()].currentPiece;
			Piece onTile = this.board[omnc.destRow()][omnc.destCol()].currentPiece;
			this.board[omnc.startRow()][omnc.startCol()].currentPiece = null;
			this.board[omnc.destRow()][omnc.destCol()].currentPiece = p;
			
			//recurse:
			result = tryMultiForLegality(startRow, startCol, action, index + 1, color);
			
			//reset:
			this.board[omnc.startRow()][omnc.startCol()].currentPiece = p;
			this.board[omnc.destRow()][omnc.destCol()].currentPiece = onTile;
			
			return result;
		}
		else if(current instanceof LegalCapture) {
			//setup:
			LegalCapture cap = (LegalCapture) current;
			Piece onTile = this.board[cap.destRow()][cap.destCol()].currentPiece;
			this.board[cap.destRow()][cap.destCol()].currentPiece = null;
			
			//recurse:
			result = tryMultiForLegality(startRow, startCol, action, index + 1, color);
			
			//setup:
			this.board[cap.destRow()][cap.destCol()].currentPiece = onTile;
			
			return result;
		}
		else if(current instanceof LegalPromotion) {
			LegalPromotion pro = (LegalPromotion) current;
			Piece onTile = this.board[startRow][startCol].currentPiece;
			boolean anyValid = false;
			for(Iterator<String> itr = pro.getOptions().iterator(); itr.hasNext();) {
				String pieceName = itr.next();
				this.board[startRow][startCol].currentPiece = Piece.forName(pieceName, color);
				result = tryMultiForLegality(startRow, startCol, action, index + 1, color);
				if(result) {
					anyValid = true;
				}
				else {
					itr.remove();
				}
			}
			
			this.board[startRow][startCol].currentPiece = onTile;
			
			return anyValid;
		}
		else if(current instanceof LegalSummon) {
			LegalSummon pro = (LegalSummon) current;
			Piece onTile = this.board[startRow][startCol].currentPiece;
			boolean anyValid = false;
			for(Iterator<String> itr = pro.getOptions().iterator(); itr.hasNext();) {
				String pieceName = itr.next();
				this.board[pro.row()][pro.col()].currentPiece = Piece.forName(pieceName, color);
				result = tryMultiForLegality(startRow, startCol, action, index + 1, color);
				if(result) {
					anyValid = true;
				}
				else {
					itr.remove();
				}
			}
			
			this.board[startRow][startCol].currentPiece = onTile;
			
			return anyValid;
		}
		else {
			throw new IllegalArgumentException(current.getClass().getName() + " actions are not supported in multis yet.");
		}
		
	}
	
	
	/**
	 *  This method will find the current locations of the kings and so DOES NOT REQUIRE KINGLOCATIONS TO BE ACCURATE.
	 * */
	private boolean isCurrentStateLegalFor(boolean color) {
		int[] kings = findKings()[color == Piece.WHITE ? 0 : 1];
		for(int i = 0; i < BOARD_SIZE; i++) {
			for(int j = 0; j < BOARD_SIZE; j++) {
				Piece o = getPieceAt(i, j);
				if(o != null) {
					if(o.getColor() != color) {
						if(o.canCheck(this, i, j, kings[0], kings[1])) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}
	
	private int[][] findKings(){
		int[][] kings = new int[2][2];
		boolean whiteFound = false, blackFound = false;
		for(int i = 0; i < BOARD_SIZE; i++) {
			for(int j = 0; j < BOARD_SIZE; j++) {
				Piece o = getPieceAt(i, j);
				if(o instanceof King) {
					if(o.getColor() == Piece.WHITE) {
						kings[0][0] = i;
						kings[0][1] = j;
						whiteFound = true;
					}
					else {
						kings[1][0] = i;
						kings[1][1] = j;
						blackFound = true;
					}
				}
			}
		}
		if(!whiteFound) {
			kings[0][0] = kings[0][1] = -1;
		}
		if(!blackFound) {
			kings[1][0] = kings[1][1] = -1;
		}
		
		return kings;
	}

	private void endGame(String result) {
		System.out.println("ending game, arg passed = " + result);
		Platform.runLater(new Runnable() {
			public void run() {
				switch(result) {
				case "white": boardPopupMessage.setText("White wins by checkmate"); break;
				case "black": boardPopupMessage.setText("Black wins by checkmate"); break;
				case "stalemate": boardPopupMessage.setText("Draw by stalemate"); break;
				case "material": boardPopupMessage.setText("Draw by insufficient material"); break;
				default: boardPopupMessage.setText("???"); break;
				}
				boardOverlay.setCenter(boardPopupBox);
				boardOverlay.setVisible(true);
			}
		});
	}
	
	private void endGameWithMessage(String message) {
		Platform.runLater(new Runnable() {
			public void run() {
				boardPopupMessage.setText(message);
				boardOverlay.setCenter(boardPopupBox);
				boardOverlay.setVisible(true);
			}
		});
	}
	
	/**
	 * MUST BE CALLED FROM FX THREAD.
	 * */
	public void reset() {
		clickHandler.cancel();
		synchronized(board) {
			if(currentlySelectedTile != null) {
				currentlySelectedTile.hideLegalMovesAndDepopulate();
				currentlySelectedTile.setHighlighted(false);
				currentlySelectedTile = null;
			}
			setupPiecesFromPreset(preset); //this will just clear the board if preset is null - no null check needed.
			Board.this.turn = preset == null ? Piece.WHITE : preset.getTurn();
			Board.this.log.clear();
			moveNumber = 1;
			playNumber = 1;
			movesSincePawnOrCapture = 0;
			actionOptionsDisplay.setVisible(false);
			boardOverlay.setCenter(null);
			boardOverlay.setVisible(false);
			movePreparerForFXThread.prepare();
			setOrientation(turn);
		}
	}
	
	public void setOrientation(boolean newOrientation) {
		if(orientation != newOrientation) {
			flip(); //flip updates the orientation for us
		}
	}

	public Piece setPieceAt(int row, int col, Piece p) {
		return board[row][col].setPiece(p);
	}

	public boolean inBounds(int row, int col) {
		return row >= 0 && col >= 0 && row < BOARD_SIZE && col < BOARD_SIZE;
	}
	
	@AFC(name="board size")
	public int getBoardSize() {
		return BOARD_SIZE;
	}
	
	public boolean getBoardOrientation() {
		return orientation;
	}
	
	/**
	 * Returns the piece on the tile indicated by row/col.
	 * @param row the row of the tile
	 * @param col the column of the tile
	 * @throws ArrayIndexOutOfBoundsException if row/col is out of bounds for this board
	 * ({@code row < 0 || row >= getBoardSize() || col < 0 || col >= getBoardSize()})
	 * @return the piece on the Tile indicated by row/col, or null if that tile is empty.
	 */
	public Piece getPieceAt(int row, int col) {
		return board[row][col].currentPiece;
	}
	
	public Tile getTileAt(int row, int col) {
		return board[row][col];
	}
	
	public GamePanel gamePanel() {
		return associatedGP;
	}
	
	public boolean getTurn() {
		return turn;
	}
	
	public void updateKingLocations() {
		this.kingLocations = findKings();
	}
	
	public void clearBoard() {
		for(int i = 0; i < BOARD_SIZE; i++) {
			for(int j = 0; j < BOARD_SIZE; j++) {
				board[i][j].setPiece(null);
			}
		}
	}
	
	public void printPieces() {
		for(int i = 0; i < BOARD_SIZE; i++) {
			for(int j = 0; j < BOARD_SIZE; j++) {
				System.out.print(board[i][j].currentPiece);
			}
			System.out.println();
		}
	}
}
