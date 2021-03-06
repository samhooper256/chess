package chess.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import javafx.scene.image.Image;

public class CustomPiece extends Piece{
	
	private static HashMap<String, CPFactory> definedPieces = new HashMap<>();
	private PieceData data;
	
	static {
		loadPieceData();
	}
	public static void defineNewPiece(PieceData data) {
		String pieceName = data.getName();
		if(definedPieces.containsKey(pieceName)) {
			throw new IllegalArgumentException("This piece already exists");
		}
		definedPieces.put(pieceName,
			new CPFactory(data)
		);
	}
	
	public static void updatePieceData(PieceData data) {
		String pieceName = data.getName();
		if(!definedPieces.containsKey(pieceName)) {
			throw new IllegalArgumentException("This piece does not exist");
		}
		definedPieces.get(pieceName).setData(data);
	}
	
	public static void deletePiece(String pieceName) {
		if(definedPieces.containsKey(pieceName)) {
			definedPieces.remove(pieceName);
		}
		else {
			throw new IllegalArgumentException("Piece does not exist");
		}
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
	
	private static void loadPieceData() {
		for(File f : Main.PIECES_FOLDER.listFiles()) {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(f);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
				continue;
			}
			try {
				if(fis == null || fis.available() == 0) {
					continue;
				}
			} catch (IOException e1) {
				e1.printStackTrace();
				continue;
			}
			try(ObjectInputStream ois = new ObjectInputStream(fis)){
				Object obj = ois.readObject();
				if(obj instanceof PieceData) {
					PieceData data = (PieceData) obj;
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
			return null;
		}
		else {
			return factory.make(color);
		}
	}
	
	public static Collection<String> getDefinedPieceNames(){
		return Collections.unmodifiableSet(definedPieces.keySet());
	}
	
	public static int getDefinedPieceCount(){
		return definedPieces.size();
	}
	
	public static Collection<Image> getAllCustomPieceImages(){
		ArrayList<Image> end = new ArrayList<>(getDefinedPieceCount() * 2);
		for(CPFactory factory : definedPieces.values()) {
			end.add(factory.getPieceData().getImage(Piece.WHITE));
			end.add(factory.getPieceData().getImage(Piece.BLACK));
		}
		return end;
	}
	
	public static boolean isDefinedPiece(String pieceName) {
		return definedPieces.containsKey(pieceName);
	}
	
	private CustomPiece(PieceData data, boolean color) {
		super(color);
		this.data = data;
	}
	
	private static class CPFactory{
		private PieceData data;
		public CPFactory(PieceData data) {
			this.data = data;
		}
		
		public CustomPiece make(boolean color) {
			return new CustomPiece(data, color);
		}
		
		public PieceData getPieceData() {
			return data;
		}
		
		private void setData(PieceData newData) {
			this.data = newData;
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
	
	@Override
	public String toString() {
		return getColor() + " + " + data.getName();
	}
}
