package chess.base;

import chess.util.ConditionClass;
import javafx.application.Platform;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;

@ConditionClass(name="legal other move and capture")
public class LegalOtherMoveAndCapture extends LegalAction{
	private final int startRow, startCol;
	public LegalOtherMoveAndCapture(int startRow, int startCol, int destRow, int destCol) {
		this.startRow = startRow;
		this.startCol = startCol;
		this.destRow = destRow;
		this.destCol = destCol;
		this.displayRow = destRow;
		this.displayCol = destCol;
	}
	
	public int startRow() {
		return startRow;
	}
	
	public int startCol() {
		return startCol;
	}
	
	@Override
	public boolean equals(Object o) {
		return super.equals(o);
	}
	
	@Override
	public String toString() {
		return String.format("%s = [start=(%d,%d), dest=(%d,%d), disp=(%d,%d)]", this.getClass().getName(), 
				startRow,startCol,destRow,destCol,displayRow,displayCol);
	}
	
	private volatile boolean handleHelper = false;
	@Override
	public void handle(int callinStartRow, int callingStartCol, Board b) {
		System.out.printf("~~~~Handling OMNC (start of calling piece = (%d,%d))~~~~%n", startRow, startCol);
		
		Piece p = b.getPieceAt(LegalOtherMoveAndCapture.this.startRow, LegalOtherMoveAndCapture.this.startCol);
		if(p != null) {
			p.setHasMoved(true);
		}
		int destRow = this.destRow();
		int destCol = this.destCol();
		
		handleHelper = false;
		Object lock1 = new Object();
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				synchronized(lock1) {
					b.setPieceAt(LegalOtherMoveAndCapture.this.startRow, LegalOtherMoveAndCapture.this.startCol, null);
					b.setPieceAt(destRow, destCol, p);
					handleHelper = true;
					lock1.notifyAll();
				}
				
			}
			
		});
		synchronized(lock1) {
			while(!handleHelper) {
				try {
					lock1.wait();
				} catch (InterruptedException e) {}
			}
		}
		handleHelper = false;
	}

	@Override
	public Shape getIndicator(int size) {
		Circle indicator = new Circle(size);
		indicator.getStyleClass().add("other-move-and-capture");
		return indicator;
	}

	@Override
	public String getName() {
		return "Other Piece Move and Capture";
	}

	@Override
	public String getDescription() {
		return String.format("Moves the piece on tile (%d, %d) to (%d, %d)", startRow, startCol, destRow, destCol);
	}
}
