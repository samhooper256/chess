package chess.base;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.Executor;

import chess.util.ReadOnlyIntAttribute;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;

/* *
 * @author Sam Hooper
 */
public class Board extends StackPane{
	public static final int MAX_BOARD_SIZE = 26;
	public static final int DEFAULT_BOARD_SIZE = 8;
	public static boolean QUEENSIDE = true;
	public static boolean KINGSIDE = false;
	private static BoardPreset defaultBoardPreset;
	
	private final ReadOnlyIntAttribute BOARD_SIZE;
	private Tile[][] board;
	private int moveNumber, playNumber, movesSincePawnOrCapture;
	private LinkedList<BoardPlay> moveList;
	private int[][] kingLocations; //kingLocations[0] is white king, kingLocations[1] is black king
	private Tile currentlySelectedTile;
	private BoardPreset preset;
	private boolean turn, orientation, fiftyMoveRule;
	private volatile boolean boardInteractionAllowed = true;
	//UI Components
	private BorderPane boardOverlay;
	private StackPane boardPopupBox, promotionStackPane;
	private GridPane grid, promotionGridPane, popupMessageArea;
	private AnchorPane popupMessageOverlay;
	private Text boardPopupMessage, popupMessageXButton, popupGameOverText;
	private Button popupResetButton, popupViewBoardButton;
	private Tile[][] boardDisplay;
	private ScrollPane promotionScrollPane;
	private ActionOptionsDisplay actionOptionsDisplay;
	
	private Lock lock2 = new ReentrantLock();
	
	private static String LIGHT_COLOR = "#1abb9a";
	private static String DARK_COLOR = "#2c3d4f";
	private static String LIGHT_COLOR_SELECTED = "#38e8c4";
	private static String DARK_COLOR_SELECTED = "#45576b";
	private static String LEGAL_MOVE_COLOR = "#000dff";
	
