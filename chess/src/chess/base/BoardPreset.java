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
import java.util.List;

/* *
 * Mutable class representing a board preset.
 * The only immutable property is the board size.
 * 
 * @author 	Sam Hooper
 */
public class BoardPreset implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4593977848666590475L;
	private static ArrayList<BoardPreset> presets;
	private final int size;
	private boolean turn;
	private String[][] pieceNames;
	private String name;
	
	static {
		try {
			File dir = new File("userpresets");
			if(dir.exists()) {
				File[] files = dir.listFiles();
				if(files.length == 0) {
					File f = new File("userpresets/presets.dat");
					f.createNewFile();
				}
				else {
					if(files[0].length() == 0) {
						presets = new ArrayList<>();
					}
					else {
						FileInputStream fis = new FileInputStream(files[0]);
						try(ObjectInputStream input = new ObjectInputStream(fis)){
							Object obj = input.readObject();
							presets = ((ArrayList<BoardPreset>) obj);
						}
					}
					
				}
			}
			else {
				dir.mkdir();
				new File("userpresets/presets.dat").createNewFile();
			}
		}
		catch(Throwable t) {
			t.printStackTrace();
		}
		if(presets == null) {
			presets = new ArrayList<>();
		}
	}
	
	void setName(String newName) {
		this.name = newName;
	}
	
	public static void savePresets() {
		File dir = new File("userpresets");
		File f = new File("userpresets/presets.dat");
		try {
			dir.createNewFile();
			f.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		
		try {
			FileWriter temp = new FileWriter(f, false);
			temp.flush();
			temp.close();
			FileOutputStream fos = new FileOutputStream(f); 
			try(ObjectOutputStream oos = new ObjectOutputStream(fos)){
				oos.writeObject(presets);
				oos.flush();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean deletePreset(BoardPreset preset) {
		return presets.remove(preset);
	}
	public BoardPreset(String name, int size) {
		this.name = name;
		this.size = size;
		turn = Piece.WHITE;
		pieceNames = new String[this.size][this.size];
	}
	
	private BoardPreset(String name, boolean turn, Board b) {
		this.name = name;
		this.size = b.getBoardSize();
		this.turn = turn;
		pieceNames = new String[size][size];
		for(int i = 0; i < size; i++) {
			for(int j = 0; j < size; j++) {
				Piece p = b.getPieceAt(i, j);
				if(p != null) {
					pieceNames[i][j] = p.getFullName();
				}
			}
		}
	}
	
	@Override 
	public String toString() {
		if(name != null) {
			return name;
		}
		return super.toString();
	}
	public void setTurn(boolean color) {
		this.turn = color;
	}
	
	public boolean getTurn() {
		return turn;
	}
	
	public void setPieceNameAt(int row, int col, String name) {
		pieceNames[row][col] = name;
	}
	
	public String getPieceNameAt(int row, int col) {
		return pieceNames[row][col];
	}
	
	public int getBoardSize() {
		return size;
	}
	
	public String getName() {
		return name;
	}
	
	
	public static void saveBoardPreset(String name, boolean turn, Board b) {
		BoardPreset newPreset = new BoardPreset(name, turn, b);
		presets.add(newPreset);
	}
	
	public static ArrayList<BoardPreset> getPresets() {
		return presets;
	}
	
	/**
	 * Expects {@code size} String arrays each with with {@code size} elements.
	 * Copies the given array into its internal array.
	 * 
	 * Each String[] represents a row in the board, going from the top to the bottom.
	 * 
	 * Null can be passed to indicate a row with no pieces.
	 * 
	 * NOTE: This method does not ensure that the given piece names are valid.
	 */
	public void setPieces(String[]... pieceNamesInit) {
		if(pieceNamesInit.length != size) {
			throw new IllegalArgumentException("Invalid number of rows (" + pieceNamesInit.length + "), should be " + size);
		}
		for(int i = 0; i < size; i++) {
			if(pieceNamesInit[i] == null) {
				pieceNames[i] = new String[size];
			}
			else {
				pieceNames[i] = Arrays.copyOf(pieceNamesInit[i], size);
			}
		}
	}
	
	public String[][] getPieces() {
		String[][] end = new String[size][size];
		for(int i = 0; i < size; i++) {
			end[i] = Arrays.copyOf(pieceNames[i], size);
		}
		return end;
	}
	
	
}
