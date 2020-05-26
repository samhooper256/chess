package chess.base;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;

public class LegalCapture extends LegalAction{
	
	public LegalCapture(int destRow, int destCol) {
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
		System.out.printf("~~~~Handling CAPTURE (start = (%d,%d))~~~~%n", startRow, startCol);
		
		int destRow = this.destRow();
		int destCol = this.destCol();
		
		handleHelper = false;
		Object lock1 = new Object();
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				synchronized(lock1) {
					b.setPieceAt(destRow, destCol, null);
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
		indicator.getStyleClass().add("capture");
		return indicator;
	}

	@Override
	public String getName() {
		return "Capture";
	}

	@Override
	public String getDescription() {
		return "Capture on tile (%d, %d)".formatted(destRow,destCol);
	}
}