	static {
		defaultBoardPreset = new BoardPreset(DEFAULT_BOARD_SIZE);
		defaultBoardPreset.setTurn(Piece.WHITE);
		defaultBoardPreset.setFiftyMoveRule(true);
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
							System.out.println("BIT = false :: from CH");
							boardInteractionAllowed = false;
							final int row = currentlySelectedTile.row;
							final int col = currentlySelectedTile.col;
							final Set<LegalAction> plays = new HashSet<>(source.legalMovesShowing);
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									System.out.println("\tAttempting to sync from CH (on FX Thread)");
									synchronized(lock1) {
										System.out.println("\t\tSuccessfully synced from CH (on FX Thread)");
										currentlySelectedTile.hideLegalMovesAndDepopulate();
										currentlySelectedTile.setHighlighted(false);
										currentlySelectedTile = null;
										System.out.println("\t\tnotifying all CH (on FX Thread)");
										lock1.notifyAll();
										flag = true;
									}
								}
							});
							
							System.out.println(">>> attempting to sync on lock1; " + Thread.currentThread().getName());
							synchronized(lock1) {
								while(!flag) {
									System.out.println("\t>>> starting wait on lock1; " + Thread.currentThread().getName());
									try {
										lock1.wait();
									} catch (InterruptedException e) {}						
								}
							}
							System.out.println("\t>>> done waiting on lock1; " + Thread.currentThread().getName());	
							
							System.out.println("----------starting select-and-making");
							Board.this.moveMaker.selectAndMake(row, col, plays); //TODO
							System.out.println("----------done select-and-making");
							
							System.out.println("BIT = true :: from CH");
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
										if(currentlySelectedTile == null) {
											//do nothing
										}
										else {
											currentlySelectedTile.hideLegalMovesAndDepopulate();
											currentlySelectedTile.setHighlighted(false);
											currentlySelectedTile = null;
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
						else {
							throw new IllegalArgumentException("Illegal click - Sam exposed!?!?!?");
						}
					}
					catch(Throwable t) {
						t.printStackTrace();
					}
					finally {
						lock2.unlock();
					}
					return null;
					
				}
				
			};
		}
		
	}
	
	private ClickHandler clickHandler = new ClickHandler();
	
	private class MovePreparer extends Service<Void>{
		
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
					prepareForNextMove();
					return null;
				}
				
			};
		}
		
	}
	
	private MovePreparer movePreparerForFXThread = new MovePreparer();
	
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
		/* *
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
			try {
				Piece piece = Board.this.getPieceAt(startRow, startCol);
				if(options.size() == 1) {
					synchronized(Board.this.board) {
						end = options.iterator().next();
						end.handle(startRow, startCol, Board.this);
					}
				}
				else {
					end = selectAction(options);
					end.handle(startRow, startCol, Board.this);
				}
				synchronized(kingLocations) {
					kingLocations = findKings();
				}
				log.log(startRow, startCol, piece, end);
				wrapUpPlay();
			}
			catch(Throwable t) {
				t.printStackTrace();
			}
			finally {
				playLock.unlock();
			}
			
		}
		
		
		public void makePlay(int startRow, int startCol, LegalCapture play, boolean wrap) {
			int destRow = play.destRow();
			int destCol = play.destCol();
			
			Platform.runLater(new Runnable() {
		
				@Override
				public void run() {
					// TODO Auto-generated method stub
					setPieceAt(destRow, destCol, null);
				}
				
			});
			
			
		}
		
		private void wrapUpPlay(){
			if(turn == Piece.WHITE) {
				turn = Piece.BLACK;
			}
			else {
				turn = Piece.WHITE;
				moveNumber++;
				movesSincePawnOrCapture = 0;
			}
			
			playNumber++;
			
			prepareForNextMove();
		}
		private void wrapUpPlay(BoardPlay theMove) {
			throw new UnsupportedOperationException("incomplete methdod (what even is this?)");
			/*
			moveList.add(theMove);
			System.out.println("Wrapping up move, incoming turn = " + turn);
			if(turn == Piece.WHITE) {
				turn = Piece.BLACK;
			}
			else {
				turn = Piece.WHITE;
				moveNumber++;
				if(fiftyMoveRule) {
					if(!(theMove.moved instanceof Pawn || theMove.captured != null ||
						moveList.get(moveList.size() - 2).moved instanceof Pawn ||
						moveList.get(moveList.size() - 2).captured != null)) {
						movesSincePawnOrCapture++;
					}
					else {
						movesSincePawnOrCapture = 0;
					}
				}
			}
			
			playNumber++;
				
			grid.requestLayout();
			
			movePreparerForFXThread.prepare();
			*/
		}
	}
	
	protected MoveMaker moveMaker = new MoveMaker();
	
	private EventHandler<? super MouseEvent> tileClickAction = event -> {
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
		
		
		
	};
	
	private EventHandler<? super MouseEvent> popupMessageXButtonClickAction = event -> {
		boardOverlay.setCenter(null);
		boardOverlay.setVisible(false);
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
		private boolean selected;
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
			selected = false;
			isShowingLegal = false;
			currentPiece = null;
			currentImageView = null;
			colorBox = new ColorBox();
			this.getChildren().add(colorBox);
			this.setStyle("-fx-background-color: " + ((row+col) % 2 == 0 ? LIGHT_COLOR : DARK_COLOR) + ";");
	        this.setOnMouseClicked(tileClickAction);
		}
		public void showLegalMovesAndPopulate() {
			if(legalActions != null) {
				for(LegalAction a : legalActions) {
					board[a.row()][a.col()].legalMovesShowing.add(a);
					board[a.row()][a.col()].showLegalIndicators();
				}
			}
		}
		
		private Set<LegalAction> getLegalActions() {
			return legalActions;
		}
		
		void printDetailedData(PrintStream out){
			out.printf("---------------%nDetailed Data for Tile at (%d, %d) :%n", row, col);
			out.printf("Piece >> %s%n", currentPiece);
			out.printf("legalMovesShowing >> %s%n", legalMovesShowing);
			out.printf("legalActions >> %s%n", legalActions);
			out.printf("---------------%n");
		}
		/* *
		 * returns true iff one or more legal moves are found, false otherwise.
		 */
		private boolean calculateLegalActions() {
			this.legalActions = currentPiece == null ? null : currentPiece.getLegalActions(Board.this, row, col);
			
			return this.legalActions != null && this.legalActions.size() > 0;
			
		}
		
		/* *
		 * Interacts with scene graph. MUST BE CALLED FROM FX APPLICATION THREAD.
		 */
		private Piece setPiece(Piece p) {
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
		
		/* *
		 * Interacts with scene graph. MUST BE CALLED FROM FX APPLICATION THREAD.
		 */
		private void setHighlighted(boolean value) {
			if(value == isHighlighted) {
				return;
			}
			else {
				isHighlighted = value;
			}
			
			// TODO Auto-generated method stub
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
				colorBox.addIndicatorWithoutRefreshing(la.getIndicator(8));
			}
			colorBox.refresh();
			isShowingLegal = true;
			
		}
		
		private boolean isShowingLegal() {
			return isShowingLegal;
		}
		
		public boolean isOccupied() {
			return currentPiece != null;
		}
		
		public boolean isEmpty() {
			return currentPiece == null;
		}
		
		public Piece getPiece() {
			return currentPiece;
		}
		public String toString() {
			return String.format("[Tile %d,%d]", row,col);
		}
		
		public boolean isCheckableBy(boolean myColor) {
			boolean attackingColor = !myColor;
			for(int i = 0; i < BOARD_SIZE.getAsInt(); i++) {
				for(int j = 0; j < BOARD_SIZE.getAsInt(); j++) {
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
		
		public int enemyLineDistance(boolean color) {
			return color == Piece.WHITE ? row + 1 : BOARD_SIZE.getAsInt() - row;
		}
		
	}
	
	public class BoardLog{
		private List<BoardPlay> logList;
		public BoardLog() {
			logList = new ArrayList<>();
		}
		
		public int playCount() {
			return logList.size();
		}
		public BoardPlay last() {
			return logList.get(logList.size() - 1);
		}
		
		public BoardPlay log(int startRow, int startCol, Piece piece, LegalAction action) {
			if(action == null) {
				throw new NullPointerException("cannot log a null action");
			}
			BoardPlay play = BoardPlay.of(startRow, startCol, piece, action);
			logList.add(play);
			return play;
		}
	}
	
	private BoardLog log = new BoardLog();
	
	public BoardLog getLog() {
		return log;
	}
	
	public BoardPlay lastPlay() {
		return log.last();
	}
	
	public boolean hasPlay() {
		return log.playCount() > 0;
	}
	
	public static Board emptyBoard() {
		return new Board(DEFAULT_BOARD_SIZE);
	}
	
	public static Board emptyBoard(int size) {
		return new Board(size);
	}
	
	public static Board defaultBoard() {
		return new Board(defaultBoardPreset);
	}
	
	public static Board fromPreset(BoardPreset preset) {
		return new Board(preset);
	}
	
	private Board(int size) {
		if(size > MAX_BOARD_SIZE) {
			throw new IllegalArgumentException("Desired board size (" + size + ") is greater " + 
			"than MAX_BOARD_SIZE (26)");
		}
		
		BOARD_SIZE = new ReadOnlyIntAttribute(size);
		turn = Piece.WHITE;
		fiftyMoveRule = true;
		this.preset = null;
		
		finishBoardInit();
		movePreparerForFXThread.prepare();
	}
	
	private Board(BoardPreset preset) {
		if(preset.getBoardSizeAsInt() > MAX_BOARD_SIZE) {
			throw new IllegalArgumentException("Desired board size (" + preset.getBoardSizeAsInt() + ") is greater " + 
			"than MAX_BOARD_SIZE (26)");
		}
		
		BOARD_SIZE = new ReadOnlyIntAttribute(preset.getBoardSizeAsInt());
		turn = preset.getTurn();
		fiftyMoveRule = preset.getFiftyMoveRule();
		this.preset = preset;
		
		finishBoardInit();
		setupPiecesFromPreset(preset);
		movePreparerForFXThread.prepare();
		
		//TODO DELETE EVERYTHING BELOW THIS LINE (IN THIS CONSTRUCTOR)

		Thread reader = new Thread(new Runnable() {

			@Override
			public void run() {
				Scanner in = new Scanner(System.in);
				while(true) {
					String line = in.nextLine().trim();
					if(line.startsWith("@")) {
						int index = line.indexOf(',');
						int row = Integer.parseInt(line.substring(1, index));
						int col = Integer.parseInt(line.substring(index + 1));
						board[row][col].printDetailedData(System.out);
					}
				}
			}
			
		});
		reader.setDaemon(true);
		reader.start();
	}
	
	private void finishBoardInit() {
		board = new Tile[BOARD_SIZE.getAsInt()][BOARD_SIZE.getAsInt()];
		boardDisplay = new Tile[BOARD_SIZE.getAsInt()][BOARD_SIZE.getAsInt()];
		kingLocations = new int[][] {{-1,-1},{-1,-1}};
		orientation = Piece.WHITE;
		moveList = new LinkedList<>();
		moveNumber = 1;
		playNumber = 1;
		movesSincePawnOrCapture = 0;
		initGUI();
	}

	private void initGUI() {
		//Setup board tiles:
		grid = new GridPane();
		grid.setMinSize(200, 200);
		
		for (int i = 0; i < BOARD_SIZE.getAsInt(); i++) {
			final ColumnConstraints columnConstraints = new ColumnConstraints(Control.USE_PREF_SIZE, Control.USE_COMPUTED_SIZE, Double.MAX_VALUE);
            columnConstraints.setPercentWidth(100.0/BOARD_SIZE.getAsInt());
            grid.getColumnConstraints().add(columnConstraints);
            
            final RowConstraints rowConstraints = new RowConstraints(Control.USE_PREF_SIZE, Control.USE_COMPUTED_SIZE, Double.MAX_VALUE);
            rowConstraints.setPercentHeight(100.0/BOARD_SIZE.getAsInt());
            grid.getRowConstraints().add(rowConstraints);
        }
		
		
		
		for (int i = 0; i < BOARD_SIZE.getAsInt(); i++) {
            for (int j = 0; j < BOARD_SIZE.getAsInt(); j++) {
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
			row2.setPercentHeight(25);
			RowConstraints row3 = new RowConstraints();
			row3.setValignment(VPos.CENTER);
			row3.setPercentHeight(50);
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
		
		boardPopupMessage = new Text();
		boardPopupMessage.setId("popup-message");
		
		popupGameOverText = new Text("Game Over");
		popupGameOverText.setId("game-over-text");
		
		popupResetButton = new Button("Reset Board");
		popupResetButton.getStyleClass().add("popup-button");
		popupResetButton.setPrefSize(100, 50);
		popupResetButton.setOnMouseClicked(event -> {
			setupPiecesFromPreset(preset);
			this.turn = preset.getTurn();
			moveList.clear();
			moveNumber = 1;
			playNumber = 1;
			movesSincePawnOrCapture = 0;
			boardOverlay.setCenter(null);
			boardOverlay.setVisible(false);
			movePreparerForFXThread.prepare();
		});
		
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
	
	private void setupPiecesFromPreset(BoardPreset preset) {
		clearPieces(); //this sets all values in kingLocations to -1.
		for(int i = 0; i < BOARD_SIZE.getAsInt(); i++) {
			for(int j = 0; j < BOARD_SIZE.getAsInt(); j++) {
				board[i][j].setPiece(Piece.forName(preset.getPieceNameAt(i, j)));
				if(board[i][j].currentPiece instanceof King) {
					if(board[i][j].currentPiece.getColor() == Piece.WHITE) {
						if(kingLocations[0][0] == -1) {
							kingLocations[0][0] = i;
							kingLocations[0][1] = j;
						}
						else {
							throw new IllegalArgumentException("Preset has too many white kings!");
						}
					}
					else {
						if(kingLocations[1][0] == -1) {
							kingLocations[1][0] = i;
							kingLocations[1][1] = j;
						}
						else {
							throw new IllegalArgumentException("Preset has too many black kings!");
						}
					}
				}
			}
		}
		
		if(kingLocations[0][0] == -1) {
			throw new IllegalArgumentException("Preset doesn't have a white king!");
		}
		if(kingLocations[1][0] == -1) {
			throw new IllegalArgumentException("Preset doesn't have a black king!");
		}
	}

	private void clearPieces() {
		for(int i = 0; i < BOARD_SIZE.getAsInt(); i++) {
			for(int j = 0; j < BOARD_SIZE.getAsInt(); j++) {
				board[i][j].setPiece(null);
			}
		}
		kingLocations[0][0] = kingLocations[0][1] = kingLocations[1][0] = kingLocations[1][1] = -1;
	}

	/* *
	 * Precondition: kingLocations holds accurate values.
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
		System.out.println("BIT = false :: from PFNM");
		
		boardInteractionAllowed = false;
		
		System.out.println("starting prep");
		if(fiftyMoveRule && movesSincePawnOrCapture >= 50) {
			endGame("fifty");
			System.out.println("BIT = true :: from PFNM");
			boardInteractionAllowed = true;
			return;
		}
		System.out.println("prep 10% complete");
		boolean anyLegalMovesWhite = false;
		boolean anyLegalMovesBlack = false;
		ArrayList<Piece> whitePieces = new ArrayList<>(BOARD_SIZE.getAsInt()/2);
		ArrayList<Piece> blackPieces = new ArrayList<>(BOARD_SIZE.getAsInt()/2);
		for(int i = 0; i < BOARD_SIZE.getAsInt(); i++) {
			for(int j = 0; j < BOARD_SIZE.getAsInt(); j++) {
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
						whitePieces.add(p);
					}
					else {
						blackPieces.add(p);
					}
				}
			}
		}
		
		
		System.out.println("prep 99% complete");
		//Now check for insufficient material:
		
		/*//TODO UNCOMMENT
				MATERIAL_CHECK:
				{
					int wk = 0, bk = 0, wb = 0, bb = 0; //White Knights, Black Knights, White Bishops, Black Bishops
					boolean wbc = false, bbc = false; //White Bishop Color, Black Bishop Color
					for(int i = 0; i < whitePieces.size(); i++) {
						Piece p = whitePieces.get(i);
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
								wbc = p.getColor();
							}
						}
						else {
							break MATERIAL_CHECK;
						}
					}
					for(int i = 0; i < blackPieces.size(); i++) {
						Piece p = blackPieces.get(i);
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
								bbc = p.getColor();
							}
						}
						else {
							break MATERIAL_CHECK;
						}
					}
					
//					 * ONLY the following conditions will cause an automatic draw:
//					 * 
//					 * King vs king with no other pieces.
//					 * King and bishop vs king.
//					 * King and knight vs king.
//					 * King and bishop vs king and bishop of the same coloured square.
//					 * 
//					 * Although there are other conditions where neither player can FORCE mate,
//					 * those positions are not an automatic draw because a mate could still be
//					 * achieved if one player "helps" the other.
					 
					if(	wk == 0 && bk == 0 && wb == 0 && bb == 0 ||
						wk == 0 && bk == 0 && (wb == 1 ^ bb == 1) ||
						(wk == 0 ^ bk == 0) && wb == 0 && bb == 0 ||
						wk == 0 && bk == 0 && wb == 1 && bb == 1 && wbc == bbc) {
						endGame("material");
					}
					else {
						break MATERIAL_CHECK;
					}
				}
				*/
		
		//Now check for checkmate/stalemate:
		
		if(Board.this.turn == Piece.WHITE) {
			if(!anyLegalMovesWhite) {
				boolean isCheckmate = false;
				OUTER:
				for(int i = 0; i < BOARD_SIZE.getAsInt(); i++) {
					for(int j = 0; j < BOARD_SIZE.getAsInt(); j++) {
						Piece p = board[i][j].currentPiece;
						if(p != null && p.getColor() == Piece.BLACK && p.canCheck(Board.this, i, j, kingLocations[0][0], kingLocations[0][1])){
							isCheckmate = true;
							break OUTER;
						}
					}
				}
				if(isCheckmate) {
					endGame("black");
				}
				else {
					endGame("stalemate");
				}
			}
		}
		else {
			if(!anyLegalMovesBlack) {
				boolean isCheckmate = false;
				OUTER:
				for(int i = 0; i < BOARD_SIZE.getAsInt(); i++) {
					for(int j = 0; j < BOARD_SIZE.getAsInt(); j++) {
						Piece p = board[i][j].currentPiece;
						if(p != null && p.getColor() == Piece.WHITE && p.canCheck(Board.this, i, j, kingLocations[1][0], kingLocations[1][1])){
							isCheckmate = true;
							break OUTER;
						}
					}
				}
				if(isCheckmate) {
					endGame("white");
				}
				else {
					endGame("stalemate");
				}
			}
		}
		System.out.println("BIT = true :: from PFNM");
		boardInteractionAllowed = true;
	}
	
	private void undoMove() {
		//TODO
	}

	private void flip() {
		Platform.runLater(new Runnable() {
			public void run() {
				Board.this.orientation = !Board.this.orientation;
				
				if(orientation == Piece.BLACK) {
					for(int i = 0; i < BOARD_SIZE.getAsInt(); i++) {
						for(int j = 0; j < BOARD_SIZE.getAsInt(); j++) {
							boardDisplay[i][j] = board[BOARD_SIZE.getAsInt() - i - 1][BOARD_SIZE.getAsInt() - j - 1];
						}
					}
				}
				else {
					for(int i = 0; i < BOARD_SIZE.getAsInt(); i++) {
						for(int j = 0; j < BOARD_SIZE.getAsInt(); j++) {
							boardDisplay[i][j] = board[i][j];
						}
					}
				}
				
				
				grid.getChildren().clear();
				for (int i = 0; i < BOARD_SIZE.getAsInt(); i++) {
		            for (int j = 0; j < BOARD_SIZE.getAsInt(); j++) {
		            	Tile child = boardDisplay[i][j];
		                GridPane.setRowIndex(child, i);
		                GridPane.setColumnIndex(child, j);
		                grid.getChildren().add(child);
		            }
		        }
			}
		});
		
	}

	boolean tryMoveForLegality(int startRow, int startCol, LegalAction action) {
		Piece p = getPieceAt(startRow, startCol);
		boolean result;
		if(p == null) {
			throw new IllegalArgumentException("cannot move a nonexistent piece. (startRow, startCol) represents an empty square.");
		}
		else if(action instanceof LegalOtherMoveAndCapture) {
			LegalOtherMoveAndCapture a = (LegalOtherMoveAndCapture) action;
			result = tryMoveForLegalityMNC(a.startRow(), a.startCol(), a.destRow(), a.destCol());
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
		
		if(!result) {
			System.out.printf("Invalidated (%d,%d) -> %s", startRow, startCol, action.toString());
		}
		return result;
	}
	
	boolean tryMoveForLegalityMNC(int startRow, int startCol, int destRow, int destCol) {
		Piece p = getPieceAt(startRow, startCol);
		if(p == null) {
			throw new IllegalArgumentException("cannot move a nonexistent piece. (startRow, startCol) represents an empty square.");
		}
		else if(p instanceof King) {
			return tryMoveForLegalityMNCKing(startRow, startCol, destRow, destCol);
		}
		else {
			return tryMoveForLegalityMNCNonKing(startRow, startCol, destRow, destCol);
		}
	}
	
	private boolean tryMoveForLegalityMNCNonKing(int startRow, int startCol, int destRow, int destCol) {
		Piece p = getPieceAt(startRow, startCol);
		Piece onTile = getPieceAt(destRow, destCol);
		int[] kingSpot = p.getColor() == Piece.WHITE ? kingLocations[0] : kingLocations[1];
		boolean attackingColor = p.getColor() == Piece.WHITE ? Piece.BLACK : Piece.WHITE;
		
		board[startRow][startCol].currentPiece = null;
		board[destRow][destCol].currentPiece = p;
		boolean result = true;
		
		outer:
		for(int i = 0; i < BOARD_SIZE.getAsInt(); i++) {
			for(int j = 0; j < BOARD_SIZE.getAsInt(); j++) {
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
		
		board[startRow][startCol].currentPiece = p;
		board[destRow][destCol].currentPiece = onTile;
		
		return result;
	}
	
	private boolean tryMoveForLegalityMNCKing(int startRow, int startCol, int destRow, int destCol) {
		Piece p = getPieceAt(startRow, startCol);
		Piece onTile = getPieceAt(destRow, destCol);
		boolean attackingColor = p.getColor() == Piece.WHITE ? Piece.BLACK : Piece.WHITE;

		board[startRow][startCol].currentPiece = null;
		board[destRow][destCol].currentPiece = p;
		boolean result = true;
		
		outer:
		for(int i = 0; i < BOARD_SIZE.getAsInt(); i++) {
			for(int j = 0; j < BOARD_SIZE.getAsInt(); j++) {
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

	//PRECONDITION: rows & cols are valid, startRow/startCol represents a NON-KING piece.
	private boolean tryMoveForLegalityNonKing(int startRow, int startCol, LegalAction action) {
		int destRow = action.destRow(), destCol = action.destCol();
		Piece p = getPieceAt(startRow, startCol);
		Piece onTile = getPieceAt(destRow, destCol);
		int[] kingSpot = p.getColor() == Piece.WHITE ? kingLocations[0] : kingLocations[1];
		boolean myColor = p.getColor();
		boolean attackingColor = myColor == Piece.WHITE ? Piece.BLACK : Piece.WHITE;
		
		if(action instanceof LegalMoveAndCapture) {
			//System.out.printf("trying MNC for legality: {(%d,%d), %s}...", startRow, startCol, action);
			board[startRow][startCol].currentPiece = null;
			board[destRow][destCol].currentPiece = p;
			boolean result = true;
			
			outer:
			for(int i = 0; i < BOARD_SIZE.getAsInt(); i++) {
				for(int j = 0; j < BOARD_SIZE.getAsInt(); j++) {
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
			
			board[startRow][startCol].currentPiece = p;
			board[destRow][destCol].currentPiece = onTile;
			
			//System.out.println(result);
			return result;
		}
		else if(action instanceof LegalCapture) {
			board[destRow][destCol].currentPiece = null;
			boolean result = true;
			
			outer:
			for(int i = 0; i < BOARD_SIZE.getAsInt(); i++) {
				for(int j = 0; j < BOARD_SIZE.getAsInt(); j++) {
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
			for(Iterator<String> itr = pro.getOptions().iterator(); itr.hasNext();) {
				board[startRow][startCol].currentPiece = Piece.forName(itr.next(), myColor);
				boolean result = true;
				outer:
				for(int i = 0; i < BOARD_SIZE.getAsInt(); i++) {
					for(int j = 0; j < BOARD_SIZE.getAsInt(); j++) {
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
			for(Iterator<String> itr = sum.getOptions().iterator(); itr.hasNext();) {
				board[destRow][destCol].currentPiece = Piece.forName(itr.next(), myColor);
				boolean result = true;
				outer:
				for(int i = 0; i < BOARD_SIZE.getAsInt(); i++) {
					for(int j = 0; j < BOARD_SIZE.getAsInt(); j++) {
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

	/* *
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
			for(int i = 0; i < BOARD_SIZE.getAsInt(); i++) {
				for(int j = 0; j < BOARD_SIZE.getAsInt(); j++) {
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
		else {
			throw new IllegalArgumentException(current.getClass().getName() + " actions are not supported in multis yet.");
		}
		
	}
	
	
	/* This method will find the current locations of the kings and so DOES NOT REQUIRE KINGLOCATIONS TO BE ACCURATE.
	 * */
	private boolean isCurrentStateLegalFor(boolean color) {
		int[] kings = findKings()[color == Piece.WHITE ? 0 : 1];
		for(int i = 0; i < BOARD_SIZE.getAsInt(); i++) {
			for(int j = 0; j < BOARD_SIZE.getAsInt(); j++) {
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
		for(int i = 0; i < BOARD_SIZE.getAsInt(); i++) {
			for(int j = 0; j < BOARD_SIZE.getAsInt(); j++) {
				Piece o = getPieceAt(i, j);
				if(o instanceof King) {
					if(o.getColor() == Piece.WHITE) {
						kings[0][0] = i;
						kings[0][1] = j;
					}
					else {
						kings[1][0] = i;
						kings[1][1] = j;
					}
				}
			}
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
				case "fifty": boardPopupMessage.setText("Draw by 50 move-rule"); break;
				case "material": boardPopupMessage.setText("Draw by insufficient material"); break;
				}
				boardOverlay.setCenter(boardPopupBox);
				boardOverlay.setVisible(true);
			}
		});	
	}

	public Piece setPieceAt(int row, int col, Piece p) {
		return board[row][col].setPiece(p);
	}

	public boolean inBounds(int row, int col) {
		return row >= 0 && col >= 0 && row < BOARD_SIZE.getAsInt() && col < BOARD_SIZE.getAsInt();
	}
	
	public int getBoardSizeAsInt() {
		return BOARD_SIZE.getAsInt();
	}
	
	public boolean getBoardOrientation() {
		return orientation;
	}
	
	public Piece getPieceAt(int row, int col) {
		return board[row][col].currentPiece;
	}
	
	public Tile getTileAt(int row, int col) {
		return board[row][col];
	}
	
	public void printPieces() {
		for(int i = 0; i < BOARD_SIZE.getAsInt(); i++) {
			for(int j = 0; j < BOARD_SIZE.getAsInt(); j++) {
				System.out.print(board[i][j].currentPiece);
			}
			System.out.println();
		}
	}
}
