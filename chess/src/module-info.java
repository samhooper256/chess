module chess {
	requires transitive javafx.graphics;
	requires javafx.controls;
	requires javafx.base;
	
	exports chess.base;
	exports chess.util;
}