package chess.base;

import java.util.ArrayList;

import chess.util.ConditionClass;
import javafx.application.Platform;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;

@ConditionClass(name="legal summon")
public class LegalSummon extends LegalAction{
	private ArrayList<String> options;
	
	public LegalSummon(int dispRow, int dispCol, ArrayList<String> options) {
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
		System.out.printf("~~~~Handling SUMMON (start = (%d,%d))~~~~%n", startRow, startCol);
		Piece startingPiece = b.getPieceAt(startRow, startCol);
		int destRow = this.destRow();
		int destCol = this.destCol();
		
		handleHelper = false;
		Object lock1 = new Object();
		Piece endingPiece;
		if(options.size() == 1) {
			endingPiece = Piece.forName(options.get(0), startingPiece.getColor());
		}
		else {
			endingPiece = b.moveMaker.selectPiece(startingPiece.getColor(), options);
		}
		
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
		indicator.getStyleClass().add("summon");
		return indicator;
	}

	@Override
	public String getName() {
		return "Summon";
	}

	@Override
	public String getDescription() {
		if(options.size() == 1) {
			return String.format("Summons a %s on tile (%d, %d)", options.get(0), destRow, destCol);
		}
		else {
			StringBuilder optionsString = new StringBuilder();
			for(int i = 0; i < options.size() - 1; i++) {
				optionsString.append(options.get(i)).append(", ");
			}
			optionsString.append(options.get(options.size() - 1));
			return String.format("Summons on tile (%d, %d) a choice of one of the following: %s", destRow, destCol, optionsString.toString());
		}
	}
}
