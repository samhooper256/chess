package chess.base;

import chess.util.ConditionClass;
import javafx.application.Platform;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;

@ConditionClass(name="legal move and capture")
public class LegalMoveAndCapture extends LegalAction{
	
	public LegalMoveAndCapture(int destRow, int destCol) {
		this.destRow = destRow;
		this.destCol = destCol;
		this.displayRow = destRow;
		this.displayCol = destCol;
	}
	
	@Override
	public boolean equals(Object o) {
		return super.equals(o);
	}
	
	private volatile boolean handleHelper = false;
	@Override
	public void handle(int startRow, int startCol, Board b) {
		System.out.printf("~~~~Handling MNC (start = (%d,%d))~~~~%n", startRow, startCol);
		Piece p = b.getPieceAt(startRow, startCol);
		
		p.setHasMoved(true);
		
		int destRow = this.destRow();
		int destCol = this.destCol();
		
		handleHelper = false;
		Object lock1 = new Object();
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				synchronized(lock1) {
					b.setPieceAt(startRow, startCol, null);
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
	public Shape indicator(int size) {
		return LegalMoveAndCapture.getIndicator(size);
	}
	
	public static Shape getIndicator(int size) {
		Circle indicator = new Circle(size);
		indicator.getStyleClass().add("move-and-capture");
		return indicator;
	}

	@Override
	public String getName() {
		return "Move and Capture";
	}

	@Override
	public String getDescription() {
		return String.format("Move and Capture on tile (%d, %d)", destRow, destCol);
	}
}
