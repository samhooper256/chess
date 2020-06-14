module chess {
	exports chess.util;
	exports chess.base;
	exports chess.piecebuilder;

	opens chess.base;
	opens chess.util;
	
	requires javafx.base;
	requires javafx.controls;
	requires javafx.web;
	requires transitive javafx.graphics;
}