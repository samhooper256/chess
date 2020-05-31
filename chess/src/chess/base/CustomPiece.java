package chess.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import chess.util.ActionTree;
import javafx.scene.image.Image;

public class CustomPiece extends Piece{
	
	private static HashMap<String, CPFactory> definedPieces = new HashMap<>();
	private String name;
	private ActionTree tree;
	private Image image;
	private int pointValue;
	
	public static void defineNewPiece(PieceData data) {
		definedPieces.put(data.name,
			new CPFactory(data)
		);
	}
	
	public static Piece forName(String name, boolean color) {
		CPFactory factory = definedPieces.get(name);
		if(factory == null) {
			throw new IllegalArgumentException("Piece does not exist!");
		}
		else {
			return factory.make(color);
		}
	}
	
	public static int getDefinedPieceCount(){
		return definedPieces.size();
	}
	
	private CustomPiece(PieceData data, boolean color) {
		super(color);
		name = data.name;
		tree = data.tree;
		image = color == Piece.WHITE ? data.whiteImage : data.blackImage;
		pointValue = data.pointValue;
	}
	
	public static class PieceData{
		
		public final String name;
		public int pointValue;
		public Image whiteImage;
		public Image blackImage;
		public ActionTree tree;
		
		public PieceData(String name) {
			this.name = name;
			whiteImage = null;
			blackImage = null;
			tree = null;
			pointValue = 5;
		}
	}
	
	private static class CPFactory{
		private PieceData data;
		public CPFactory(PieceData data) {
			this.data = data;
		}
		
		public CustomPiece make(boolean color) {
			return new CustomPiece(data, color);
		}
	}
	
	@Override
	public Set<LegalAction> getLegalActions(Board b, int row, int col) {
		Set<LegalAction> legals = tree.getLegals(b, row, col);
		legals.removeIf(x -> !b.tryMoveForLegality(row, col, x));
		return legals;
	}
	@Override
	public boolean canCheck(Board b, int startRow, int startCol, int destRow, int destCol) {
		return tree.canCheck(b, startRow, startCol, destRow, destCol);
	}
	@Override
	public Image getImage() {
		return image;
	}
	@Override
	public int getPointValue() {
		return pointValue;
	}
	
	@Override
	public String getPieceName(){
		return name;
	}
	
	public static Collection<Piece> getInstancesOfDefinedPieces(){
		ArrayList<Piece> end = new ArrayList<>(getDefinedPieceCount()*2);
		for(CPFactory factory : definedPieces.values()) {
			end.add(factory.make(Piece.WHITE));
			end.add(factory.make(Piece.BLACK));
		}
		return end;
	}
}
