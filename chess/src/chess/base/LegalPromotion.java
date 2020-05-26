package chess.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;

public class LegalPromotion extends LegalAction{
	
	private ArrayList<String> options;
	
	public LegalPromotion(int dispRow, int dispCol, ArrayList<String> options) {
		this.displayRow = dispRow;
		this.displayCol = dispCol;
		this.destRow = dispRow;
		this.destCol = dispCol;
		this.options = new ArrayList<>(options);
	}
	
	public ArrayList<String> getOptions(){
		return options;
	}
	
	private volatile boolean handleHelper = false;
	@Override
	public void handle(int startRow, int startCol, Board b) {
		System.out.printf("~~~~Handling PROMO (start = (%d,%d))~~~~%n", startRow, startCol);
		Piece startingPiece = b.getPieceAt(startRow, startCol);
		
		int destRow = this.destRow();
		int destCol = this.destCol();
		
		handleHelper = false;
		Object lock1 = new Object();
		
		Piece endingPiece = b.moveMaker.selectPiece(startingPiece.getColor(), options);
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				synchronized(lock1) {
					b.setPieceAt(destRow, destCol, endingPiece);
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
		indicator.getStyleClass().add("promotion");
		return indicator;
	}

	@Override
	public String getName() {
		return "Promotion";
	}

	@Override
	public String getDescription() {
		StringBuilder optionsString = new StringBuilder();
		for(int i = 0; i < options.size() - 1; i++) {
			optionsString.append(options.get(i)).append(", ");
		}
		optionsString.append(options.get(options.size() - 1));
		return "Promotes the acting piece to a choice of one of the following: %s".formatted(optionsString.toString());
	}
}
