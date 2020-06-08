package chess.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import chess.piecebuilder.PieceBuilder;
import chess.util.ActionTree;
import javafx.scene.image.Image;

public class CustomPiece extends Piece{
	
	private static HashMap<String, CPFactory> definedPieces = new HashMap<>();
	private PieceData data;
	
	static {
		loadPieceData();
	}
	public static void defineNewPiece(PieceData data) {
		definedPieces.put(data.getName(),
			new CPFactory(data)
		);
	}
	
	public static PieceData getDataFor(String name) {
		CPFactory factory = definedPieces.get(name);
		if(factory == null) {
			throw new IllegalArgumentException("Invalid piece name:\""+name+"\"");
		}
		else {
			return factory.getPieceData();
		}
	}
	
	private static void deleteFolderContents(File folder) {
	    File[] files = folder.listFiles();
	    if(files!=null) { //some JVMs return null for empty dirs
	        for(File f: files) {
	            if(f.isDirectory()) {
	            	deleteFolderContents(f);
	            } else {
	                f.delete();
	            }
	        }
	    }
	}
	
	private static void loadPieceData() {
		File userpieces = new File("userpieces");
		for(File datFile : userpieces.listFiles()) {
			ObjectInputStream ois = null;
			try {
				ois = new ObjectInputStream(new FileInputStream(datFile));
				Object readObject = ois.readObject();
				if(readObject instanceof PieceData) {
					PieceData data = (PieceData) readObject;
					data.updateImages();
					CustomPiece.defineNewPiece(data);
				}
				ois.close();
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
			
		}
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
	
	public static Collection<String> getDefinedPieceNames(){
		return definedPieces.keySet();
	}
	
	public static int getDefinedPieceCount(){
		return definedPieces.size();
	}
	
	public static boolean isDefinedPiece(String pieceName) {
		return definedPieces.containsKey(pieceName);
	}
	
	private CustomPiece(PieceData data, boolean color) {
		super(color);
		this.data = data;
	}
	
	private static class CPFactory{
		private final PieceData data;
		public CPFactory(PieceData data) {
			this.data = data;
		}
		
		public CustomPiece make(boolean color) {
			return new CustomPiece(data, color);
		}
		
		public PieceData getPieceData() {
			return data;
		}
	}
	
	@Override
	public Set<LegalAction> getLegalActions(Board b, int row, int col) {
		Set<LegalAction> legals = data.getTree().getLegals(b, row, col);
		legals.removeIf(x -> !b.tryMoveForLegality(row, col, x));
		return legals;
	}
	@Override
	public boolean canCheck(Board b, int startRow, int startCol, int destRow, int destCol) {
		return data.getTree().canCheck(b, startRow, startCol, destRow, destCol);
	}
	@Override
	public Image getImage() {
		return data.getImage(this.getColor());
	}
	@Override
	public int getPointValue() {
		return data.getPointValue();
	}
	
	@Override
	public String getPieceName(){
		return data.getName();
	}
	
	@Override
	public PieceData getPieceData() {
		return data;
	}
	
	public static Collection<Piece> getInstancesOfDefinedPieces(){
		ArrayList<Piece> end = new ArrayList<>(getDefinedPieceCount()*2);
		for(CPFactory factory : definedPieces.values()) {
			end.add(factory.make(Piece.WHITE));
			end.add(factory.make(Piece.BLACK));
		}
		return end;
	}

	@Override
	public PieceType getPieceType() {
		return data.getPieceType();
	}

	public static boolean isDefinedPieceName(String name) {
		return definedPieces.containsKey(name);
	}
}
