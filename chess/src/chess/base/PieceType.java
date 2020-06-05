package chess.base;

import chess.util.AFC;

/* *
 * Immutable class representing a piece type (Pawn, Rook, Knight, etc.)
 * One PieceType should be shared across all instances of a certain piece.
 */
public class PieceType {
	private boolean isCustom;
	private String name;
	
	public static PieceType define(String name, boolean isCustom) {
		if(name == null) {
			throw new NullPointerException("name is null");
		}
		return new PieceType(name, isCustom);
	}
	
	private PieceType(String name, boolean isCustom) {
		this.name = name;
		this.isCustom = isCustom;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof PieceType) {
			return this == o || this.name.equals(((PieceType) o).name);
		}
		return false;
	}
	
	@AFC(name="is custom")
	public boolean isCustom() {
		return isCustom;
	}
}
